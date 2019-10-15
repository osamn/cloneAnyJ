package ReIW.tiny.cloneAny.pojo.impl;

import static ReIW.tiny.cloneAny.utils.Consumers.withIndex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import ReIW.tiny.cloneAny.asm7.DefaultClassVisitor;
import ReIW.tiny.cloneAny.asm7.DefaultMethodVisitor;
import ReIW.tiny.cloneAny.pojo.Accessor.AccessType;
import ReIW.tiny.cloneAny.pojo.Accessor.FieldAccess;
import ReIW.tiny.cloneAny.pojo.Accessor.IndexedAccess;
import ReIW.tiny.cloneAny.pojo.Accessor.KeyedAccess;
import ReIW.tiny.cloneAny.pojo.Accessor.LumpSetAccess;
import ReIW.tiny.cloneAny.pojo.Accessor.PropAccess;
import ReIW.tiny.cloneAny.pojo.UnboundFormalTypeParameterException;
import ReIW.tiny.cloneAny.pojo.UnboundMethodParameterNameException;
import ReIW.tiny.cloneAny.utils.AccessFlag;
import ReIW.tiny.cloneAny.utils.Propertys;

final class ClassTypeBuilder extends DefaultClassVisitor {

	private final Map<String, ClassType> hive = Collections.synchronizedMap(new WeakHashMap<>());

	ClassType buildClassType(Class<?> clazz) {
		return buildClassType(Type.getDescriptor(clazz));
	}

	ClassType buildClassType(String descriptor) {
		final ClassType ct = hive.computeIfAbsent(descriptor, this::computeClassType);
		ct.complete();
		return ct;
	}

	private ClassType computeClassType(String descriptor) {
		if (descriptor.startsWith("[")) {
			// 配列の場合はそのアクセサだけ追加してかえす
			// クラスのパースはしないよ
			final SlotValue slot = new SlotValueBuilder(null).build(descriptor);
			final SlotValue elementSlot = slot.slotList.get(0);
			final ClassType ct = new ClassType();
			ct.thisSlot = slot;
			ct.accessors.add(new IndexedAccess(AccessType.ArrayGet, elementSlot));
			ct.accessors.add(new IndexedAccess(AccessType.ArraySet, elementSlot));
			return ct;
		}
		classType = new ClassType();
		classType.thisSlot = new SlotValue(null, descriptor);
		ownerName = Type.getType(descriptor).getInternalName();
		try {
			new ClassReader(ownerName).accept(this, 0);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
		return classType;
	}

	private ClassType classType;
	private String ownerName;

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		new ClassSignatureParser(classType.thisSlot.slotList::add, superSlot -> {
			classType.supers.add(superSlot);
			// extends/implements してるスロットについて List/Map をチェックする
			if (superSlot.descriptor.contentEquals("Ljava/util/List;")) {
				classType.accessors.add(new IndexedAccess(AccessType.ListGet, superSlot.slotList.get(0)));
				classType.accessors.add(new IndexedAccess(AccessType.ListAdd, superSlot.slotList.get(0)));
			}
			if (superSlot.descriptor.contentEquals("Ljava/util/Map;")) {
				classType.accessors
						.add(new KeyedAccess(AccessType.MapGet, superSlot.slotList.get(0), superSlot.slotList.get(1)));
				classType.accessors
						.add(new KeyedAccess(AccessType.MapPut, superSlot.slotList.get(0), superSlot.slotList.get(1)));
			}
		}).parse(superName, interfaces, signature);
	}

