package ReIW.tiny.cloneAny.pojo.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ReIW.tiny.cloneAny.pojo.Slot;
import ReIW.tiny.cloneAny.utils.Descriptors;

class SlotValue implements Slot {

	final String typeParam;
	final String descriptor;

	final boolean arrayType;
	final boolean primitiveType;
	final boolean boxingType;

	final List<SlotValue> slotList = new ArrayList<>();

	SlotValue(final String typeParam, final String descriptor) {
		this.typeParam = typeParam;
		this.descriptor = descriptor;
		arrayType = descriptor.contentEquals("[");
		primitiveType = arrayType ? false : Descriptors.isPrimitiveType(descriptor);
		boxingType = arrayType ? false : Descriptors.isBoxingType(descriptor);
	}

	@Override
	public String getDescriptor() {
		return descriptor;
	}

	@Override
	public List<Slot> descendants() {
		return slotList.stream().map(Slot.class::cast).collect(Collectors.toUnmodifiableList());
	}

	/** 配列も考慮した descriptor */
	// 型パラメタはふくまない単純な descriptor
	// なんだけど、rebind とかされて決定する場合もあるので実行時に作成する
	String getTypeDescriptor() {
		if (arrayType) {
			return descriptor + slotList.get(0).getTypeDescriptor();
		}
		return descriptor;
	}

	boolean isCertainBound() {
		final boolean bound = typeParam == null || typeParam.contentEquals("=") || typeParam.contentEquals("+")
				|| typeParam.contentEquals("-");
		return bound && (slotList == null || slotList.stream().allMatch(SlotValue::isCertainBound));
	}

	SlotValue rebind(final Map<String, String> binds) {
		if (isCertainBound()) {
			// 型パラメタが解決済みなので自身をそのまま返す
			return this;
		}

		final String bound = binds.get(typeParam);
		if (bound == null) {
			// 自分の typeParam が再定義されていない
			final SlotValue slot = new SlotValue(typeParam, descriptor);
			// 子スロットを bind しながらコピーする
			for (SlotValue child : slotList) {
				slot.slotList.add(child.rebind(binds));
			}
			return slot;
		} else if (slotList.size() == 0) {
			// もともとが型パラメタの定義の場合
			// 型パラメタのスロットに子供がいることはありえないので slotList.size == 0 ね
			if (bound.startsWith("T")) {
				// 型パラメタ名の再定義
				// see TypeSlot#createBindMap
				return new SlotValue(bound.substring(1), descriptor);
			} else {
				// 型パラメタに型引数をくっつける
				// certain bind
				if (bound.startsWith("[") || bound.indexOf('<') >= 0) {
					// 配列もしくはシグネチャなんでパースしてつくる
					return new SlotValueBuilder("=").build(bound);
				} else {
					// そのまま Slot つくればおｋ
					return new SlotValue("=", bound);
				}
			}
		} else {
			// コンパイラが作ってるものベースなので、ここに落ちることはない！
			throw new IllegalArgumentException();
		}
	}

	@Override
	public String toString() {
		return "SlotValue [typeParam=" + typeParam + ", descriptor=" + descriptor + ", slotList=" + slotList + "]";
	}

}
