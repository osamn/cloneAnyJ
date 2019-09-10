package ReIW.tiny.cloneAny.pojo.impl;

import java.util.Arrays;
import java.util.function.Consumer;

import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import ReIW.tiny.cloneAny.pojo.Slot;

final class ClassSignatureParser extends SignatureParser {

	private final Consumer<Slot> supers;

	ClassSignatureParser(final Consumer<Slot> formals, final Consumer<Slot> supers) {
		this.cons = formals;
		this.supers = supers;
	}

	void parse(final String superName, final String[] interfaces, final String signature) {
		if (signature == null) {
			// signature がない場合は、自クラスも継承元も non generic なので
			// ルートは配列になることがないのでそのまま Slot 作る
			supers.accept(new Slot(null, Type.getObjectType(superName).getDescriptor()));
			if (interfaces != null) {
				Arrays.stream(interfaces).map(intf -> new Slot(null, Type.getObjectType(intf).getDescriptor()))
						.forEach(supers);
			}
		} else {
			new SignatureReader(signature).accept(this);
		}
	}

	@Override
	public SignatureVisitor visitSuperclass() {
		cons = supers;
		return super.visitSuperclass();
	}

}
