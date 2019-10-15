package ReIW.tiny.cloneAny.pojo.impl;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.objectweb.asm.Type;

import ReIW.tiny.cloneAny.pojo.Accessor;
import ReIW.tiny.cloneAny.pojo.Accessor.AccessType;
import ReIW.tiny.cloneAny.pojo.Accessor.FieldAccess;
import ReIW.tiny.cloneAny.pojo.Accessor.IndexedAccess;
import ReIW.tiny.cloneAny.pojo.Accessor.KeyedAccess;
import ReIW.tiny.cloneAny.pojo.Accessor.PropAccess;

final class ClassType {

	// complete するまでの一時的なもの
	// complete あとは null になるので注意
	ArrayList<SlotValue> supers = new ArrayList<>();

	// instance of を見るために用意しとく
    // ancestors.contains(...) で instanceOf を調べるよ
	Set<String> ancestors;

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

		// TODO Object までたどったらだとなんでだめだったんだっけ？
		// 継承階層のルートまでたどってるので終了する
		if (supers.size() == 0) {
			return;
		}

		final String superDesc = supers.get(0).descriptor;
		// 親の ClassType を作って
		final ClassType superType = new ClassTypeBuilder().buildClassType(superDesc);
		superType.complete();

		// 親のアクセサをバインドして自分に持ってくる
		final Map<String, String> binds = ClassTypeBuilder.createBindMap(supers.get(0), superType.thisSlot);
		pullAllUp(binds, superType);

		// 継承階層の descriptor を収集する
		// ここには配列はでてこないよ、継承関係だから
		// あと、instanceOf 相当なんで generic もみないよ
		ancestors = Stream.concat(supers.stream().map(s -> s.descriptor), superType.ancestors.stream())
				.collect(Collectors.toUnmodifiableSet());

		// もう使わんので開放しとく
		supers = null;
	}

	// スーパークラス上で公開されたアクセスエントリを自分の型引数をバインドして
	// 自分のエントリとして追加する
	private void pullAllUp(final Map<String, String> binds, final ClassType superType) {
		final Function<Accessor, String> accKey = acc -> acc.getType().name() + ":" + acc.getName();

		// 同じエントリがないように type + name で確認するための Set をつくっておく
		// override したときとか同じエントリが階層上位にあったりするので
		final Set<String> memberOfThis = this.accessors.stream().map(accKey).collect(Collectors.toSet());

		// superSlots の implements 相当の型パラメタは、自身の型パラメタとしてアクセサに展開済みなので
		// extends 相当の supers.get(0) のみで rebind すればいいよ
		final String ownerName = Type.getType(thisSlot.descriptor).getInternalName();
		superType.accessors.forEach(acc -> {
			if (acc.getType() == AccessType.LumpSet) {
				// ただしスーパークラスのコンストラクタは除外しとく
				return;
			}
			if (memberOfThis.add(accKey.apply(acc))) {
				// set に追加された -> 同じものがなかったのでアクセサを追加するよ
				switch (acc.getType()) {
				case Field:
				case ReadonlyField:
					final FieldAccess field = (FieldAccess) acc;
					this.accessors.add(new FieldAccess(field.getType(), ownerName, field.getName(),
							((SlotValue) field.slot).rebind(binds)));
					break;
				case Get:
				case Set:
					final PropAccess prop = (PropAccess) acc;
					this.accessors.add(new PropAccess(prop.getType(), ownerName, prop.getName(), prop.rel,
							prop.methodDescriptor, ((SlotValue) prop.slot).rebind(binds)));
					break;
				case ArrayGet:
				case ArraySet:
				case ListGet:
				case ListAdd:
					final IndexedAccess indexed = (IndexedAccess) acc;
					this.accessors
							.add(new IndexedAccess(indexed.getType(), ((SlotValue) indexed.elementSlot).rebind(binds)));
					break;
				case MapGet:
				case MapPut:
					final KeyedAccess keyed = (KeyedAccess) acc;
					this.accessors.add(new KeyedAccess(keyed.getType(), ((SlotValue) keyed.keySlot).rebind(binds),
							((SlotValue) keyed.valueSlot).rebind(binds)));
					break;
				case LumpSet:
					// これは来るはずないんだよ
					break;
				}
			}
		});
	}

}
