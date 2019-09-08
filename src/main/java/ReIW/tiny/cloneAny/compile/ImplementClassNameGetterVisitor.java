package ReIW.tiny.cloneAny.compile;

import static org.objectweb.asm.Opcodes.ARETURN;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import ReIW.tiny.cloneAny.asm7.DefaultClassVisitor;

final class ImplementClassNameGetterVisitor extends DefaultClassVisitor {

	private final String lhs;
	private final String rhs;

	ImplementClassNameGetterVisitor(final String lhs, final String rhs, final ClassVisitor cv) {
		super(cv);
		this.lhs = lhs.replace('/', '.');
		this.rhs = rhs.replace('/', '.');
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
			String[] exceptions) {
		if (name.contentEquals("getLhsClass")) {
			final MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
			mv.visitCode();
			mv.visitLdcInsn(lhs);
			mv.visitInsn(ARETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
			return null;
		} else if (name.contentEquals("getRhsClass")) {
			final MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
			mv.visitCode();
			mv.visitLdcInsn(rhs);
			mv.visitInsn(ARETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
			return null;
		} else {
			return super.visitMethod(access, name, descriptor, signature, exceptions);
		}
	}
}
