package ReIW.tiny.cloneAny.pojo.impl;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import ReIW.tiny.cloneAny.asm7.DefaultClassVisitor;
import ReIW.tiny.cloneAny.asm7.DefaultMethodVisitor;
import ReIW.tiny.cloneAny.core.AccessFlag;
import ReIW.tiny.cloneAny.pojo.AccessEntry;
import ReIW.tiny.cloneAny.pojo.PropertyUtil;
import ReIW.tiny.cloneAny.pojo.Slot;
import ReIW.tiny.cloneAny.pojo.UnboundFormalTypeParameterException;
import ReIW.tiny.cloneAny.pojo.UnboundMethodParameterNameException;

public final class TypeSlotBuilder extends DefaultClassVisitor {

	// いつまでも hive 抱えててもいかんので GC で回収されるように弱参照をもっておく
	private static WeakReference<ConcurrentHashMap<Class<?>, TypeSlot>> hiveRef = new WeakReference<>(
			new ConcurrentHashMap<>());

	public static TypeSlot createTypeDef(final Class<?> clazz) {
		ConcurrentHashMap<Class<?>, TypeSlot> hive;
		synchronized (hiveRef) {
			hive = hiveRef.get();
			if (hive == null) {
				hive = new ConcurrentHashMap<>();
				hiveRef = new WeakReference<>(hive);
			}
		}
		final TypeSlot td = hive.computeIfAbsent(clazz, TypeSlotBuilder::build);
		td.complete();
		return td;
	}

	private static TypeSlot build(final Class<?> clazz) {
		try {
			final TypeSlotBuilder builder = new TypeSlotBuilder(new TypeSlot(clazz));
			new ClassReader(Type.getType(clazz).getInternalName()).accept(builder, 0);
			return builder.typeSlot;
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private final TypeSlot typeSlot;

	private TypeSlotBuilder(final TypeSlot typeDef) {
		this.typeSlot = typeDef;
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		new ClassSignatureParser(typeSlot.slotList::add, typeSlot.superSlots::add).parse(superName, interfaces,
				signature);
	}

	@Override
	public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
		if (isAccessible(access)) {
			// final なものは読み取り専用になるよ
			final int aceType = AccessFlag.isFinal(access) ? AccessEntry.ACE_FINAL_FIELD : AccessEntry.ACE_FIELD;
			new FieldSignatureParser(slot -> typeSlot.access.add(new AccessEntry(aceType, name, slot, name)))
					.parse(descriptor, signature);
		}
		return null;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
			String[] exceptions) {
		if (isAccessible(access)) {
			if (name.contentEquals("<init>")) {
				if (descriptor.contentEquals("()V")) {
					typeSlot.hasDefaultCtor = true;
				}
				final ArrayList<Slot> slots = new ArrayList<>();
				new MethodSignatureParser(slots::add, null).parseArgumentsAndReturn(descriptor, signature);
				return new MethodParamNameMapper(slots, (paramName, slot) -> {
					typeSlot.access.add(new AccessEntry(AccessEntry.ACE_CTOR_ARG, paramName, slot, descriptor));
				});
			} else {
				try {
					if (PropertyUtil.isGetter(name, descriptor)) {
						new MethodSignatureParser(null, slot -> {
							final String propName = PropertyUtil.getPropertyName(name);
							typeSlot.access.add(new AccessEntry(AccessEntry.ACE_PROP_GET, propName, slot, name));
						}).parseArgumentsAndReturn(descriptor, signature);
					} else if (PropertyUtil.isSetter(name, descriptor)) {
						new MethodSignatureParser(slot -> {
							final String propName = PropertyUtil.getPropertyName(name);
							typeSlot.access.add(new AccessEntry(AccessEntry.ACE_PROP_SET, propName, slot, name));
						}, null).parseArgumentsAndReturn(descriptor, signature);
					}
				} catch (UnboundFormalTypeParameterException e) {
					// プロパティっぽいけど、メソッド自体に型パラメタがあるので無視する
					// public <X> X getHoge()
					// みたいなやつ
				}
			}
		}
		return null;
	}

	private static boolean isAccessible(int access) {
		return AccessFlag.isPublic(access) // public で
				&& !AccessFlag.isStatic(access) // instance で
				&& !AccessFlag.isAbstract(access) // concrete で
				&& !AccessFlag.isSynthetic(access); // コンパイラが生成したものじゃない
	}

	private static final class MethodParamNameMapper extends DefaultMethodVisitor {

		private final Iterator<Slot> slots;
		private final BiConsumer<String, Slot> cons;

		private MethodParamNameMapper(final ArrayList<Slot> slots, final BiConsumer<String, Slot> cons) {
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
				cons.accept(name, slots.next());
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
