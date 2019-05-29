package ReIW.tiny.cloneAny.pojo;

import java.util.ArrayList;
import java.util.Stack;
import java.util.function.Consumer;

import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import ReIW.tiny.cloneAny.asm7.DefaultSignatureVisitor;

final class FieldSignatureParser extends DefaultSignatureVisitor {

	static AccessEntry accept(final String name, final String descriptor, final String signature) {
		if (signature == null) {
			return new AccessEntry(Types.ACC_FIELD, name, new Slot("@", descriptor), null);
		} else {
			final FieldSignatureParser parser = new FieldSignatureParser();
			new SignatureReader(signature).accept(parser);
			return new AccessEntry(Types.ACC_FIELD, name, parser.slots.get(0), null);
		}
	}

	static void accept(final String descriptor, final String signature, final Consumer<Slot> cons) {
		if (signature == null) {
			cons.accept(new Slot("@", descriptor));
		} else {
			final FieldSignatureParser parser = new FieldSignatureParser();
			new SignatureReader(signature).accept(parser);
			cons.accept(parser.slots.get(0));
		}
	}

	private final ArrayList<Slot> slots = new ArrayList<>(1);
	private final Stack<Slot> stack = new Stack<>();
	private String typeParam = "@";

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
			slots.add(slot);
		} else {
			stack.peek().slotList.add(slot);
		}
	}

}
