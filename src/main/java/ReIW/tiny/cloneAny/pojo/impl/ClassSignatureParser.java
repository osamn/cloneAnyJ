package ReIW.tiny.cloneAny.pojo.impl;

import java.util.Arrays;
import java.util.function.Consumer;

import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import ReIW.tiny.cloneAny.pojo.Slot;

final class ClassSignatureParser extends SlotLikeSignatureVisitor<Slot> {

	private final Consumer<Slot> supers;

	ClassSignatureParser(final Consumer<Slot> formals, final Consumer<Slot> supers) {
		this.consumer = formals;
		this.supers = supers;
	}

	void parse(final String superName, final String[] interfaces, final String signature) {
		if (signature == null) {
			if (superName == null) {
				return;
			}
			// signature がない場合は、自クラスも継承元も non generic だし
			// superName とか interfaces の中身とかに配列 '[' がくることはない
			// はずなんで new Slot で
			supers.accept(new Slot(null, Type.getObjectType(superName).getDescriptor()));
			if (interfaces != null) {
				Arrays.stream(interfaces)
						.map(intfName -> new Slot(null, Type.getObjectType(intfName).getDescriptor()))
						.forEach(supers);
			}
		} else {
			new SignatureReader(signature).accept(this);
		}
	}

	@Override
	protected Slot newSlotLike(final String typeParam, final String descriptor) {
		return new Slot(typeParam, descriptor);
	}

	@Override
	public SignatureVisitor visitSuperclass() {
		consumer = supers;
		return super.visitSuperclass();
	}

}
