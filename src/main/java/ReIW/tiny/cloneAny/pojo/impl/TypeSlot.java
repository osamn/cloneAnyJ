package ReIW.tiny.cloneAny.pojo.impl;

import static ReIW.tiny.cloneAny.utils.Consumers.withIndex;
import static ReIW.tiny.cloneAny.utils.Descriptors.toInternalName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import ReIW.tiny.cloneAny.pojo.Accessor;
import ReIW.tiny.cloneAny.pojo.Slot;
import ReIW.tiny.cloneAny.pojo.TypeDef;

public final class TypeSlot extends Slot implements TypeDef {

	// -- builder が設定するものは package スコープ

	final ArrayList<Slot> superSlots = new ArrayList<>();

	final ArrayList<Accessor> access = new ArrayList<>();

	Slot listSlot;

	Slot mapSlot;

	boolean charSequence;

	boolean defaultCtor;

	// 階層含めて完了したか？
	private boolean completed;

	TypeSlot(final String typeParam, final String descriptor) {
		super(typeParam, descriptor);
	}

	@Override
	public boolean hasDefaultCtor() {
		return defaultCtor;
	}

	@Override
	public boolean isList() {
		return listSlot != null;
	}

	@Override
	public boolean isMap() {
		return mapSlot != null && mapSlot.slotList.get(0).descriptor.contentEquals("Ljava/lang/String;");
	}

	@Override
	public boolean isCharSequence() {
		return charSequence;
	}

	@Override
	public Slot elementSlot() {
		if (isArrayType) {
			return slotList.get(0);
		}
		return listSlot.slotList.get(0);
	}

	@Override
	public Slot valueSlot() {
		return mapSlot.slotList.get(1);
	}

	@Override
	public Stream<Accessor> accessors() {
		return access.stream();
	}

	@Override
	public Slot toSlot() {
		return this;
	}

	/**
	 * 未解決の型パラメタを指定引数で解決したアクセサのストリームを作るためのラッパをつくる.
	 * 
	 * フィールドとかメソッドの戻りとか引数とかで直接 generic を指定しているタイプのアクセサから引っ張ってきた TypeDef
	 * のアクセサをとる場合はこっち
	 */
	public TypeDef bind(final List<Slot> binds) {
		if (binds.size() == 0) {
			return this;
		}
		return new Binder(binds);
	}

	// complete から再帰的に継承元をたどることで
	// 結果として継承階層上のすべての Accessor が含まれる形になる
	void complete() {
		// 何回も呼ばれる可能性もあるのでガードしとく
		if (completed) {
			return;
		}
		completed = true;

		// 親が Object.class だったら継承階層のルートまでたどってるので終了する
		// 継承元が配列はありえないので array を考慮する必要ない
		// なんで getTypeDescriptor じゃなくておｋ
		final String superDesc = superSlots.get(0).descriptor;
		if (superDesc.contentEquals("Ljava/lang/Object;")) {
			return;
		}

		// 親の TypeSlot を作って
		final TypeSlot superType = new TypeSlotBuilder().buildTypeSlot(superDesc);
		superType.complete();
		final HashMap<String, String> binds = createBindMap(superType);
		// 親のアクセサを自分に持ってくる
		pullAllUp(binds, superType);
		// リスト/マップのスロットをバインド済みにしておく
		if (superType.listSlot != null) {
			listSlot = superType.listSlot.rebind(binds);
		}
		if (superType.mapSlot != null) {
			mapSlot = superType.mapSlot.rebind(binds);
		}
	}

