package ReIW.tiny.cloneAny.asm7;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

public class DefaultClassVisitor extends ClassVisitor {

	public DefaultClassVisitor() {
		super(Opcodes.ASM7);
	}
}
