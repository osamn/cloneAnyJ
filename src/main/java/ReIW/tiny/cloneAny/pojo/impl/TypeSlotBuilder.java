package ReIW.tiny.cloneAny.pojo.impl;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import ReIW.tiny.cloneAny.asm7.DefaultClassVisitor;
import ReIW.tiny.cloneAny.asm7.DefaultMethodVisitor;
import ReIW.tiny.cloneAny.pojo.Accessor;
import ReIW.tiny.cloneAny.pojo.Slot;
import ReIW.tiny.cloneAny.pojo.UnboundFormalTypeParameterException;
import ReIW.tiny.cloneAny.pojo.UnboundMethodParameterNameException;
import ReIW.tiny.cloneAny.utils.AccessFlag;
import ReIW.tiny.cloneAny.utils.Propertys;

// TODO lombok 使ってるとき signature のこってるの？

public final class TypeSlotBuilder extends DefaultClassVisitor {

	// いつまでも hive 抱えててもいかんので GC で回収されるように弱参照をもっておく
	private static WeakReference<ConcurrentHashMap<Class<?>, TypeSlot>> hiveRef = new WeakReference<>(
			new ConcurrentHashMap<>());

	private TypeSlot typeSlot;

	public TypeSlotBuilder() {
	}

	public TypeSlot buildTypeSlot(final Class<?> clazz) {
		ConcurrentHashMap<Class<?>, TypeSlot> hive;
		synchronized (TypeSlotBuilder.class) {
			hive = hiveRef.get();
			if (hive == null) {
				hive = new ConcurrentHashMap<>();
				hiveRef = new WeakReference<>(hive);
			}
		}
		final TypeSlot ts = hive.computeIfAbsent(clazz, this::computeTypeSlot);
		ts.complete();
		return ts;
	}

	private TypeSlot computeTypeSlot(final Class<?> clazz) {
		try {
			if (clazz.isArray()) {
				// クラスファイルとして定義されているものが対象なんで配列型はありえない
				throw new IllegalArgumentException("Top level class should not be array type.");
			}
			typeSlot = new TypeSlot(null, Type.getDescriptor(clazz));
			new ClassReader(Type.getInternalName(clazz)).accept(this, 0);
			return typeSlot;
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		new ClassSignatureParser(typeSlot.slotList::add, slot -> {
			if (slot.descriptor.contentEquals("Ljava/util/List;")) {
				typeSlot.listSlot = slot;
			} else if (slot.descriptor.contentEquals("Ljava/util/Map;")) {
				typeSlot.mapSlot = slot;
			} else if (slot.descriptor.contentEquals("Ljava/lang/CharSequence;")) {
				typeSlot.charSequence = true;
			}
			typeSlot.superSlots.add(slot);
		}).parse(superName, interfaces, signature);
	}

	@Override
	public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
		if (isAccessible(access)) {
			// final なものは読み取り専用になるよ
			final Accessor.Kind type = AccessFlag.isFinal(access) ? Accessor.Kind.ReadonlyField : Accessor.Kind.Field;
			final Slot slot = Slot.getSlot(null, descriptor, signature);
			typeSlot.access.add(new SingleSlotAccessor(type, typeSlot.getName(), name, name, descriptor, slot));
		}
		return null;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
			String[] exceptions) {
		if (isAccessible(access)) {
			if (name.contentEquals("<init>")) {
				if (descriptor.contentEquals("()V")) {
					typeSlot.defaultCtor = true;
				}
				final MultiSlotAccessor acc = new MultiSlotAccessor(typeSlot.getName(), name, name, descriptor);
				typeSlot.access.add(acc);
				new MethodSignatureParser(acc.slots::add, null).parseArgumentsAndReturn(descriptor, signature);
				return new MethodParamNameMapper(acc.slots, acc.names::add);
			} else {
				try {
					if (Propertys.isGetter(name, descriptor)) {
						new MethodSignatureParser(null, slot -> {
							typeSlot.access.add(new SingleSlotAccessor(Accessor.Kind.Get, typeSlot.getName(),
									Propertys.getPropertyName(name), name, descriptor, slot));
						}).parseArgumentsAndReturn(descriptor, signature);
					} else if (Propertys.isSetter(name, descriptor)) {
						new MethodSignatureParser(slot -> {
							typeSlot.access.add(new SingleSlotAccessor(Accessor.Kind.Set, typeSlot.getName(),
									Propertys.getPropertyName(name), name, descriptor, slot));
						}, null).parseArgumentsAndReturn(descriptor, signature);
					}
				} catch (UnboundFormalTypeParameterException e) {
					// プロパティっぽいけど、メソッド自体に型パラメタがあるので無視する
					// public <X> X getHoge()
					// public <X> void setHoge(X val)
					// みたいなやつ
				}
			}
		}
		return null;
	}

	private static boolean isAccessible(int access) {
		return AccessFlag.isPublic(access) // public で
				&& !AccessFlag.isStatic(access) // instance で
				&& !AccessFlag.isAbstract(access); // concrete で
		// lombok とかがつけそうなので synthetic は判定しないでおく
		// && !AccessFlag.isSynthetic(access);
	}

	private static final class MethodParamNameMapper extends DefaultMethodVisitor {

		private final Iterator<Slot> slots;
		private final Consumer<String> cons;

		private MethodParamNameMapper(final List<Slot> slots, final Consumer<String> cons) {
			this.slots = slots.iterator();
			this.cons = cons;
		}

		/*
		 * You need to compile your class with the -parameters option to make javac
		 * include the parameter names. ということで visitParameter はコンパイルオプション依存らしいので使えない
		 */
		// public void visitParameter(String name, int access)

		@Override
		public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end,
				int index) {
			// this(0) を除いた引数だけ処理する
			// ガードしないと引数じゃないローカル変数まで処理しちゃうので
			if (0 < index && slots.hasNext()) {
				cons.accept(name);
				slots.next();
			}
		}

		@Override
		public void visitEnd() {
			if (slots.hasNext()) {
				// 対象が残っている
				// -> visitLocalVariable が呼ばれていない
				// -> debug 情報がついていない
				// なのでコンストラクタパラメタに名前でマッチングできない
				// なのでエラーにしておく
				throw new UnboundMethodParameterNameException("No debug symbols.");
			}
		}
	}
}
