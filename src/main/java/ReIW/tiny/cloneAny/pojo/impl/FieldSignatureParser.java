package ReIW.tiny.cloneAny.pojo.impl;

import java.util.Stack;
import java.util.function.Consumer;

import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import ReIW.tiny.cloneAny.asm7.DefaultSignatureVisitor;
import ReIW.tiny.cloneAny.pojo.Slot;

final class FieldSignatureParser extends DefaultSignatureVisitor {

	private final Stack<Slot> stack = new Stack<>();
	private final Consumer<Slot> cons;

	private String typeParamName;

	FieldSignatureParser(final Consumer<Slot> cons) {
		this.cons = cons;
	}

	void parse(final String descriptor, final String signature) {
		if (signature == null) {
			cons.accept(new Slot(null, descriptor));
		} else {
			new SignatureReader(signature).accept(this);
		}
	}

	@Override
	public void visitClassType(String name) {
		// generic は primitive がないので getObjectType でおけ
		stack.push(new Slot(typeParamName, Type.getObjectType(name).getDescriptor()));
	}

	@Override
	public SignatureVisitor visitArrayType() {
		stack.push(new Slot(null, "["));
		return super.visitArrayType();
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
		Slot slot = stack.pop();
		if (stack.isEmpty()) {
			cons.accept(slot);
		} else {
			stack.peek().slotList.add(slot);
		}
		typeParamName = null;
	}

}
