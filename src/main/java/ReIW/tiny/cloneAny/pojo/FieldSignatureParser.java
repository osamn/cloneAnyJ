package ReIW.tiny.cloneAny.pojo;

import java.util.Stack;
import java.util.function.Consumer;

import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import ReIW.tiny.cloneAny.asm7.DefaultSignatureVisitor;

final class FieldSignatureParser extends DefaultSignatureVisitor {

	static void parse(final String descriptor, final String signature, final Consumer<Slot> fieldCons) {
		if (signature == null) {
			fieldCons.accept(new Slot(null, descriptor));
		} else {
			final FieldSignatureParser parser = new FieldSignatureParser(fieldCons);
			new SignatureReader(signature).accept(parser);
		}
	}

	private final Consumer<Slot> cons;

	private final Stack<Slot> stack = new Stack<>();
	private String typeParamName;

	private FieldSignatureParser(final Consumer<Slot> cons) {
		this.cons = cons;
	}

	@Override
	public void visitClassType(String name) {
		// generic は primitive がないので getObjectType でおけ
		stack.push(new Slot(typeParamName, Type.getObjectType(name).getDescriptor()));
	}

	@Override
	public SignatureVisitor visitTypeArgument(char wildcard) {
		typeParamName = String.valueOf(wildcard);
		return super.visitTypeArgument(wildcard);
	}

	@Override
	public void visitTypeVariable(String name) {
		if (stack.isEmpty()) {
			cons.accept(new Slot(name));
		} else {
			stack.peek().slotList.add(new Slot(name));
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
