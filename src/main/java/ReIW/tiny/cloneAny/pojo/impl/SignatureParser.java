package ReIW.tiny.cloneAny.pojo.impl;

import java.util.Stack;
import java.util.function.Consumer;

import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureVisitor;

import ReIW.tiny.cloneAny.asm7.DefaultSignatureVisitor;
import ReIW.tiny.cloneAny.pojo.Slot;

public abstract class SignatureParser extends DefaultSignatureVisitor {

	private final Stack<Slot> stack = new Stack<>();

	protected String typeParamName;
	protected Consumer<Slot> cons;

	@Override
	public void visitFormalTypeParameter(String name) {
		typeParamName = name;
	}

	@Override
	public void visitClassType(String name) {
		stack.push(new Slot(typeParamName, Type.getObjectType(name).getDescriptor()));
	}

	@Override
	public void visitBaseType(char descriptor) {
		stack.push(new Slot(typeParamName, Character.toString(descriptor)));
		// primitive の場合 visitEnd に回らないので、ここで明示的に呼んでおく
		visitEnd();
	}

	@Override
	public SignatureVisitor visitArrayType() {
		stack.push(new Slot(typeParamName, "["));
		typeParamName = null;
		return super.visitArrayType();
	}

	@Override
	public SignatureVisitor visitTypeArgument(char wildcard) {
		typeParamName = String.valueOf(wildcard);
		return super.visitTypeArgument(wildcard);
	}

	@Override
	public void visitTypeVariable(String name) {
		stack.push(new Slot(name, "Ljava/lang/Object;"));
		visitEnd();
	}

	@Override
	public void visitEnd() {
		Slot slot = unrollArray();
		if (stack.isEmpty()) {
			cons.accept(slot);
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
