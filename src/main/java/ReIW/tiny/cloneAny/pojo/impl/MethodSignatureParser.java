package ReIW.tiny.cloneAny.pojo.impl;

import java.util.function.Consumer;

import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import ReIW.tiny.cloneAny.pojo.Slot;
import ReIW.tiny.cloneAny.pojo.UnboundFormalTypeParameterException;

final class MethodSignatureParser extends SignatureParser {

	private static final void NOP(Slot val) {
	}

	private final Consumer<Slot> argCons;
	private final Consumer<Slot> retCons;

	MethodSignatureParser(final Consumer<Slot> argCons, final Consumer<Slot> retCons) {
		this.argCons = argCons == null ? MethodSignatureParser::NOP : argCons;
		this.retCons = retCons == null ? MethodSignatureParser::NOP : retCons;
	}

	void parseArgumentsAndReturn(final String descriptor, final String signature) {
		if (signature == null) {
			Type m = Type.getMethodType(descriptor);
			for (Type arg : m.getArgumentTypes()) {
				argCons.accept(new Slot(null, arg.getDescriptor()));
			}
			retCons.accept(new Slot(null, m.getReturnType().getDescriptor()));
		} else {
			new SignatureReader(signature).accept(this);
		}
	}

	@Override
	public SignatureVisitor visitParameterType() {
		cons = argCons;
		return super.visitParameterType();
	}

	@Override
	public SignatureVisitor visitReturnType() {
		cons = retCons;
		return super.visitReturnType();
	}

	@Override
	public void visitFormalTypeParameter(String name) {
		throw new UnboundFormalTypeParameterException("Method should not have formal type parameter.");
	}

}
