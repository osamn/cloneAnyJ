package ReIW.tiny.cloneAny.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

final class TypeDef {

	final String name;
	final String superName;

	final TypeSlot typeSlot;
	final ArrayList<AccessEntry> access = new ArrayList<AccessEntry>();

	private boolean completed;
	private TypeDef superType; // キャッシュする関係で構築時には初期化できないので complete まで遅延

	TypeDef(final String name, final String superName, final TypeSlot typeSlot) {
		this.name = name;
		this.superName = superName;
		this.typeSlot = typeSlot;
	}

	Stream<AccessEntry> accessors() {
		return access.stream();
	}

	/**
	 * 未解決の型パラメタを指定引数で解決したアクセサのストリームを作る.
	 * 
	 * フィールドとかメソッドの戻りとか引数とかで直接 generic を指定しているタイプのアクセサから引っ張ってきた TypeDef
	 * のアクセサをとる場合はこっち
	 * 
	 * @param binds
	 * @return
	 */
	Stream<AccessEntry> accessors(final List<Slot> binds) {
		final HashMap<String, String> bounds = new HashMap<>();
		for (int i = 0; i < binds.size(); i++) {
			bounds.put(typeSlot.formalSlots.get(i).typeParam, binds.get(i).typeClass);
		}

		final Stream.Builder<AccessEntry> builder = Stream.builder();
		for (AccessEntry entry : superType.access) {
			if (entry.elementType == AccessEntry.ACE_CTOR_ARG) {
				// ただしスーパークラスのコンストラクタ引数は除外しとく
				continue;
			}
			builder.add(new AccessEntry(entry.elementType, entry.name, entry.slot.rebind(bounds), entry.rel));
		}
		return builder.build();
	}

	// complete から再帰的に継承元をたどることで
	// 結果として継承階層上のすべての AccessEntry が含まれる形になる
	void complete() {
		if (completed) {
			return;
		}
		superType = TypeDefBuilder.createTypeDef(superName);
		if (superType == null) {
			return;
		}
		superType.complete();
		pullAllUp();
		completed = true;
	}

	// スーパークラス上で公開されたアクセスエントリを自分の型引数をバインドして
	// 自分のエントリとして追加する
	private void pullAllUp() {
		final Map<String, String> bounds = createBindMap();
		for (AccessEntry entry : superType.access) {
			if (entry.elementType == AccessEntry.ACE_CTOR_ARG) {
				// ただしスーパークラスのコンストラクタ引数は除外しとく
				continue;
			}
			access.add(new AccessEntry(entry.elementType, entry.name, entry.slot.rebind(bounds), entry.rel));
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
}
