package ReIW.tiny.cloneAny.pojo.impl;

import java.util.Arrays;
import java.util.function.Consumer;

import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import ReIW.tiny.cloneAny.pojo.UnboundFormalTypeParameterException;

public class MethodSignatureParser extends SlotLikeSignatureParser<SlotValue> {

	private static final Consumer<SlotValue> NOP = v -> {
	};

	private final Consumer<SlotValue> argCons;
	private final Consumer<SlotValue> retCons;

	MethodSignatureParser(final Consumer<SlotValue> argCons, final Consumer<SlotValue> retCons) {
		this.argCons = argCons == null ? NOP : argCons;
		this.retCons = retCons == null ? NOP : retCons;
	}

	void parseArgumentsAndReturn(final String descriptor, final String signature) {
		if (signature == null) {
			Arrays.stream(Type.getArgumentTypes(descriptor))
					.forEach(t -> argCons.accept(newSlotLike(null, t.getDescriptor())));
			retCons.accept(newSlotLike(null, Type.getReturnType(descriptor).getDescriptor()));
		} else {
			new SignatureReader(signature == null ? descriptor : signature).accept(this);
		}
	}

	@Override
	protected SlotValue newSlotLike(final String typeParam, final String descriptor) {
		return new SlotValue(typeParam, descriptor);
	}

	@Override
	public SignatureVisitor visitParameterType() {
		slotCons = argCons;
		return super.visitParameterType();
	}

	@Override
	public SignatureVisitor visitReturnType() {
		slotCons = retCons;
		return super.visitReturnType();
	}

	@Override
	public void visitFormalTypeParameter(String name) {
		throw new UnboundFormalTypeParameterException("Method should not have formal type parameter.");
	}

}
