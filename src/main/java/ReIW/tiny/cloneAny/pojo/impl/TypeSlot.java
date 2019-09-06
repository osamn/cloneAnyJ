package ReIW.tiny.cloneAny.pojo.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import org.objectweb.asm.Type;

import ReIW.tiny.cloneAny.pojo.Accessor;
import ReIW.tiny.cloneAny.pojo.Slot;
import ReIW.tiny.cloneAny.pojo.TypeAccessDef;
import ReIW.tiny.cloneAny.utils.Descriptors;

import static ReIW.tiny.cloneAny.utils.Consumers.withIndex;

public class TypeSlot extends Slot implements TypeAccessDef {

	final ArrayList<Slot> superSlots = new ArrayList<>();

	final ArrayList<Accessor> access = new ArrayList<>();

	private boolean completed;

	TypeSlot(final Class<?> clazz) {
		super(clazz);
	}

	@Override
	public String getName() {
		return Type.getType(this.getClassDescriptor()).getInternalName();
	}

	@Override
	public Stream<Accessor> accessors() {
		return access.stream();
	}

	/**
	 * 未解決の型パラメタを指定引数で解決したアクセサのストリームを作るためのラッパをつくる.
	 * 
	 * フィールドとかメソッドの戻りとか引数とかで直接 generic を指定しているタイプのアクセサから引っ張ってきた TypeDef
	 * のアクセサをとる場合はこっち
	 */
	public TypeAccessDef bind(final List<Slot> binds) {
		return new BoundTypeDef(binds);
	}

	// complete から再帰的に継承元をたどることで
	// 結果として継承階層上のすべての AccessEntry が含まれる形になる
	void complete() {
		// 何回も呼ばれる可能性もあるのでガードしとく
		if (completed) {
			return;
		}
		completed = true;

		// 親が Object.class だったら継承階層のルートまでたどってるので終了する
		final String superDesc = superSlots.get(0).getClassDescriptor();
		if (superDesc.contentEquals("Ljava/lang/Object;")) {
			return;
		}
		// 親の TypeSlot を作って
		final TypeSlot superType = TypeSlotBuilder.createTypeSlot(Descriptors.toClass(superDesc));
		superType.complete();
		// 親のアクセサを自分に持ってくる
		pullAllUp(superType);
	}

	// スーパークラス上で公開されたアクセスエントリを自分の型引数をバインドして
	// 自分のエントリとして追加する
	private void pullAllUp(final TypeSlot superType) {
		final HashMap<String, String> binds = createBindMap(superType);
		final HashSet<String> checkExists = new HashSet<>();
		this.access.forEach(acc -> checkExists.add(acc.getRel() + acc.getDescriptor()));
		superType.access.stream().map(acc -> (SlotAccessor) acc).forEach(acc -> {
			if (acc.getType() == Accessor.Type.LumpSet) {
				// ただしスーパークラスのコンストラクタは除外しとく
				return;
			}
			// 同じエントリがないように name + rel で確認する
			// override したときとか同じエントリが階層上位にあったりするので
			if (checkExists.add(acc.getRel() + acc.getDescriptor())) {
				final SlotAccessor sa = acc.chown(getName()).rebind(binds);
				access.add(sa);
			}
		});
	}

	private HashMap<String, String> createBindMap(final TypeSlot superSlot) {
		final HashMap<String, String> map = new HashMap<>();
		// 継承元のクラス側で定義されてる formal type parameter を基にして
		superSlot.slotList.forEach(withIndex((baseSlot, i) -> {
			// 自身の extends で宣言されている type argument を退避
			final Slot thisSlot = superSlots.get(0).slotList.get(i);
			// で、それらを比べてなにが型パラメタにくっついたかを調べる
			// それぞれの型パラメタの数とか並び順はコンパイルとおってるかぎり絶対一致してるはずだよ

			//if (thisSlot.descriptor.contentEquals("Ljava/lang/Object;")) {
			if (!thisSlot.isCertainBound()) {
				// 型パラメタをリマップする。目印として 'T' をつける
				// 以下より T で始まる型引数はありえないため T を目印にしてるよ
				//// Object -> L
				//// void -> V
				//// primitive -> ZCBSIFJD
				//// array -> [
				map.put(baseSlot.typeParam, "T" + thisSlot.typeParam);
			} else {
				map.put(baseSlot.typeParam, thisSlot.getClassDescriptor());
			}
		}));
		return map;
	}

	private final class BoundTypeDef implements TypeAccessDef {

		private final List<Slot> binds;

		private BoundTypeDef(final List<Slot> binds) {
			this.binds = binds;
		}

		@Override
		public String getName() {
			return TypeSlot.this.getName();
		}

		@Override
		public Stream<Accessor> accessors() {
			final HashMap<String, String> bindMap = new HashMap<>();
			binds.forEach(withIndex((slot, i) -> {
				bindMap.put(TypeSlot.this.slotList.get(i).typeParam, slot.getClassDescriptor());
			}));
			return TypeSlot.this.accessors().map(acc -> (SlotAccessor) acc).map(acc -> acc.rebind(bindMap));
		}

	}

}
