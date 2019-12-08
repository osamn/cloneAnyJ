package ReIW.tiny.cloneAny.pojo.impl;

import java.util.Arrays;
import java.util.function.Consumer;

import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import ReIW.tiny.cloneAny.function.Consumers;
import ReIW.tiny.cloneAny.pojo.UnboundFormalTypeParameterException;

public class MethodSignatureParser extends SlotLikeSignatureParser {

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
					.forEach(t -> argCons.accept(newSlotLike(null, null, t.getDescriptor())));
			retCons.accept(newSlotLike(null, null, Type.getReturnType(descriptor).getDescriptor()));
		}
	}

	@Override
	protected SlotValue newSlotLike(final String wildcard, final String typeParam, final String descriptor) {
		return new SlotValue(wildcard, typeParam, descriptor);
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
