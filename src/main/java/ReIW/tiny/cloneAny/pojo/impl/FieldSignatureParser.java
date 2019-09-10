package ReIW.tiny.cloneAny.pojo.impl;

import java.util.function.Consumer;

import org.objectweb.asm.signature.SignatureReader;

import ReIW.tiny.cloneAny.pojo.Slot;
import ReIW.tiny.cloneAny.pojo.UnboundFormalTypeParameterException;

final class FieldSignatureParser extends SignatureParser {

	FieldSignatureParser(final Consumer<Slot> cons) {
		this.cons = cons;
	}

	void parse(final String descriptor, final String signature) {
		if (signature == null) {
			cons.accept(new Slot(null, descriptor));
		} else {
			new SignatureReader(signature).accept(this);
		}
	}

	@Override
	public void visitFormalTypeParameter(String name) {
		// Field には独立した型パラメタはふつうに書けないはずなんでありえないっす
		throw new UnboundFormalTypeParameterException();
	}

}
