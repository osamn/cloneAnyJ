package ReIW.tiny.cloneAny.asm7;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureVisitor;

public class DefaultSignatureVisitor extends SignatureVisitor {
	public DefaultSignatureVisitor() {
		super(Opcodes.ASM7);
	}

}
