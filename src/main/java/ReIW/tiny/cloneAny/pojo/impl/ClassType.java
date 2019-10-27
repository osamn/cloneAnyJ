package ReIW.tiny.cloneAny.pojo.impl;

import static ReIW.tiny.cloneAny.utils.Consumers.withIndex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.objectweb.asm.Type;

import ReIW.tiny.cloneAny.pojo.AccessDef;
import ReIW.tiny.cloneAny.pojo.Accessor;
import ReIW.tiny.cloneAny.pojo.Accessor.AccessType;
import ReIW.tiny.cloneAny.pojo.Accessor.FieldAccess;
import ReIW.tiny.cloneAny.pojo.Accessor.IndexedAccess;
import ReIW.tiny.cloneAny.pojo.Accessor.KeyedAccess;
import ReIW.tiny.cloneAny.pojo.Accessor.LumpSetAccess;
import ReIW.tiny.cloneAny.pojo.Accessor.PropAccess;
import ReIW.tiny.cloneAny.pojo.Slot;

public final class ClassType implements AccessDef {

	@Override
	public String getInternalName() {
		return Type.getType(thisSlot.getDescriptor()).getInternalName();
	}

	@Override
	public Stream<Accessor> accessors() {
		return this.accessors.stream();
	}

	public AccessDef bind(final List<Slot> boundSlots) {
		final List<SlotValue> lhs = boundSlots.stream().map(SlotValue.class::cast)
				.collect(Collectors.toUnmodifiableList());
		final Map<String, String> binds = createBindMap(lhs, thisSlot.slotList);
		return new Binder(binds);
	}

	// bind するときに型パラメタの情報で使うよ
	final SlotValue thisSlot;

	// extends したスロット
	// 終端は Object になるよ
	SlotValue superSlot;

	// 継承階層上にあるすべてのクラス、インターフェースの descriptor を保持するセット
	final Set<String> ancestors = new HashSet<>();

	// このクラスからアクセスできるすべてのアクセサのリスト
	final List<Accessor> accessors = new ArrayList<>();

	private boolean completed;

	ClassType(final SlotValue slot) {
		thisSlot = slot;
	}

	void complete() {
		if (completed) {
			return;
		}
		completed = true;
		if (thisSlot.arrayType) {
			// 配列の場合はなにもしない
			return;
		}

		/* 以下配列はありえないので getDescriptor じゃなく descriptor でおｋ*/

		final String superDesc = superSlot.descriptor;
		if (superDesc.contentEquals("Ljava/lang/Object;")) {
			// 継承階層のルートまでたどってるので終了する
			// なんで Object#getClass はアクセサにふくまれないよ
			return;
		}

		// 親の ClassType を作って
		final ClassType superType = new ClassTypeBuilder().buildClassType(superDesc);
		superType.complete();

		// 親のアクセサをバインドして自分に持ってくる
		pullAllUp(superType);
	}

	// スーパークラス上で公開されたアクセスエントリ、および継承階層のすべてのクラス、インターフェースを
	// 自分の型引数をバインドして自分のエントリとして追加する
	private void pullAllUp(final ClassType superType) {
		// バインド情報を作成する
		final Map<String, String> binds = createBindMap(superSlot.slotList, superType.thisSlot.slotList);

		// 同じエントリがないように type + name で確認するための Set 用のキーをつくるひと
		// setter の場合は overload があるので descriptor もキーに含める
		final Function<Accessor, String> accKey = acc -> acc.getType().name() + ":" + acc.getName() + ":"
				+ ((acc.getType() == Accessor.AccessType.Set) ? ((PropAccess) acc).slot.getDescriptor() : "");

		// override したときとか同じエントリが階層上位にあったりするので
		// セットに accKey で作った識別子いれて重複しないようにしておく
		final Set<String> declaredMember = this.accessors.stream().map(accKey).collect(Collectors.toSet());
		
		// superSlots の implements 相当の型パラメタは、自身の型パラメタとしてアクセサに展開済みなので
		// extends 相当の supers.get(0) のみで rebind すればいいよ
		superType.accessors.stream()
				// スーパークラスのコンストラクタはのぞいて
				.filter(acc -> acc.getType() != AccessType.LumpSet)
				// アクセサのスロットの型パラメタをバインドして
				// バインド先にしないと次のフィルタで除外しきれないので注意！
				.map(rebindTypeParam(binds))
				// 重複しないように set で確認してフィルタする
				.filter(acc -> declaredMember.add(accKey.apply(acc)))
				// 自身のアクセサとして追加する
				.forEach(this.accessors::add);

		// 継承階層をマージしておく
		ancestors.addAll(superType.ancestors);
	}

