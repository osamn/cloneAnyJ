package ReIW.tiny.cloneAny.pojo.impl;

import java.util.function.Consumer;

import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import ReIW.tiny.cloneAny.pojo.Slot;
import ReIW.tiny.cloneAny.pojo.UnboundFormalTypeParameterException;

final class MethodSignatureParser extends Slot.SlotSignatureVisitor {

	private final Consumer<Slot> argCons;
	private final Consumer<Slot> retCons;

	MethodSignatureParser(final Consumer<Slot> argCons, final Consumer<Slot> retCons) {
		this.argCons = argCons == null ? NOP : argCons;
		this.retCons = retCons == null ? NOP : retCons;
	}

	void parseArgumentsAndReturn(final String descriptor, final String signature) {
		new SignatureReader(signature == null ? descriptor : signature).accept(this);
	}

	@Override
	public SignatureVisitor visitParameterType() {
		consumer = argCons;
		return super.visitParameterType();
	}

	@Override
	public SignatureVisitor visitReturnType() {
		consumer = retCons;
		return super.visitReturnType();
	}

	@Override
	public void visitFormalTypeParameter(String name) {
		throw new UnboundFormalTypeParameterException("Method should not have formal type parameter.");
	}

}
