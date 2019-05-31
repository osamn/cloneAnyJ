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

	static MethodVisitor parameterParserVisitor(final String descriptor, final String signature,
			final BiConsumer<String, Slot> parametersCons) {
		final ArrayList<Slot> slots = new ArrayList<>();
		parseArgumentsAndReturn(descriptor, signature, slots::add, null);
		final Iterator<Slot> it = slots.iterator();
		return new DefaultMethodVisitor() {
			@Override
			public void visitParameter(String name, int access) {
				parametersCons.accept(name, it.next());
			};
		};
	}

	static void parseArgumentsAndReturn(final String descriptor, final String signature,
			final Consumer<Slot> argumentsCons, final Consumer<Slot> returnCons) {
		if (signature == null) {
			Type m = Type.getMethodType(descriptor);
			for (Type t : m.getArgumentTypes()) {
				argumentsCons.accept(new Slot(null, t.getInternalName()));
			}
			returnCons.accept(new Slot(null, m.getReturnType().getInternalName()));
		} else {
			final MethodSignatureParser parser = new MethodSignatureParser(argumentsCons, returnCons);
			new SignatureReader(signature).accept(parser);
		}
	}

	private final Consumer<Slot> arguments;
	private final Consumer<Slot> returns;

	private final Stack<Slot> stack = new Stack<>();

	private Consumer<Slot> cons;
	private String typeParamName;

	private MethodSignatureParser(Consumer<Slot> argumentsCons, Consumer<Slot> returnCons) {
		this.arguments = argumentsCons;
		this.returns = returnCons;
	}

	@Override
	public void visitFormalTypeParameter(String name) {
		throw new UnboundFormalTypeParameterException();
	}

	@Override
	public SignatureVisitor visitParameterType() {
		cons = arguments;
		return super.visitParameterType();
	}

	@Override
	public void visitBaseType(char descriptor) {
		cons.accept(new Slot(null, String.valueOf(descriptor)));
	}

	@Override
	public SignatureVisitor visitReturnType() {
		cons = returns;
		return super.visitReturnType();
	}

	@Override
	public void visitClassType(String name) {
		stack.push(new Slot(typeParamName, name));
	}

	@Override
	public SignatureVisitor visitTypeArgument(char wildcard) {
		typeParamName = String.valueOf(wildcard);
		return super.visitTypeArgument(wildcard);
	}

	@Override
	public void visitTypeVariable(String name) {
		if (stack.isEmpty()) {
			cons.accept(new Slot(name, null));
		} else {
			stack.peek().slotList.add(new Slot(name, null));
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
	}

}
