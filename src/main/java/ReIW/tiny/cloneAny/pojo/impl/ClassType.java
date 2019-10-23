package ReIW.tiny.cloneAny.pojo.impl;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.objectweb.asm.Type;

import ReIW.tiny.cloneAny.pojo.Accessor;
import ReIW.tiny.cloneAny.pojo.Accessor.AccessType;
import ReIW.tiny.cloneAny.pojo.Accessor.FieldAccess;
import ReIW.tiny.cloneAny.pojo.Accessor.IndexedAccess;
import ReIW.tiny.cloneAny.pojo.Accessor.KeyedAccess;
import ReIW.tiny.cloneAny.pojo.Accessor.LumpSetAccess;
import ReIW.tiny.cloneAny.pojo.Accessor.PropAccess;

final class ClassType {

	// FIXME binder 追加しないといかんけど AccessDef とかそんなインターフェースきめてから
	// map は slotList(lhs) をもらって thisSlot.slotList(rhs) とあわせてマップつくる
	// null, =, +, - 以外のものをエントリとしてついかする感じで

	ArrayList<SlotValue> superSlots = new ArrayList<>();

	// bind するときに型パラメタの情報で使うよ
	SlotValue thisSlot;

	// これがつくりたいだけ
	final ArrayList<Accessor> accessors = new ArrayList<>();

	private boolean completed;

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

		final String superDesc = superSlots.get(0).descriptor;
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
		final Map<String, String> binds = ClassTypeBuilder.createBindMap(superSlots.get(0), superType.thisSlot);

		// 同じエントリがないように type + name で確認するための Set 用のキーをつくるひと
		// getter の場合は overload があるので descriptor もキーに含める
		final Function<Accessor, String> accKey = acc -> acc.getType().name() + ":" + acc.getName()
				+ (acc.getType() == Accessor.AccessType.Get ? ((PropAccess) acc).methodDescriptor : "");

		// override したときとか同じエントリが階層上位にあったりするので
		// これで重複して登録されないようにしておく
		final Set<String> declaredMember = this.accessors.stream().map(accKey).collect(Collectors.toSet());

		// superSlots の implements 相当の型パラメタは、自身の型パラメタとしてアクセサに展開済みなので
		// extends 相当の supers.get(0) のみで rebind すればいいよ
		final String ownerName = Type.getType(thisSlot.descriptor).getInternalName();
		superType.accessors.stream()
				// スーパークラスのコンストラクタはのぞいて
				.filter(acc -> acc.getType() != AccessType.LumpSet)
				// スパークラスのインスタンスのメンバのうちで
				// すでに同じものが自クラスのメンバとして追加されてなかったらおｋ
				.filter(acc -> declaredMember.add(accKey.apply(acc)))
				// アクセサの owner を変更して
				.map(chown(ownerName))
				// アクセサのスロットの型パラメタをバインドして
				.map(rebindTypeParam(binds))
				// 自身のアクセサとして追加する
				.forEach(this.accessors::add);

		// 親から superSlots をマージする
		// これにより継承階層上のすべてのクラス、インターフェースをバインドした状態で保持する形になるよ
		superType.superSlots.stream()
				// Object はのぞいておく
				.filter(slot -> !slot.descriptor.contentEquals("Ljava/lang/Object;"))
				// 型パラメタをバインドして
				.map(slot -> slot.rebind(binds))
				// 継承階層の要素として追加しておく
				.forEach(this.superSlots::add);
	}

	private static Function<Accessor, Accessor> chown(final String ownerName) {
		return acc -> {
			switch (acc.getType()) {
			case Field:
			case ReadonlyField:
				final FieldAccess field = (FieldAccess) acc;
				return new FieldAccess(acc.getType(), ownerName, acc.getName(), field.slot);
			case Get:
			case Set:
				final PropAccess prop = (PropAccess) acc;
				return new PropAccess(acc.getType(), ownerName, acc.getName(), prop.rel, prop.methodDescriptor,
						prop.slot);
			case ArrayGet:
			case ArraySet:
			case ListGet:
			case ListAdd:
			case MapGet:
			case MapPut:
				// owner もってないのでそのまま返す
				return acc;
			case LumpSet:
				// fall through
				// ここに return かくとラムダが戻り値返してないって叱られるよ
				break;
			}
			// LumpSet
			assert acc.getType() == AccessType.LumpSet;
			final LumpSetAccess lump = (LumpSetAccess) acc;
			final LumpSetAccess newLump = new LumpSetAccess(ownerName, lump.rel, lump.methodDescriptor);
			lump.slotInfo.forEach((key, val) -> newLump.slotInfo.put(key, val));
			return newLump;
		};

	}

	private static Function<Accessor, Accessor> rebindTypeParam(final Map<String, String> binds) {
		return acc -> {
			switch (acc.getType()) {
			case Field:
			case ReadonlyField:
				final FieldAccess field = (FieldAccess) acc;
				return new FieldAccess(acc.getType(), acc.getOwner(), acc.getName(),
						((SlotValue) field.slot).rebind(binds));
			case Get:
			case Set:
				final PropAccess prop = (PropAccess) acc;
				return new PropAccess(acc.getType(), acc.getOwner(), acc.getName(), prop.rel, prop.methodDescriptor,
						((SlotValue) prop.slot).rebind(binds));
			case ArrayGet:
			case ArraySet:
			case ListGet:
			case ListAdd:
				final IndexedAccess indexed = (IndexedAccess) acc;
				return new IndexedAccess(acc.getType(), ((SlotValue) indexed.elementSlot).rebind(binds));
			case MapGet:
			case MapPut:
				final KeyedAccess keyed = (KeyedAccess) acc;
				return new KeyedAccess(acc.getType(), ((SlotValue) keyed.keySlot).rebind(binds),
						((SlotValue) keyed.valueSlot).rebind(binds));
			case LumpSet:
				// fall through
				break;
			}
			// LumpSet
			assert acc.getType() == AccessType.LumpSet;
			final LumpSetAccess lump = (LumpSetAccess) acc;
			final LumpSetAccess dstLump = new LumpSetAccess(acc.getOwner(), lump.rel, lump.methodDescriptor);
			lump.slotInfo.forEach((key, val) -> dstLump.slotInfo.put(key, ((SlotValue) val).rebind(binds)));
			return lump;
		};
	}

}
