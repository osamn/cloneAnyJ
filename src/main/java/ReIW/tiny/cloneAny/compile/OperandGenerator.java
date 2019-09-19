package ReIW.tiny.cloneAny.compile;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.objectweb.asm.Type;

import ReIW.tiny.cloneAny.pojo.Accessor;
import ReIW.tiny.cloneAny.pojo.Accessor.SlotInfo;
import ReIW.tiny.cloneAny.pojo.Slot;
import ReIW.tiny.cloneAny.pojo.TypeDef;

public class OperandGenerator {

	private final TypeDef lhs;
	private final TypeDef rhs;

	// 左のアクセサのパラメタ名をキーにするマップ
	// ここにない場合は lhs の Map#get からとる感じになる
	private final Map<String, Accessor> sourceAccMap;

	private final Accessor effectiveCtor;
	private final List<Accessor> effectiveDst;

	OperandGenerator(final TypeDef lhs, final TypeDef rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
		this.sourceAccMap = lhs.accessors().filter(acc -> acc.canRead())
				.collect(Collectors.toMap(acc -> acc.getName(), acc -> acc));

		final OperandGenerator.MaxArgsAccessor ctorSelector = new OperandGenerator.MaxArgsAccessor();
		this.effectiveDst = this.rhs.accessors().filter(this::selectDstWithEffectiveSrc).filter(acc -> {
			// コンストラクタを別途よせておいて
			if (acc.getType() == Accessor.Type.LumpSet) {
				ctorSelector.set(acc);
				return false;
			}
			return true;
		}).collect(Collectors.toList());
		// 一番引数の長いコンストラクタをとる
		this.effectiveCtor = ctorSelector.get();
	}

	/**
	 * 対応する lhs が存在しかつ rhs に変換可能である rhs を選択する.
	 * 
	 * これにより、以降の処理で lhs の存在や変換可能性を見る必要がなくなるよ
	 */
	private boolean selectDstWithEffectiveSrc(final Accessor acc) {
		if (!acc.canWrite()) {
			return false;
		}
		// 右側のスロット情報すべてについて左側に move 可能なソースがあるか調べる
		return acc.slotInfo().allMatch(inf -> {
			// 左側にパラメタ名前が一致するアクセサがあるかみて
			if (sourceAccMap.containsKey(inf.param)) {
				final SlotInfo single = Accessor.asSingle(sourceAccMap.get(inf.param));
				if (canMove(single.slot, inf.slot)) {
					// スロットが move 可能なのでおｋ
					return true;
				}
			}
			// じゃなければ左側がマップで、その value 型で move 可能か
			if (lhs.isMap()) {
				return canMove(lhs.valueSlot(), inf.slot);
			}
			return false;
		});
	}

	private final HashSet<String> used = new HashSet<>();

	public Stream<Operand> initStream() {
		if (effectiveCtor == null) {
			return null;
		}
		Stream.Builder<Operand> builder = Stream.builder();

		effectiveCtor.slotInfo().forEach(inf -> {
			final String name = inf.param;
			final Accessor src = sourceAccMap.get(name);
			if (src == null) {
				// コンストラクタ引数なんでマップにキーが存在しない場合はすぐエラーに
				// TODO copy Map#get to arg check exists
			} else {
				// TODO copy property to arg
			}
			used.add(name);
		});

		return builder.build();
	}

	public Stream<Operand> copyStream() {
		Stream.Builder<Operand> builder = Stream.builder();

		effectiveDst.forEach(acc -> {
			final String name = acc.getName();
			final Accessor src = sourceAccMap.get(name);
			if (src == null) {
				// TODO copy Map#get(propName) to property if exists
				// 場合によってはコピーしてないのに used に入っちゃうけどいい？
			} else {
				// TODO copy property to property
			}
			used.add(name);
		});

		/* rhs が Map の場合特別扱いで計算する */

		// any -> Map
		if (rhs.isMap()) {
			// any -> Map の場合、ここまでの処理でコピーしていない要素を右マップにコピーする
			final Slot valueSlot = rhs.valueSlot();
			sourceAccMap.entrySet().stream()
					// コピーの対象になっていない左側要素のアクセサをとって
					.filter(entry -> !used.contains(entry.getKey())).map(Map.Entry::getValue)
					// 変換可能なものだけ対象とする
					.filter(acc -> canMove(Accessor.asSingle(acc).slot, valueSlot))
					// copy source property/filed to Map#put(name, val)
					.forEach(acc -> {
						// TODO copy property to Map#put(name, val)
					});
		}

		// Map -> Map
		if (lhs.isMap() && rhs.isMap()) {
			if (canMove(lhs.valueSlot(), rhs.valueSlot())) {
				// TODO key loop copy Map#get(key) to Map#put(key, val) if not exists
				// すでに設定されてる場合はコピーしないようにする感じで
				// ここでは実行時に入るので used による抑止がきかないけどしょうがないよねってことで
			}
		}

		/* list -> list の場合も特別扱い */

		if (lhs.isList() && rhs.isList()) {
			if (canMove(lhs.elementSlot(), rhs.elementSlot())) {
				// TODO index loop copy lhs#get(i) to rhs.set(i)
			}
		}
		return builder.build();
	}

