package ReIW.tiny.cloneAny.pojo_.impl;

import java.util.function.Consumer;

import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import ReIW.tiny.cloneAny.pojo.UnboundFormalTypeParameterException;
import ReIW.tiny.cloneAny.pojo_.Slot;

final class MethodSignatureParser extends SlotLikeSignatureVisitor<Slot> {

	private static final Consumer<Slot> NOP = v -> {
	};
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
	protected Slot newSlotLike(final String typeParam, final String descriptor) {
		return new Slot(typeParam, descriptor);
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
