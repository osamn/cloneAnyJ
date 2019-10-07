package ReIW.tiny.cloneAny.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ReIW.tiny.cloneAny.pojo.impl.SlotLikeSignatureVisitor;
import ReIW.tiny.cloneAny.utils.Descriptors;

// TODO typeParam が + とか - の場合は実行時型をつかって Ditto つくる必要あるよね

/*
 * クラスのメンバの型情報を持つ人
 */
public class Slot {

	public final String typeParam;
	public final String descriptor;

	public final boolean isArrayType;
	public final boolean isPrimitiveType;
	public final boolean isBoxingType;

	public final List<Slot> slotList = new ArrayList<>();

	public Slot(final String typeParam, final String descriptor) {
		this.typeParam = typeParam;
		if (descriptor.contentEquals("[")) {
			this.descriptor = "[";
			this.isArrayType = true;
			this.isPrimitiveType = false;
			this.isBoxingType = false;
		} else {
			this.descriptor = descriptor;
			this.isArrayType = false;
			this.isPrimitiveType = Descriptors.isPrimitiveType(descriptor);
			this.isBoxingType = Descriptors.isBoxingType(descriptor);
		}
	}

	/** 配列も考慮した descriptor */
	// 型パラメタはふくまない単純な descriptor
	// なんだけど、rebind とかされて決定する場合もあるので実行時に作成する
	public String getTypeDescriptor() {
		if (isArrayType) {
			return descriptor + slotList.get(0).getTypeDescriptor();
		}
		return descriptor;
	}

	public boolean isCertainBound() {
		final boolean bound = typeParam == null || typeParam.contentEquals("=") || typeParam.contentEquals("+")
				|| typeParam.contentEquals("-");
		return bound && slotList.stream().allMatch(Slot::isCertainBound);
	}

	public Slot rebind(final Map<String, String> binds) {
		if (isCertainBound()) {
			// 型パラメタが解決済みなので自身をそのまま返す
			return this;
		}

		final String bound = binds.get(typeParam);
		if (bound == null) {
			// 自分の typeParam が再定義されていない
			final Slot slot = new Slot(typeParam, descriptor);
			// 子スロットを bind しながらコピーする
			for (Slot child : slotList) {
				slot.slotList.add(child.rebind(binds));
			}
			return slot;
		} else if (slotList.size() == 0) {
			// もともとが型パラメタの定義の場合
			// 型パラメタのスロットに子供がいることはありえないので slotList.size == 0 ね
			if (bound.startsWith("T")) {
				// 型パラメタ名の再定義
				// see TypeSlot#createBindMap
				return new Slot(bound.substring(1), descriptor);
			} else {
				// 型パラメタに型引数をくっつける
				// certain bind
				if (bound.startsWith("[") || bound.indexOf('<') >= 0) {
					// 配列もしくはシグネチャなんでパースしてつくる
					return new SlotBuilder("=").build(bound);
				} else {
					// そのまま Slot つくればおｋ
					return new Slot("=", bound);
				}
			}
		} else {
			// コンパイラが作ってるものベースなので、ここに落ちることはない！
			throw new IllegalArgumentException();
		}
	}

	@Override
	public String toString() {
		return "Slot [typeParam=" + typeParam + ", descriptor=" + descriptor + ", slotList=" + slotList + "]";
	}
	
	static class SlotBuilder extends SlotLikeSignatureVisitor<Slot> {
		private Slot slot;

		private SlotBuilder(final String typeParam) {
			super.typeParamName = typeParam;
			super.consumer = (val) -> {
				this.slot = val;
			};
		}

		private Slot build(final String signature) {
			this.accept(signature);
			return slot;
		}

		@Override
		protected Slot newSlotLike(final String typeParam, final String descriptor) {
			return new Slot(typeParam, descriptor);
		}
	}

}