	private void readValue(final Stream.Builder<Operand> builder, final Accessor acc) {

	}

	private void conv(final Stream.Builder<Operand> builder, final Slot lhs, final Slot rhs) {

	}

	private void writeValue(final Stream.Builder<Operand> builder, final Accessor acc) {

	}

	// Accessor として抽出されたスロットの変換可能性をみる
	// Object -> Object の場合はここで true になっても右側のコンストラクタセッタあたりで
	// うまく転記できなかったら runtime 時に例外になったりするよ
	private static boolean canMove(final Slot lhs, final Slot rhs) {
		if (lhs.getTypeDescriptor().contentEquals(rhs.getTypeDescriptor())) {
			// 型パラメタを除いて一緒なんでとりあえず true
			return true;
		}

		/* array が絡む場合は Slot#descriptor が型を示さないので先に処理する */

		// array -> array のチェック
		if (lhs.isArrayType && rhs.isArrayType) {
			return canMove(lhs.slotList.get(0), rhs.slotList.get(0));
		}

		// array -> List のチェック
		if (lhs.isArrayType) {
			final TypeDef rhsDef = TypeDef.createInstance(rhs);
			// 右側が List の場合 array と同等に生成できないといかんのでデフォルトコンストラクタが必要
			if (rhsDef.isList() && rhsDef.hasDefaultCtor()) {
				return canMove(lhs.slotList.get(0), rhsDef.elementSlot());
			}
			return false;
		}

		// List -> array のチェック
		if (rhs.isArrayType) {
			final TypeDef lhsDef = TypeDef.createInstance(lhs);
			if (lhsDef.isList()) {
				return canMove(lhsDef.elementSlot(), rhs.slotList.get(0));
			}
			return false;
		}

		/* array じゃない場合は getTypeDescriptor じゃなくて descriptor を直に扱えるよ */

		// キャスト操作のチェック
		if (lhs.isPrimitiveType && rhs.isPrimitiveType) {
			// boolean はキャストでほかの primitive に持っていけないので特別扱い
			// 片方だけ真偽値は NG
			if (lhs.descriptor.contentEquals("Z") && rhs.descriptor.contentEquals("Z")) {
				return true;
			} else if (lhs.descriptor.contentEquals("Z") || rhs.descriptor.contentEquals("Z")) {
				return false;
			}
			// それ以外はキャスト可能
			return true;
		}

		// Box 操作のチェック
		if (lhs.isPrimitiveType && rhs.isBoxingType) {
			if (lhs.descriptor.contentEquals("Z") && rhs.descriptor.contentEquals("Ljava/lang/Boolean;")) {
				return true;
			} else if (lhs.descriptor.contentEquals("Z") || rhs.descriptor.contentEquals("Ljava/lang/Boolean;")) {
				return false;
			}
			return true;
		}

		// Unbox 操作のチェック
		if (lhs.isBoxingType && rhs.isPrimitiveType) {
			if (lhs.descriptor.contentEquals("Ljava/lang/Boolean;") && rhs.descriptor.contentEquals("Z")) {
				return true;
			} else if (lhs.descriptor.contentEquals("Ljava/lang/Boolean;") || rhs.descriptor.contentEquals("Z")) {
				return false;
			}
			return true;
		}

		// * -> String のチェック
		if (rhs.descriptor.contentEquals("Ljava/lang/String;")) {
			if (lhs.isPrimitiveType || lhs.isBoxingType) {
				return true;
			}
			final TypeDef lhsDef = TypeDef.createInstance(lhs);
			if (lhsDef.isCharSequence()) {
				return true;
			}
			return false;
		}

		// CharSequence -> primitive/boxing のチェック
		if (rhs.isPrimitiveType || rhs.isBoxingType) {
			final TypeDef lhsDef = TypeDef.createInstance(lhs);
			if (lhsDef.isCharSequence()) {
				return true;
			}
			return false;
		}

		// あとは Operand 作るところで頑張る
		return true;
	}

	private static class MaxArgsAccessor {
		private Accessor curr;

		void set(final Accessor acc) {
			if (curr == null) {
				curr = acc;
				return;
			}
			final int curSize = Type.getArgumentsAndReturnSizes(curr.getDescriptor()) >> 2;
			final int accSize = Type.getArgumentsAndReturnSizes(acc.getDescriptor()) >> 2;
			if (curSize == accSize) {
				throw new IllegalArgumentException("Anbiguous constructor found.");
			}
			if (curSize < accSize) {
				curr = acc;
			}
		}

		Accessor get() {
			return curr;
		}
	}

}
