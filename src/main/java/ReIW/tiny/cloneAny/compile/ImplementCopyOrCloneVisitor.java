package ReIW.tiny.cloneAny.compile;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;

import java.util.stream.Stream;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import ReIW.tiny.cloneAny.asm7.DefaultClassVisitor;

final class ImplementCopyOrCloneVisitor extends DefaultClassVisitor {
	private final OperandGenerator operands;

	ImplementCopyOrCloneVisitor(final OperandGenerator operands, final ClassVisitor cv) {
		super(cv);
		this.operands = operands;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
			String[] exceptions) {
		if (name.contentEquals("copyOrClone")) {
			final MethodVisitor mv = super.visitMethod(access, name, descriptor, null, exceptions);
			mv.visitCode();
			
			
			
			mv.visitVarInsn(ALOAD, 2);
			mv.visitInsn(ARETURN);
			mv.visitEnd();
			mv.visitMaxs(1, 3);
			return null;
		} else {
			return super.visitMethod(access, name, descriptor, signature, exceptions);
		}
	}

}
