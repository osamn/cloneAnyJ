package ReIW.tiny.cloneAny.pojo.impl;

import java.util.Arrays;
import java.util.function.Consumer;

import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import ReIW.tiny.cloneAny.pojo.UnboundFormalTypeParameterException;
import ReIW.tiny.cloneAny.utils.Consumers;

public class MethodSignatureParser extends SlotLikeSignatureParser<SlotValue> {

	private final Consumer<SlotValue> argCons;
	private final Consumer<SlotValue> retCons;

	MethodSignatureParser(final Consumer<SlotValue> argCons, final Consumer<SlotValue> retCons) {
		this.argCons = argCons == null ? Consumers.nop() : argCons;
		this.retCons = retCons == null ? Consumers.nop() : retCons;
	}

	void parseArgumentsAndReturn(final String descriptor, final String signature) {
		if (signature != null) {
			// generic なとき
			new SignatureReader(signature).accept(this);
		} else if (descriptor.contains("[")) {
			// generic じゃないけど配列がありそうなとき
			new SignatureReader(descriptor).accept(this);
		} else {
			// generic でも配列でもないので子要素を持たないよ
			Arrays.stream(Type.getArgumentTypes(descriptor))
					.forEach(t -> argCons.accept(newSlotLike(null, t.getDescriptor())));
			retCons.accept(newSlotLike(null, Type.getReturnType(descriptor).getDescriptor()));
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