	private static Function<Accessor, Accessor> rebindTypeParam(final Map<String, String> binds) {
		return acc -> {
			switch (acc.getType()) {
			case Field:
			case ReadonlyField:
				final FieldAccess field = (FieldAccess) acc;
				return new FieldAccess(field.getType(), field.getOwner(), field.getName(),
						SlotValue.of(field.slot).rebind(binds));
			case Get:
			case Set:
				final PropAccess prop = (PropAccess) acc;
				return new PropAccess(prop.getType(), prop.getOwner(), prop.getName(), prop.rel, prop.methodDescriptor,
						SlotValue.of(prop.slot).rebind(binds));
			case ArrayType:
			case ListType:
				final IndexedAccess indexed = (IndexedAccess) acc;
				return new IndexedAccess(indexed.getType(), indexed.getOwner(),
						SlotValue.of(indexed.elementSlot).rebind(binds));
			case MapType:
				final KeyedAccess keyed = (KeyedAccess) acc;
				return new KeyedAccess(keyed.getType(), keyed.getOwner(), SlotValue.of(keyed.keySlot).rebind(binds),
						SlotValue.of(keyed.valueSlot).rebind(binds));
			case LumpSet:
				// fall through
				break;
			}
			// LumpSet
			assert acc.getType() == AccessType.LumpSet;
			final LumpSetAccess lump = (LumpSetAccess) acc;
			final LumpSetAccess dstLump = new LumpSetAccess(lump.getOwner(), lump.rel, lump.methodDescriptor);
			lump.parameters.forEach((key, val) -> dstLump.parameters.put(key, SlotValue.of(val).rebind(binds)));
			return dstLump;
		};
	}

	private static Map<String, String> createBindMap(
			// 型パラメタの実際にバインドされてるスロット
			final List<SlotValue> lhs,
			// 宣言してる側の型パラメタのスロット */
			final List<SlotValue> rhs) {
		final HashMap<String, String> map = new HashMap<>();
		// class Bar<X, Y>
		// rhs: ==> declaredSlot[X, Y]
		// に対して
		// class Foo<A> extends Bar<A, String>
		// lhs ==> actualSlot[A, String]
		// の以下のマップをつくる
		// -> X, TA
		// -> Y, String
		// この対応をマップとして作成する
		rhs.forEach(withIndex((declaredSlot/* 親のフォーマルスロット */, i) -> {
			// extends に定義された型引数をとってきて
			final SlotValue actualSlot = lhs.get(i);
			// で、それらを比べてなにが型パラメタにくっついたかを調べる
			// それぞれの型パラメタの数とか並び順はコンパイルとおってるかぎり絶対一致してるはずだよ

			if (actualSlot.isCertainBound()) {
				// 型パラメタが解決されてるので、そいつをくっつける
				// X -> List<String> みたいのもあるので signature ベースで名前作る
				map.put(declaredSlot.typeParam, actualSlot.getSignature());
			} else {
				// 型パラメタをリネームする。目印として 'T' をつける
				// 以下より T で始まる型引数はありえないため T を目印にしてるよ
				//// Object -> L
				//// void -> V
				//// primitive -> ZCBSIFJD
				//// array -> [
				map.put(declaredSlot.typeParam, "T" + actualSlot.typeParam);
			}
		}));
		return map;
	}

	// FIXME みてい
	class Binder implements AccessDef {

		private final Map<String, String> binds;

		Binder(Map<String, String> binds) {
			this.binds = binds;
		}

		@Override
		public String getInternalName() {
			return ClassType.this.getInternalName();
		}

		@Override
		public Stream<Accessor> accessors() {
			return ClassType.this.accessors().map(rebindTypeParam(binds));
		}

	}

}
