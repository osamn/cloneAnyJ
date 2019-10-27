package ReIW.tiny.cloneAny.pojo.impl;

import java.util.Stack;
import java.util.function.Consumer;

import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import ReIW.tiny.cloneAny.asm7.DefaultSignatureVisitor;

public abstract class SlotLikeSignatureParser extends DefaultSignatureVisitor {

	private final Stack<SlotValue> stack = new Stack<>();

	/** '#' '*' '=' '-' '+' または null */
	String wildcard;
	String typeParam;
	Consumer<SlotValue> slotCons;

	protected abstract SlotValue newSlotLike(final String wildcard, final String typeParam, final String descriptor);

	public void accept(final String signature) {
		new SignatureReader(signature).accept(this);
	}

	@Override
	public void visitFormalTypeParameter(final String name) {
		// formal は目印に wildcard を # にしてみる
		wildcard = "#";
		typeParam = name;
	}

	@Override
	public void visitClassType(final String name) {
		stack.push(newSlotLike(wildcard, typeParam, Type.getObjectType(name).getDescriptor()));
	}

	@Override
	public void visitBaseType(final char descriptor) {
		stack.push(newSlotLike(wildcard, typeParam, Character.toString(descriptor)));
		// primitive の場合 visitEnd に回らないので、ここで明示的に呼んでおく
		completeSlot();
	}

	@Override
	public SignatureVisitor visitArrayType() {
		stack.push(newSlotLike(wildcard, typeParam, "["));
		// 配列は特別の型なんで completeSlot しないよ
		wildcard = null;
		typeParam = null;
		return super.visitArrayType();
	}

	@Override
	public SignatureVisitor visitTypeArgument(final char wildcard) {
		this.wildcard = String.valueOf(wildcard);
		return super.visitTypeArgument(wildcard);
	}

	// List<?> みたいに未指定を明示した時
	@Override
	public void visitTypeArgument() {
		// visit*Type とか visitTypeVariable とかに行かないのでここで push する
		// wildcard は * にしておくよ
		stack.push(newSlotLike("*", null, "Ljava/lang/Object;"));
		// visitEnd にいかないので明示的に
		completeSlot();
	}

	@Override
	public void visitTypeVariable(final String name) {
		stack.push(newSlotLike(wildcard, name, "Ljava/lang/Object;"));
		// visitEnd にいかないので明示的に
		completeSlot();
	}

	@Override
	public void visitEnd() {
		completeSlot();
	}

	private void completeSlot() {
		SlotValue slot = unrollArray();
		if (stack.isEmpty()) {
			slotCons.accept(slot);
		} else {
			stack.peek().slotList.add(slot);
		}
		wildcard = null;
		typeParam = null;
	}

	private SlotValue unrollArray() {
		SlotValue slot = stack.pop();
		while (!stack.isEmpty() && stack.peek().arrayType) {
			stack.peek().slotList.add(slot);
			slot = stack.pop();
		}
		return slot;
	}

}