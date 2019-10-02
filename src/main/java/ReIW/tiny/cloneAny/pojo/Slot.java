package ReIW.tiny.cloneAny.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;

import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import ReIW.tiny.cloneAny.asm7.DefaultSignatureVisitor;
import ReIW.tiny.cloneAny.utils.Descriptors;

/*
 * クラスのメンバの型情報を持つ人
 */
public class Slot {

	public static Slot getSlot(final String typeParam, final String descriptor, final String signature) {
		if (signature != null) {
			final SlotInitializer init = new SlotInitializer(typeParam);
			init.accept(signature);
			return init.slot;
		} else if (descriptor.startsWith("[")) {
			final SlotInitializer init = new SlotInitializer(typeParam);
			init.accept(descriptor);
			return init.slot;
		} else {
			return new Slot(typeParam, descriptor);
		}
	}

	public final String typeParam;
	public final String descriptor;

	public final boolean isArrayType;
	public final boolean isPrimitiveType;
	public final boolean isBoxingType;

	public final List<Slot> slotList = new ArrayList<>();

	protected Slot(final String typeParam, final String descriptor) {
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

	// TODO あとでさくじょ
	@Deprecated
	public String getTypeSignature() {
		if (isArrayType) {
			return descriptor + slotList.get(0).getTypeSignature();
		}
		if (slotList.size() > 0) {
			// primitive はここには来ないので L...; 前提だよ
			// L... と ; に分割して間に <generic> を埋め込む
			final String body = descriptor.substring(0, descriptor.length() - 1);
			return body + "<" + slotList.stream().map(s -> {
				return s.getTypeSignature();
			}).collect(java.util.stream.Collectors.joining()) + ">;";
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
				if (bound.startsWith("[")) {
					// 配列なんでパースしてつくる
					return Slot.getSlot("=", bound, null);
				} else if (bound.indexOf('<') >= 0) {
					// signature としてパースする
					return Slot.getSlot("=", null, bound);
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

	private static class SlotInitializer extends SlotSignatureVisitor {
		private Slot slot;

		private SlotInitializer(final String typeParam) {
			super.typeParamName = typeParam;
			super.consumer = (val) -> {
				this.slot = val;
			};
		}

		@Override
		public void visitFormalTypeParameter(String name) {
			throw new UnboundFormalTypeParameterException();
		}
	}

	public static abstract class SlotSignatureVisitor extends DefaultSignatureVisitor {

		public static final Consumer<Slot> NOP = slot -> {
		};

		private final Stack<Slot> stack = new Stack<>();

		protected String typeParamName;
		protected Consumer<Slot> consumer;

		protected Slot newSlot(final String typeParam, final String descriptor) {
			return new Slot(typeParam, descriptor);
		}

		protected void accept(final String signature) {
			new SignatureReader(signature).accept(this);
		}

		@Override
		public void visitFormalTypeParameter(final String name) {
			typeParamName = name;
		}

		@Override
		public void visitClassType(final String name) {
			stack.push(newSlot(typeParamName, Type.getObjectType(name).getDescriptor()));
		}

		@Override
		public void visitBaseType(final char descriptor) {
			stack.push(newSlot(typeParamName, Character.toString(descriptor)));
			// primitive の場合 visitEnd に回らないので、ここで明示的に呼んでおく
			visitEnd();
		}

		@Override
		public SignatureVisitor visitArrayType() {
			stack.push(newSlot(typeParamName, "["));
			typeParamName = null;
			return super.visitArrayType();
		}

		@Override
		public SignatureVisitor visitTypeArgument(final char wildcard) {
			typeParamName = String.valueOf(wildcard);
			return super.visitTypeArgument(wildcard);
		}

		@Override
		public void visitTypeVariable(final String name) {
			stack.push(newSlot(name, "Ljava/lang/Object;"));
			// visitEnd にいかないので明示的に読んでおく
			visitEnd();
		}

		@Override
		public void visitEnd() {
			Slot slot = unrollArray();
			if (stack.isEmpty()) {
				consumer.accept(slot);
			} else {
				stack.peek().slotList.add(slot);
			}
			typeParamName = null;
		}

		private Slot unrollArray() {
			Slot slot = stack.pop();
			while (!stack.isEmpty() && stack.peek().isArrayType) {
				stack.peek().slotList.add(slot);
				slot = stack.pop();
			}
			return slot;
		}

	}

}
