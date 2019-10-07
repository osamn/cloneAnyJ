package ReIW.tiny.cloneAny.pojo.impl;

import java.util.Stack;
import java.util.function.Consumer;

import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import ReIW.tiny.cloneAny.asm7.DefaultSignatureVisitor;
import ReIW.tiny.cloneAny.pojo.Slot;

public abstract class SlotLikeSignatureVisitor<TSlot extends Slot> extends DefaultSignatureVisitor {

	private final Stack<TSlot> stack = new Stack<>();

	protected String typeParamName;
	protected Consumer<TSlot> consumer;

	protected abstract TSlot newSlotLike(final String typeParam, final String descriptor);

	public void accept(final String signature) {
		new SignatureReader(signature).accept(this);
	}

	@Override
	public void visitFormalTypeParameter(final String name) {
		typeParamName = name;
	}

	@Override
	public void visitClassType(final String name) {
		stack.push(newSlotLike(typeParamName, Type.getObjectType(name).getDescriptor()));
	}

	@Override
	public void visitBaseType(final char descriptor) {
		stack.push(newSlotLike(typeParamName, Character.toString(descriptor)));
		// primitive の場合 visitEnd に回らないので、ここで明示的に呼んでおく
		visitEnd();
	}

	@Override
	public SignatureVisitor visitArrayType() {
		stack.push(newSlotLike(typeParamName, "["));
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
		stack.push(newSlotLike(name, "Ljava/lang/Object;"));
		// visitEnd にいかないので明示的に読んでおく
		visitEnd();
	}

	@Override
	public void visitEnd() {
		TSlot slot = unrollArray();
		if (stack.isEmpty()) {
			consumer.accept(slot);
		} else {
			stack.peek().slotList.add(slot);
		}
		typeParamName = null;
	}

	private TSlot unrollArray() {
		TSlot slot = stack.pop();
		while (!stack.isEmpty() && stack.peek().isArrayType) {
			stack.peek().slotList.add(slot);
			slot = stack.pop();
		}
		return slot;
	}

}