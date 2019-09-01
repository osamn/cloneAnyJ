package ReIW.tiny.cloneAny.pojo.impl;

import java.util.Stack;
import java.util.function.Consumer;

import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import ReIW.tiny.cloneAny.asm7.DefaultSignatureVisitor;
import ReIW.tiny.cloneAny.pojo.Slot;
import ReIW.tiny.cloneAny.pojo.UnboundFormalTypeParameterException;

final class MethodSignatureParser extends DefaultSignatureVisitor {

	private static final void nop(Slot val) {
	}

	private final Stack<Slot> stack = new Stack<>();

	private final Consumer<Slot> argCons;
	private final Consumer<Slot> retCons;

	private Consumer<Slot> cons;
	private String typeParamName;

	MethodSignatureParser(final Consumer<Slot> argCons, final Consumer<Slot> retCons) {
		this.argCons = argCons == null ? MethodSignatureParser::nop : argCons;
		this.retCons = retCons == null ? MethodSignatureParser::nop : retCons;
	}

	void parseArgumentsAndReturn(final String descriptor, final String signature) {
		if (signature == null) {
			Type m = Type.getMethodType(descriptor);
			for (Type t : m.getArgumentTypes()) {
				argCons.accept(Slot.getSlot(t.getDescriptor()));
			}
			retCons.accept(Slot.getSlot(m.getReturnType().getDescriptor()));
		} else {
			new SignatureReader(signature).accept(this);
		}
	}

	@Override
	public void visitFormalTypeParameter(String name) {
		throw new UnboundFormalTypeParameterException("Method should not have formal type parameter.");
	}

	@Override
	public void visitClassType(String name) {
		stack.push(new Slot(typeParamName, Type.getObjectType(name).getDescriptor()));
	}

	@Override
	public SignatureVisitor visitArrayType() {
		stack.push(new Slot(typeParamName, "["));
		typeParamName = null;
		return super.visitArrayType();
	}

	@Override
	public SignatureVisitor visitParameterType() {
		cons = argCons;
		return super.visitParameterType();
	}

	@Override
	public void visitBaseType(char descriptor) {
		cons.accept(new Slot(null, String.valueOf(descriptor)));
	}

	@Override
	public SignatureVisitor visitReturnType() {
		cons = retCons;
		return super.visitReturnType();
	}

	@Override
	public SignatureVisitor visitTypeArgument(char wildcard) {
		typeParamName = String.valueOf(wildcard);
		return super.visitTypeArgument(wildcard);
	}

	@Override
	public void visitTypeVariable(String name) {
		if (stack.isEmpty()) {
			cons.accept(new Slot(name, "Ljava/lang/Object;"));
		} else {
			stack.peek().slotList.add(new Slot(name, "Ljava/lang/Object;"));
		}
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
		while (!stack.isEmpty() && stack.peek().isArrayType()) {
			stack.peek().slotList.add(slot);
			slot = stack.pop();
		}
		return slot;
	}

}
