package ReIW.tiny.cloneAny.pojo.impl;

import java.util.Arrays;
import java.util.function.Consumer;

import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

class ClassSignatureParser extends SlotLikeSignatureParser<SlotValue> {

	final Consumer<SlotValue> superCons;

	ClassSignatureParser(Consumer<SlotValue> thisCons, Consumer<SlotValue> superCons) {
		this.superCons = superCons;
		slotCons = thisCons;
	}

	void parse(final String superName, final String[] interfaces, final String signature) {
		if (signature == null) {
			// signature がない場合は、自クラスも継承元も non generic だし
			// superName とか interfaces の中身とかに配列 '[' がくることはない
			// はずなんで new Slot で
			superCons.accept(new SlotValue(null, Type.getObjectType(superName).getDescriptor()));
			if (interfaces != null) {
				Arrays.stream(interfaces)
						.map(intfName -> new SlotValue(null, Type.getObjectType(intfName).getDescriptor()))
						.forEach(superCons);
			}
		} else {
			new SignatureReader(signature).accept(this);
		}
	}

	@Override
	protected SlotValue newSlotLike(final String typeParam, final String descriptor) {
		return new SlotValue(typeParam, descriptor);
	}

	@Override
	public SignatureVisitor visitSuperclass() {
		slotCons = superCons;
		return super.visitSuperclass();
	}

}
