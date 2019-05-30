package ReIW.tiny.cloneAny.pojo;

import java.util.Stack;
import java.util.function.Consumer;

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
	private String typeParam;

	private FieldSignatureParser(final Consumer<Slot> cons) {
		this.cons = cons;
	}

	@Override
	public void visitFormalTypeParameter(String name) {
		// FIXME これいらないようなきがする
		throw new UnboundFormalTypeParameterException();
	}

	@Override
	public void visitClassType(String name) {
		stack.push(new Slot(typeParam, name));
	}

	@Override
	public SignatureVisitor visitTypeArgument(char wildcard) {
		typeParam = String.valueOf(wildcard);
		return super.visitTypeArgument(wildcard);
	}

	@Override
	public void visitTypeVariable(String name) {
		stack.peek().slotList.add(new Slot(name, null));
	}

	@Override
	public void visitEnd() {
		Slot slot = stack.pop();
		if (stack.isEmpty()) {
			cons.accept(slot);
		} else {
			stack.peek().slotList.add(slot);
		}
	}

}
