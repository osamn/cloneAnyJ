package ReIW.tiny.cloneAny.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.objectweb.asm.signature.SignatureReader;

import ReIW.tiny.cloneAny.pojo.impl.SignatureParser;
import ReIW.tiny.cloneAny.utils.Descriptors;

/*
 * クラスのメンバの型情報を持つ人
 * ただし、型パラメタについては各 SignatureParser を経由しないと設定されないよ
 */
public class Slot {

	public final String typeParam;
	public final String descriptor;

	public final List<Slot> slotList = new ArrayList<>();

	public final boolean isArrayType;
	public final boolean isPrimitiveType;
	public final boolean isBoxingType;

	// generic じゃない配列は自分で階層までつくる
	public Slot(final String typeParam, final String descriptor) {
		if (descriptor.contains("<")) {
			// generic はエラーにしとく
			throw new IllegalArgumentException();
		}
		this.typeParam = typeParam;
		if (descriptor.startsWith("[")) {
			this.descriptor = "[";
			this.isArrayType = true;
			this.isPrimitiveType = false;
			this.isBoxingType = false;

			final String componentDesc = descriptor.substring(1);
			// SignatureReader#visitArrayType から呼ばれるときは descriptor == "[" なので
			// 子スロットを追加しないよ
			if (!componentDesc.isEmpty()) {
				this.slotList.add(new Slot(typeParam, componentDesc));
			}
		} else {
			this.descriptor = descriptor;
			this.isArrayType = false;
			this.isPrimitiveType = Descriptors.isPrimitiveType(descriptor);
			this.isBoxingType = Descriptors.isBoxingType(descriptor);
		}
	}

	public String getClassDescriptor() {
		if (isArrayType) {
			return descriptor + slotList.get(0).getClassDescriptor();
		}
		if (slotList.size() > 0) {
			// primitive はここには来ないので L...; 前提だよ
			// L... と ; に分割して間に <generic> を埋め込む
			final String body = descriptor.substring(0, descriptor.length() - 1);
			return body + "<" + slotList.stream().map(s -> {
				return s.getClassDescriptor();
			}).collect(Collectors.joining()) + ">;";
		} else {
			if (typeParam == null || typeParam.contentEquals("=") || typeParam.contentEquals("+")
					|| typeParam.contentEquals("-")) {
				return descriptor;
			} else {
				return "T" + typeParam + ";";
			}
		}
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

		// とりあえず自身の型引数を bind したコピーを作る
		final String bound = binds.get(typeParam);
		if (bound == null) {
			// 自分の typeParam が再定義されていない
			final Slot slot = new Slot(typeParam, descriptor);
			// 子スロットを bind しながらコピーする
			for (Slot child : slotList) {
				slot.slotList.add(child.rebind(binds));
			}
			return slot;
		} else {
			// もともとが型パラメタの定義で子スロットがない場合
			if (bound.startsWith("T")) {
				// 型パラメタ名の再定義
				// see TypeSlot#createBindMap
				return new Slot(bound.substring(1), descriptor);
			} else {
				// 型パラメタに型引数をくっつける
				// certain bind
				if (!bound.contains("<")) {
					// generic じゃない場合はそのままくっつける
					return new Slot("=", bound);
				} else {
					return new BoundSlotBuilder().parse(bound);
				}
			}
		}
	}

	@Override
	public String toString() {
		return "Slot [typeParam=" + typeParam + ", descriptor=" + descriptor + ", slotList=" + slotList + "]";
	}

	private static final class BoundSlotBuilder extends SignatureParser {

		private Slot slot;
		
		private BoundSlotBuilder() {
			this.typeParamName = "=";
			this.cons = s -> this.slot = s;
		}

		private Slot parse(final String signature) {
			new SignatureReader(signature).accept(this);
			return slot;
		}

		@Override
		public void visitFormalTypeParameter(String name) {
			// 独立した型パラメタがあったらエラー
			throw new UnboundFormalTypeParameterException();
		}

	}
}
