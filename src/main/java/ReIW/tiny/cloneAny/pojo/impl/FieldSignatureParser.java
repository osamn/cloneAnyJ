package ReIW.tiny.cloneAny.pojo.impl;

import java.util.function.Consumer;

import org.objectweb.asm.signature.SignatureReader;

class FieldSignatureParser extends SlotLikeSignatureParser<SlotValue> {

	FieldSignatureParser(Consumer<SlotValue> cons) {
		super.slotCons = cons;
	}

	void parse(final String descriptor, final String signature) {
		if (signature == null) {
			slotCons.accept(newSlotLike(null, descriptor));
		} else {
			new SignatureReader(signature == null ? descriptor : signature).accept(this);
		}
	}

	@Override
	protected SlotValue newSlotLike(final String typeParam, final String descriptor) {
		return new SlotValue(typeParam, descriptor);
	}

	// Field に formal parameter はつけられないので
	// visitFormalTypeParameter(String name)
	// を override する必要はないよ

}
