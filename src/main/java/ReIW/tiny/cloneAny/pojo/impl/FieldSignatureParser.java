package ReIW.tiny.cloneAny.pojo.impl;

import java.util.function.Consumer;

import org.objectweb.asm.signature.SignatureReader;

import ReIW.tiny.cloneAny.pojo.Slot;

class FieldSignatureParser extends SlotLikeSignatureVisitor<Slot> {

	FieldSignatureParser(Consumer<Slot> cons) {
		super.consumer = cons;
	}

	void parse(final String descriptor, final String signature) {
		new SignatureReader(signature == null ? descriptor : signature).accept(this);
	}

	@Override
	protected Slot newSlotLike(final String typeParam, final String descriptor) {
		return new Slot(typeParam, descriptor);
	}

	// Field に formal parameter はつけられないのでとくに例外にするとかは必要ないので
	// visitFormalTypeParameter(String name)
	// を override する必要はないよ
}
