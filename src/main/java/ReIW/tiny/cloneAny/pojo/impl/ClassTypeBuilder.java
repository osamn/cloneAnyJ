package ReIW.tiny.cloneAny.pojo.impl;

import static ReIW.tiny.cloneAny.utils.Consumers.withIndex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;
import java.util.function.ObjIntConsumer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import ReIW.tiny.cloneAny.asm7.DefaultClassVisitor;
import ReIW.tiny.cloneAny.asm7.DefaultMethodVisitor;
import ReIW.tiny.cloneAny.pojo.Accessor.AccessType;
import ReIW.tiny.cloneAny.pojo.Accessor.FieldAccess;
import ReIW.tiny.cloneAny.pojo.Accessor.SequentialAccess;
import ReIW.tiny.cloneAny.pojo.Accessor.KeyedAccess;
import ReIW.tiny.cloneAny.pojo.Accessor.LumpSetAccess;
import ReIW.tiny.cloneAny.pojo.Accessor.PropAccess;
import ReIW.tiny.cloneAny.pojo.UnboundFormalTypeParameterException;
import ReIW.tiny.cloneAny.pojo.UnboundMethodParameterNameException;
import ReIW.tiny.cloneAny.utils.AccessFlag;
import ReIW.tiny.cloneAny.utils.Propertys;

public final class ClassTypeBuilder extends DefaultClassVisitor {

	private final Map<String, ClassType> hive = Collections.synchronizedMap(new WeakHashMap<>());

	public ClassType buildClassType(String descriptor) {
		final ClassType ct = hive.computeIfAbsent(descriptor, this::computeClassType);
		ct.complete();
		return ct;
	}

	private ClassType computeClassType(String descriptor) {
		if (descriptor.startsWith("[")) {
			// 配列の場合はそのアクセサだけ追加してかえす
			// で、配列なんで superSlot は null だよ
			final SlotValue slot = new SlotValueBuilder().build(descriptor);
			final SlotValue elementSlot = slot.slotList.get(0);
			final ClassType ct = new ClassType(slot);
			// owner がいないのでアクセサの owner も明示的に null にしておく
			ct.accessors.add(new SequentialAccess(AccessType.ArrayType, null, elementSlot));
			return ct;
		}

		final String className = Type.getType(descriptor).getInternalName();
		// formal slot はビルダが追加するのでそのルートの slot だけつくっておく
		final SlotValue thisRoot = new SlotValue(null, null, descriptor);
		this.classType = new ClassType(thisRoot);
		try {
			new ClassReader(className).accept(this, 0);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
		return classType;
	}

	private ClassType classType;

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		if (AccessFlag.isInterface(access)) {
			// インターフェースだと右側に指定されたとき何を new すればいいかわからんので
			throw new IllegalArgumentException();
		}
		// abstract とか not public なクラスでも継承ツリー上あるものは全部処理するよ
		new ClassSignatureParser(classType.thisSlot.slotList::add, withIndex(addAncestorTo(classType))).parse(signature,
				superName, interfaces);
	}

	// スロットを、指定された ClassType の継承階層として追加するひとを返すひと
	// List と Map のインターフェース見つけたらアクセサ追加しとく処理もあるんで別途切り出しておいてみた
	private static ObjIntConsumer<SlotValue> addAncestorTo(final ClassType ct) {
		return (slot, i) -> {
			if (i == 0) {
				// extends しているクラスなので superSlot として設定する
				ct.superSlot = slot;
				// 継承階層として追加しておく
				ct.ancestors.add(slot.descriptor);
				return;
			}
			// i > 0 のスロットは implements として宣言されてるインターフェース
			// List/Map のアクセサが重複して登録されないように、すでに登録されているか ancestors を確認する
			if (ct.ancestors.add(slot.descriptor)) {
				// 新規に追加されたので List/Map のアクセサ追加してもいいよ
				if (slot.descriptor.contentEquals("Ljava/util/List;")) {
					ct.accessors
							.add(new SequentialAccess(AccessType.ListType, ct.thisSlot.descriptor, slot.slotList.get(0)));
				}
				if (slot.descriptor.contentEquals("Ljava/util/Set;")) {
					ct.accessors
							.add(new SequentialAccess(AccessType.SetType, ct.thisSlot.descriptor, slot.slotList.get(0)));
				}
				if (slot.descriptor.contentEquals("Ljava/util/Map;")) {
					ct.accessors.add(new KeyedAccess(AccessType.MapType, ct.thisSlot.descriptor, slot.slotList.get(0),
							slot.slotList.get(1)));
				}
			}
		};
	}

	@Override
	public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
		if (isAccessible(access)) {
			// final なものは読み取り専用になるよ
			final AccessType type = AccessFlag.isFinal(access) ? AccessType.ReadonlyField : AccessType.Field;
			new FieldSignatureParser(
					slot -> classType.accessors.add(new FieldAccess(type, classType.thisSlot.descriptor, name, slot)))
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
			// コンストラクタの場合
			final LumpSetAccess ctor = new LumpSetAccess(classType.thisSlot.descriptor, name, descriptor);
			classType.accessors.add(ctor);

			if (descriptor.contentEquals("()V")) {
				// 引数ないんで終了
				return null;
			}

			// descriptor/signature をパースして型パラメタのスロットのリストを作る
			final ArrayList<SlotValue> params = new ArrayList<>();
			// ちなみに <init> にフォーマルパラメタはありえないので try で囲むひつようなし
			new MethodSignatureParser(params::add, null).parseArgumentsAndReturn(descriptor, signature);

			// そのスロットとパラメタ名とくっつけてもらうように MethodVisitor をかえしてあげる
			return new MethodParamNameMapper(params, ctor.parameters::put);
		} else {
			try {
				// FIXME プロパティ名作るところはほんとは BeanInfo を先に見ないといかんとおもう
				if (Propertys.isGetter(name, descriptor)) {
					new MethodSignatureParser(null, slot -> {
						classType.accessors.add(new PropAccess(AccessType.Getter, classType.thisSlot.descriptor,
								Propertys.getPropertyName(name), name, descriptor, slot));
					}).parseArgumentsAndReturn(descriptor, signature);
				} else if (Propertys.isSetter(name, descriptor)) {
					new MethodSignatureParser(slot -> {
						classType.accessors.add(new PropAccess(AccessType.Setter, classType.thisSlot.descriptor,
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
				&& !AccessFlag.isStatic(access) // インスタンスのメンバで
				&& !AccessFlag.isInterface(access) // インターフェース上の定義は実装がないのでだめ
				&& !AccessFlag.isAbstract(access) // abstract も実装がないのでだめ
				&& !AccessFlag.isSynthetic(access);
		// generic を継承してメソッドを実装するとスロット Object を使う synthetic のメソッドができるの
		// でアクセサのリスト作るときに Object と type_argument 版の２エントリができていやな感じ
		// なんで synthetic は取り除いておく
		// TODO lombok とかがつけそうなんだけど -> synthetic
	}

	// メソッドのパラメタ名を対応するスロットにくっつけるひと
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
