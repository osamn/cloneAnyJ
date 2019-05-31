package ReIW.tiny.cloneAny.pojo;

import java.util.ArrayList;
import java.util.Stack;

import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import ReIW.tiny.cloneAny.asm7.DefaultSignatureVisitor;

final class TypeSlotBuilder extends DefaultSignatureVisitor {

	static TypeSlot createTypeSlot(final String signature) {
		if (signature == null) {
			return null;
		} else {
			final TypeSlotBuilder parser = new TypeSlotBuilder();
			new SignatureReader(signature).accept(parser);
			return new TypeSlot(parser.formalSlots, parser.supersSlots.get(0));
		}
	}

	private final ArrayList<Slot> formalSlots = new ArrayList<>(5);
	private final ArrayList<Slot> supersSlots = new ArrayList<>(1);

	private final Stack<Slot> stack = new Stack<>();
	private String typeParam;
	private ArrayList<Slot> activeSlots;

	private TypeSlotBuilder() {
		activeSlots = formalSlots;
	}

	@Override
	public SignatureVisitor visitSuperclass() {
		activeSlots = supersSlots;
		return super.visitSuperclass();
	}

	@Override
	public void visitFormalTypeParameter(String name) {
		typeParam = name;
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
			activeSlots.add(slot);
		} else {
			stack.peek().slotList.add(slot);
		}
	}
}