	// スーパークラス上で公開されたアクセスエントリを自分の型引数をバインドして
	// 自分のエントリとして追加する
	private void pullAllUp(final HashMap<String, String> binds, final TypeSlot superType) {
		final HashSet<String> checkExists = new HashSet<>();
		this.access.forEach(acc -> checkExists.add(acc.getRel() + acc.getDescriptor()));
		superType.access.stream().map(acc -> (SlotAccessor) acc).forEach(acc -> {
			if (acc.getType() == Accessor.Kind.LumpSet) {
				// ただしスーパークラスのコンストラクタは除外しとく
				return;
			}
			// 同じエントリがないように name + rel で確認する
			// override したときとか同じエントリが階層上位にあったりするので
			if (checkExists.add(acc.getRel() + acc.getDescriptor())) {
				final SlotAccessor sa = acc.chown(toInternalName(descriptor)).rebind(binds);
				access.add(sa);
			}
		});
	}

	private HashMap<String, String> createBindMap(final TypeSlot superType) {
		final HashMap<String, String> map = new HashMap<>();
		// class Bar<X, Y>
		// -> X, Y
		// に対して
		// class Foo<A> extends Bar<A, String>
		// -> A, String
		// この対応をマップとして作成する
		superType.slotList.forEach(withIndex((superFormalSlot, i) -> {
			// extends 元のクラスで宣言されている type argument を退避
			final Slot typeParamSlot = superSlots.get(0).slotList.get(i);
			// で、それらを比べてなにが型パラメタにくっついたかを調べる
			// それぞれの型パラメタの数とか並び順はコンパイルとおってるかぎり絶対一致してるはずだよ

			// if (thisSlot.descriptor.contentEquals("Ljava/lang/Object;")) {
			if (typeParamSlot.isCertainBound()) {
				// 型パラメタが解決されてるので、そいつをくっつける
				map.put(superFormalSlot.typeParam, typeParamSlot.getTypeDescriptor());
			} else {
				// 型パラメタをリネームする。目印として 'T' をつける
				// 以下より T で始まる型引数はありえないため T を目印にしてるよ
				//// Object -> L
				//// void -> V
				//// primitive -> ZCBSIFJD
				//// array -> [
				map.put(superFormalSlot.typeParam, "T" + typeParamSlot.typeParam);
			}
		}));
		return map;
	}

	private final class Binder implements TypeDef {

		private final HashMap<String, String> formalBindMap;

		private Binder(final List<Slot> binds) {
			if (binds.size() != TypeSlot.this.slotList.size()) {
				// this.slotList -> formal parameter なので binds と必ず長さが一致するはず
				throw new IllegalArgumentException("Type parameters to bind should be match formal-parameters.");
			}
			formalBindMap = new HashMap<>();
			// マップを作るのに binds 側の typeParam はみてないよ
			// 単純に位置で対応表をつくるだけなので、定義順じゃないとずれるよ
			binds.forEach(withIndex((slot, i) -> {
				final Slot formal = TypeSlot.this.slotList.get(i);
				if (!formal.isCertainBound()) {
					formalBindMap.put(formal.typeParam, slot.getTypeDescriptor());
				}
			}));
		}

		@Override
		public boolean hasDefaultCtor() {
			return TypeSlot.this.hasDefaultCtor();
		}

		@Override
		public boolean isList() {
			return TypeSlot.this.isList();
		}

		@Override
		public boolean isMap() {
			if (TypeSlot.this.mapSlot != null) {
				final Slot slot = TypeSlot.this.mapSlot.rebind(formalBindMap);
				return slot.slotList.get(0).descriptor.contentEquals("Ljava/lang/String;");
			}
			return false;
		}

		@Override
		public boolean isCharSequence() {
			return TypeSlot.this.isCharSequence();
		}

		// List の要素スロット
		@Override
		public Slot elementSlot() {
			return TypeSlot.this.elementSlot().rebind(formalBindMap);
		}

		// Map の value スロット
		@Override
		public Slot valueSlot() {
			return TypeSlot.this.valueSlot().rebind(formalBindMap);
		}

		@Override
		public Stream<Accessor> accessors() {
			return TypeSlot.this.accessors().map(acc -> (SlotAccessor) acc).map(acc -> acc.rebind(formalBindMap));
		}

		@Override
		public Slot toSlot() {
			return TypeSlot.this.rebind(formalBindMap);
		}

	}

}
