package ReIW.tiny.cloneAny.pojo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import ReIW.tiny.cloneAny.asm7.DefaultMethodVisitor;
import ReIW.tiny.cloneAny.asm7.DefaultSignatureVisitor;

final class MethodSignatureParser extends DefaultSignatureVisitor {

	static MethodVisitor acceptCtor(final String descriptor, final String signature, BiConsumer<String, Slot> cons) {
		final Iterator<Slot> it;
		if (signature == null) {
			ArrayList<Slot> slots = new ArrayList<>();
			for (Type t : Type.getArgumentTypes(descriptor)) {
				slots.add(new Slot("@", t.getInternalName()));
			}
			it = slots.iterator();
		} else {
			final MethodSignatureParser parser = new MethodSignatureParser();
			new SignatureReader(signature).accept(parser);
			it = parser.params.iterator();
		}
		return new DefaultMethodVisitor() {
			@Override
			public void visitParameter(String name, int access) {
				cons.accept(name, it.next());
			};
		};
	}

	static void acceptGet(final String descriptor, final String signature, Consumer<Slot> cons) {
		final Slot slot;
		if (signature == null) {
			slot = new Slot("@", Type.getReturnType(descriptor).getInternalName());
		} else {
			final MethodSignatureParser parser = new MethodSignatureParser();
			new SignatureReader(signature).accept(parser);
			slot = parser.returns.get(0);
		}
		cons.accept(slot);
	}

	static void acceptSet(final String descriptor, final String signature, Consumer<Slot> cons) {
		final Slot slot;
		if (signature == null) {
			slot = new Slot("@", Type.getArgumentTypes(descriptor)[0].getInternalName());
		} else {
			final MethodSignatureParser parser = new MethodSignatureParser();
			new SignatureReader(signature).accept(parser);
			slot = parser.params.get(0);
		}
		cons.accept(slot);
	}

	private final ArrayList<Slot> params = new ArrayList<>(7);
	private final ArrayList<Slot> returns = new ArrayList<>(1);

	private final Stack<Slot> stack = new Stack<>();

	private String typeParam = "@";
	private ArrayList<Slot> activeSlots;

	@Override
	public SignatureVisitor visitParameterType() {
		activeSlots = params;
		return super.visitParameterType();
	}

	@Override
	public SignatureVisitor visitReturnType() {
		activeSlots = returns;
		return super.visitReturnType();
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
