package ReIW.tiny.cloneAny.pojo.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import ReIW.tiny.cloneAny.pojo.Slot;
import ReIW.tiny.cloneAny.utils.Descriptors;

class SlotValue implements Slot {

	final String wildcard;
	final String typeParam;
	final String descriptor;

	final boolean arrayType;
	final boolean primitiveType;
	final boolean boxingType;

	final List<SlotValue> slotList = new ArrayList<>();

	SlotValue(final String wildcard, final String typeParam, final String descriptor) {
		assert descriptor != null;

		this.wildcard = wildcard;
		this.typeParam = typeParam;
		this.descriptor = descriptor;
		arrayType = descriptor.contentEquals("[");
		primitiveType = arrayType ? false : Descriptors.isPrimitiveType(descriptor);
		boxingType = arrayType ? false : Descriptors.isBoxingType(descriptor);
	}

	/** 配列も考慮した descriptor */
	// 型パラメタはふくまない単純な descriptor
	// なんだけど、rebind とかされて決定する場合もあるので実行時に作成する
	// spock でつかうときは descriptor(getter) と @descriptor(field) で使い分けてね
	@Override
	public String getDescriptor() {
		if (arrayType) {
			return descriptor + slotList.get(0).getDescriptor();
		}
		return descriptor;
	}

	@Override
	public boolean isArray() {
		return arrayType;
	}

	public String getSignature() {
		if (arrayType) {
			return descriptor + slotList.get(0).getSignature();
		}
		if (slotList.size() == 0) {
			if (Objects.equals(wildcard, "*")) {
				return wildcard;
			}
			final String c = (wildcard == null || wildcard.contentEquals("=")) ? "" : wildcard;
			if (typeParam == null) {
				return c + descriptor;
			} else {
				return c + "T" + typeParam + ";";
			}
		}
		// シグネチャを再構築する
		final StringBuilder buf = new StringBuilder();
		buf.append(descriptor.substring(0, descriptor.length() - 1));
		buf.append("<");
		slotList.forEach(slot -> buf.append(slot.getSignature()));
		buf.append(">;");
		return buf.toString();
	}

	// 型パラメタのスロットリスト
	// 配列の要素スロットもあるよ
	@Override
	public List<Slot> descendants() {
		return slotList.stream().map(Slot.class::cast).collect(Collectors.toUnmodifiableList());
	}

	boolean isCertainBound() {
		final boolean bound = typeParam == null;
		return bound && (slotList == null || slotList.stream().allMatch(SlotValue::isCertainBound));
	}

	// see ClassType#createBindMap
	SlotValue rebind(final Map<String, String> binds) {
		if (isCertainBound()) {
			// 型パラメタが解決済みなので自身をそのまま返す
			return this;
		}

		final String bound = typeParam == null ? null : binds.get(typeParam);
		if (bound == null) {
			// 自分の typeParam が再定義されていない
			final SlotValue slot = new SlotValue(wildcard, typeParam, descriptor);
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
				return new SlotValue(wildcard, bound.substring(1), descriptor);
			} else {
				// 型パラメタに型引数をくっつける
				// certain bind
				if (bound.startsWith("[") || bound.indexOf('<') >= 0) {
					// 配列、もしくは generic なんでパースしてつくる
					return new SlotValueBuilder().setPrimaryWildcard(wildcard).build(bound);
				} else {
					// そのまま Slot つくればおｋ
					return new SlotValue("=", null, bound);
				}
			}
		} else {
			// コンパイラが作ってるものベースなので、ここに落ちることはない！
			throw new IllegalArgumentException();
		}
	}

	@Override
	public String toString() {
		return "SlotValue [wildcard=" + wildcard + ", typeParam=" + typeParam + ", descriptor=" + descriptor + ", "
				+ slotList + "]";
	}

	// なんかキャスト書くのもいまいちなんで追加してみたけどどうなんやろ
	static SlotValue of(Slot slot) {
		return (SlotValue) slot;
	}
}