	@Override
	public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
		if (isAccessible(access)) {
			// final なものは読み取り専用になるよ
			final AccessType type = AccessFlag.isFinal(access) ? AccessType.ReadonlyField : AccessType.Field;
			new FieldSignatureParser(slot -> classType.accessors.add(new FieldAccess(type, ownerName, name, slot)))
					.parse(descriptor, signature);
		}
		return null;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
			String[] exceptions) {
		if (!isAccessible(access)) {
			return null;
		}

		if (name.contentEquals("<init>")) {
			final LumpSetAccess ctor = new LumpSetAccess(ownerName, name, descriptor);
			classType.accessors.add(ctor);

			if (descriptor.contentEquals("()V")) {
				// 引数ないんで終了
				return null;
			}

			// 引数のスロットをパースしてリストを作る
			final ArrayList<SlotValue> params = new ArrayList<>();
			// ちなみに <init> にフォーマルパラメタはないので try で囲むひつようなし
			new MethodSignatureParser(params::add, null).parseArgumentsAndReturn(descriptor, signature);

			// そのスロットとパラメタ名とくっつけてもらうように MethodVisitor をかえしてあげる
			return new MethodParamNameMapper(params, ctor.slotInfo::put);
		} else {
			try {
				if (Propertys.isGetter(name, descriptor)) {
					new MethodSignatureParser(null, slot -> {
						classType.accessors.add(new PropAccess(AccessType.Get, ownerName,
								Propertys.getPropertyName(name), name, descriptor, slot));
					}).parseArgumentsAndReturn(descriptor, signature);
				} else if (Propertys.isSetter(name, descriptor)) {
					new MethodSignatureParser(slot -> {
						classType.accessors.add(new PropAccess(AccessType.Set, ownerName,
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
		return null;

	}

	private static boolean isAccessible(int access) {
		return AccessFlag.isPublic(access) // public で
				&& !AccessFlag.isStatic(access) // instance で
				&& !AccessFlag.isAbstract(access); // concrete で
		// lombok とかがつけそうなので synthetic は判定しないでおく
		// && !AccessFlag.isSynthetic(access);
	}

	static Map<String, String> createBindMap(final SlotValue lhs/*this.super[0]*/, final SlotValue rhs/*super.this*/) {
		final HashMap<String, String> map = new HashMap<>();
		// class Bar<X, Y> >>> super.this -> definedSlot
		// -> X, Y
		// に対して
		// class Foo<A> extends Bar<A, String> >>> this.super[0] -> extendsSlot
		// -> X, TA
		// -> Y, String
		// この対応をマップとして作成する
		rhs.slotList.forEach(withIndex((definedSlot, i) -> {
			// extends 元のクラスで宣言されている type argument を退避
			final SlotValue extendsSlot = lhs.slotList.get(i);
			// で、それらを比べてなにが型パラメタにくっついたかを調べる
			// それぞれの型パラメタの数とか並び順はコンパイルとおってるかぎり絶対一致してるはずだよ

			if (extendsSlot.isCertainBound()) {
				// 型パラメタが解決されてるので、そいつをくっつける
				// TODO シグネチャ考えなくていいのはなんで？コメントつけといて
				map.put(definedSlot.typeParam, extendsSlot.getTypeDescriptor());
			} else {
				// 型パラメタをリネームする。目印として 'T' をつける
				// 以下より T で始まる型引数はありえないため T を目印にしてるよ
				//// Object -> L
				//// void -> V
				//// primitive -> ZCBSIFJD
				//// array -> [
				map.put(definedSlot.typeParam, "T" + extendsSlot.typeParam);
			}
		}));
		return map;
	}

	private static final class MethodParamNameMapper extends DefaultMethodVisitor {

		private final Iterator<SlotValue> slots;
		private final BiConsumer<String, SlotValue> cons;

		private MethodParamNameMapper(final List<SlotValue> slotList, final BiConsumer<String, SlotValue> cons) {
			this.slots = slotList.iterator();
			this.cons = cons;
		}

		/*
		 * You need to compile your class with the -parameters option to make javac
		 * include the parameter names.
		 */
		// public void visitParameter(String name, int access)
		// ということで visitParameter はコンパイルオプション依存らしいので使えない

		@Override
		public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end,
				int index) {
			// this(0) を除いた引数だけ処理する
			// あとイテレータでガードしないと引数じゃないローカル変数まで処理しちゃうよ
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
				// ということでエラーにしておく
				throw new UnboundMethodParameterNameException("No debug symbols.");
			}
		}

	}
}
