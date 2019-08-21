package ReIW.tiny.cloneAny.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public final class TypeDef implements TypeAccessDef {

	// name superName は internalName なので注意
	private final String name;
	private final String superName;

	// 自身が signature を持たない場合は null
	final TypeSlot typeSlot;

	final ArrayList<AccessEntry> access = new ArrayList<>();
	final HashSet<String> ctors = new HashSet<>();

	private boolean completed;
	private TypeDef superType; // キャッシュする関係で構築時には初期化できないので complete まで遅延

	TypeDef(final String name, final String superName, final TypeSlot typeSlot) {
		this.name = name;
		this.superName = superName;
		this.typeSlot = typeSlot;
	}

	@Override
	public boolean hasDefaultCtor() {
		return ctors.contains("()V");
	}

	@Override
	public String getInternalName() {
		return name;
	}

	/**
	 * 未解決の型パラメタを指定引数で解決したアクセサのストリームを作るためのラッパをつくる.
	 * 
	 * フィールドとかメソッドの戻りとか引数とかで直接 generic を指定しているタイプのアクセサから引っ張ってきた TypeDef
	 * のアクセサをとる場合はこっち
	 */
	public TypeAccessDef bind(final List<Slot> binds) {
		return new BoundTypeAccessDef(binds);
	}

	@Override
	public Stream<AccessEntry> accessors() {
		// Map#put Map#get をストリームの最後に回すためにいろいろする
		final Stream.Builder<AccessEntry> maps = Stream.builder();
		final Stream.Builder<AccessEntry> prop = Stream.builder();
		access.forEach(acc -> {
			if (acc.name.contentEquals("*")) {
				// マップの get/put の場合
				maps.accept(acc);
			} else {
				prop.accept(acc);
			}
		});
		return Stream.concat(prop.build(), maps.build());
	}

	// complete から再帰的に継承元をたどることで
	// 結果として継承階層上のすべての AccessEntry が含まれる形になる
	void complete() {
		// 何回も呼ばれる可能性もあるのでガードしとく
		if (completed) {
			return;
		}
		// 親が Object.class だったら継承階層のルートまでたどってるので終了する
		if (superName.contentEquals("java/lang/Object")) {
			completed = true;
			return;
		}
		// 親のアクセサを自分に持ってくる
		superType = (TypeDef) TypeDefBuilder.createTypeDef(superName);
		superType.complete();
		pullAllUp();
		completed = true;
	}

	// スーパークラス上で公開されたアクセスエントリを自分の型引数をバインドして
	// 自分のエントリとして追加する
	private void pullAllUp() {
		final Map<String, String> bounds = createBindMap();
		final Set<String> uniq = new HashSet<>();
		for (AccessEntry entry : superType.access) {
			if (entry.elementType == AccessEntry.ACE_CTOR_ARG) {
				// ただしスーパークラスのコンストラクタ引数は除外しとく
				continue;
			}
			// 同じエントリがないように name + rel で確認する
			// override したときとか同じエントリが階層上位にあったりするので
			if (uniq.add(entry.name + entry.rel)) {
				access.add(new AccessEntry(entry.elementType, entry.name, entry.slot.rebind(bounds), entry.rel));
			}
		}
	}

	private Map<String, String> createBindMap() {
		final HashMap<String, String> map = new HashMap<>();
		if (superType.typeSlot != null) {
			for (int i = 0; i < superType.typeSlot.formalSlots.size(); i++) {
				// 継承元のクラス側で定義されてる formal type parameter を退避
				final Slot baseSlot = superType.typeSlot.formalSlots.get(i);
				// 自身の extends で宣言されている type argument を退避
				final Slot thisSlot = typeSlot.superSlot.slotList.get(i);
				// で、それらを比べてなにが型パラメタにくっついたかを調べる
				// それぞれの型パラメタの数とか並び順はコンパイルとおってるかぎり絶対一致してるはずだよ
				if (thisSlot.typeClass == null) {
					// 型パラメタをリマップする。目印として 'T' をつける
					// 以下より T で始まる型引数はありえないため T を目印にしてるよ
					//// Object -> L
					//// void -> V
					//// primitive -> ZCBSIFJD
					//// array -> [
					map.put(baseSlot.typeParam, "T" + thisSlot.typeParam);
				} else {
					map.put(baseSlot.typeParam, thisSlot.typeClass);
				}
			}
		}
		return map;
	}

	/*
	 * 未解決の型パラメタを指定引数で解決したアクセサのストリームを作るためのラッパ
	 */
	private final class BoundTypeAccessDef implements TypeAccessDef {

		private final List<Slot> binds;

		private BoundTypeAccessDef(final List<Slot> binds) {
			this.binds = binds;
		}

		@Override
		public boolean hasDefaultCtor() {
			return TypeDef.this.hasDefaultCtor();
		}

		@Override
		public String getInternalName() {
			return TypeDef.this.getInternalName();
		}

		@Override
		public Stream<AccessEntry> accessors() {
			final HashMap<String, String> bindMap = new HashMap<>();
			for (int i = 0; i < binds.size(); i++) {
				bindMap.put(typeSlot.formalSlots.get(i).typeParam, binds.get(i).typeClass);
			}
			return TypeDef.this.accessors().map(
					(entry) -> new AccessEntry(entry.elementType, entry.name, entry.slot.rebind(bindMap), entry.rel));
		}

	}

}
