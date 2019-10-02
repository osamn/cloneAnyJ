package ReIW.tiny.cloneAny.pojo.impl;

import java.util.Arrays;
import java.util.function.Consumer;

import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import ReIW.tiny.cloneAny.pojo.Slot;

final class ClassSignatureParser extends Slot.SlotSignatureVisitor {

	private final Consumer<Slot> supers;

	ClassSignatureParser(final Consumer<Slot> formals, final Consumer<Slot> supers) {
		this.consumer = formals;
		this.supers = supers;
	}

	void parse(final String superName, final String[] interfaces, final String signature) {
		if (signature == null) {
			// signature がない場合は、自クラスも継承元も non generic だし
			// getSlot でシグネチャなしでつくる
			supers.accept(Slot.getSlot(null, Type.getObjectType(superName).getDescriptor(), null));
			if (interfaces != null) {
				Arrays.stream(interfaces).map(intf -> Slot.getSlot(null, Type.getObjectType(intf).getDescriptor(), null))
						.forEach(supers);
			}
		} else {
			new SignatureReader(signature).accept(this);
		}
	}

	@Override
	public SignatureVisitor visitSuperclass() {
		consumer = supers;
		return super.visitSuperclass();
	}

}
