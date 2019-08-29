package ReIW.tiny.cloneAny.pojo.impl;

import java.util.Arrays;
import java.util.Stack;
import java.util.function.Consumer;

import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import ReIW.tiny.cloneAny.asm7.DefaultSignatureVisitor;
import ReIW.tiny.cloneAny.pojo.Slot;

final class ClassSignatureParser extends DefaultSignatureVisitor {

	private final Stack<Slot> stack = new Stack<>();

	private final Consumer<Slot> supers;

	private String typeParamName;

	private Consumer<Slot> cons;

	ClassSignatureParser(final Consumer<Slot> formals, final Consumer<Slot> supers) {
		this.supers = supers;
		cons = formals;
	}

	void parse(final String superName, final String[] interfaces, final String signature) {
		if (signature == null) {
			// signature がない場合は、自クラスも継承元も non generic なので
			supers.accept(new Slot(null, Type.getObjectType(superName).getDescriptor()));
			if (interfaces != null) {
				Arrays.stream(interfaces).map(intf -> new Slot(null, Type.getObjectType(intf).getDescriptor()))
						.forEach(supers);
			}
		} else {
			new SignatureReader(signature).accept(this);
		}
	}

	@Override
	public void visitFormalTypeParameter(String name) {
		typeParamName = name;
	}

	@Override
	public SignatureVisitor visitSuperclass() {
		cons = supers;
		return super.visitSuperclass();
	}

	@Override
	public void visitClassType(String name) {
		stack.push(new Slot(typeParamName, Type.getObjectType(name).getDescriptor()));
	}

	@Override
	public SignatureVisitor visitTypeArgument(char wildcard) {
		typeParamName = String.valueOf(wildcard);
		return super.visitTypeArgument(wildcard);
	}

	@Override
	public void visitTypeVariable(String name) {
		stack.peek().slotList.add(new Slot(name, "Ljava/lang/Object;"));
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
