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

	private Slot listSlot;

	private Slot mapSlot;

	private boolean charSequence;

	private boolean number;

	// -- builder が設定するものは package スコープ

	final List<Slot> superSlots = new ArrayList<>();

	final List<Accessor> access = new ArrayList<>();

	boolean defaultCtor;

	// 階層含めて完了したか？
	private boolean completed;

	TypeSlot(final String typeParam, final String descriptor) {
		this(typeParam, descriptor, false);
	}

	private TypeSlot(final String typeParam, final String descriptor, final boolean completed) {
		super(typeParam, descriptor);
		this.completed = completed;
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
	public boolean isNumber() {
		return number;
	}

	@Override
	public Slot elementSlot() {
		if (isArrayType) {
			return slotList.get(0);
		}
		return listSlot.slotList.get(0);
	}

	@Override
	public boolean isDecendantOf(final String descriptor) {
		return superSlots.stream().anyMatch(slot -> slot.descriptor.contentEquals(descriptor));
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

		// 親がなかったら継承階層のルートまでたどってるので終了する
		// 継承元が配列はありえないので array を考慮する必要ない
		// なんで getTypeDescriptor じゃなくておｋ
		if (superSlots.size() == 0) {
			return;
		}

		// Object.class も処理するからね

		final String superDesc = superSlots.get(0).descriptor;
		// 親の TypeSlot を作って
		final TypeSlot superType = new TypeSlotBuilder().buildTypeSlot(superDesc);
		superType.complete();
		final HashMap<String, String> binds = createBindMap(superType);
		// 親のアクセサとか継承情報を自分に持ってくる
		pullAllUp(binds, superType);
		// クラス階層からじゃないと取れないものを設定する
		setupHierachicalOptions();
	}

	// スーパークラス上で公開されたアクセスエントリを自分の型引数をバインドして
	// 自分のエントリとして追加する
	private void pullAllUp(final HashMap<String, String> binds, final TypeSlot superType) {
		final HashSet<String> checkExists = new HashSet<>();
		this.access.forEach(acc -> checkExists.add(acc.getRel() + acc.getDescriptor()));
		superType.access.forEach(acc -> {
			if (acc.getType() == Accessor.Kind.LumpSet) {
				// ただしスーパークラスのコンストラクタは除外しとく
				return;
			}
			// 同じエントリがないように name + rel で確認する
			// override したときとか同じエントリが階層上位にあったりするので
			if (checkExists.add(acc.getRel() + acc.getDescriptor())) {
				final SlotAccessor slotAcc = (SlotAccessor) acc;
				access.add(slotAcc.chown(toInternalName(descriptor)).rebind(binds));
			}
		});
		// 親階層の継承クラス、実装インターフェースをすべてマージする
		superType.superSlots.forEach(slot -> {
			this.superSlots.add(slot.rebind(binds));
		});
	}

	private void setupHierachicalOptions() {
		this.superSlots.forEach(slot -> {
			if (slot.descriptor.contentEquals("Ljava/util/List;")) {
				this.listSlot = slot;
			} else if (slot.descriptor.contentEquals("Ljava/util/Map;")) {
				this.mapSlot = slot;
			} else if (slot.descriptor.contentEquals("Ljava/lang/CharSequence;")) {
				this.charSequence = true;
			} else if (slot.descriptor.contentEquals("Ljava/lang/Number;")) {
				// ほとんどくることはないとおもう
				// BidDecimal とか AtomicInteger とかそんなやつ
				this.number = true;
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

		@Override
		public boolean isNumber() {
			return TypeSlot.this.isNumber();
		}

		@Override
		public boolean isDecendantOf(String descriptor) {
			return TypeSlot.this.isDecendantOf(descriptor);
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

	static final HashMap<String, TypeSlot> systemTypes = new HashMap<>();

	static {
		/* java.lang 配下とか、とくに Builder 経由じゃなくていいものを complete 済みであらかじめ定義しとく */
		systemTypes.put("Ljava/lang/Object;", new TypeSlot(null, "Ljava/lang/Object;", true));

		systemTypes.put("Z", new TypeSlot(null, "Z", true));
		systemTypes.put("B", new TypeSlot(null, "B", true));
		systemTypes.put("C", new TypeSlot(null, "C", true));
		systemTypes.put("D", new TypeSlot(null, "D", true));
		systemTypes.put("F", new TypeSlot(null, "F", true));
		systemTypes.put("I", new TypeSlot(null, "I", true));
		systemTypes.put("J", new TypeSlot(null, "J", true));
		systemTypes.put("S", new TypeSlot(null, "S", true));

		final TypeSlot integerType = new TypeSlot(null, "Ljava/lang/Integer;", true);
		integerType.number = true;
		systemTypes.put("Ljava/lang/Integer;", integerType);

		final TypeSlot byteType = new TypeSlot(null, "Ljava/lang/Byte;", true);
		byteType.number = true;
		systemTypes.put("Ljava/lang/Byte;", byteType);

		final TypeSlot doubleType = new TypeSlot(null, "Ljava/lang/Double;", true);
		doubleType.number = true;
		systemTypes.put("Ljava/lang/Double;", doubleType);

		final TypeSlot floatType = new TypeSlot(null, "Ljava/lang/Float;", true);
		floatType.number = true;
		systemTypes.put("Ljava/lang/Float;", floatType);

		final TypeSlot longType = new TypeSlot(null, "Ljava/lang/Long;", true);
		longType.number = true;
		systemTypes.put("Ljava/lang/Long;", longType);

		final TypeSlot shortType = new TypeSlot(null, "Ljava/lang/Short;", true);
		shortType.number = true;
		systemTypes.put("Ljava/lang/Short;", shortType);

		final TypeSlot stringType = new TypeSlot(null, "Ljava/lang/String;", true);
		stringType.charSequence = true;
		systemTypes.put("Ljava/lang/String;", stringType);

		systemTypes.put("Ljava/lang/Boolean;", new TypeSlot(null, "Ljava/lang/Boolean;", true));
		systemTypes.put("Ljava/lang/Character;", new TypeSlot(null, "Ljava/lang/Character;", true));
	}

}
