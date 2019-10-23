package ReIW.tiny.cloneAny.pojo.impl;

import java.util.function.Consumer;

import org.objectweb.asm.signature.SignatureReader;

class FieldSignatureParser extends SlotLikeSignatureParser {

	FieldSignatureParser(Consumer<SlotValue> fieldCons) {
		super.slotCons = fieldCons;
	}

	void parse(final String descriptor, final String signature) {
		if (signature != null) {
			// generic なとき
			new SignatureReader(signature).accept(this);
		} else if (descriptor.startsWith("[")) {
			// 配列の時
			new SignatureReader(descriptor).accept(this);
		} else {
			// それ以外は子要素をもたないのでそのまま作る
			slotCons.accept(newSlotLike(null, null, descriptor));
		}
	}

	@Override
	protected SlotValue newSlotLike(final String wildcard, final String typeParam, final String descriptor) {
		return new SlotValue(wildcard, typeParam, descriptor);
	}

	// Field に formal parameter はつけられないので
	// visitFormalTypeParameter(String name)
	// を処理する必要はないよ

}
