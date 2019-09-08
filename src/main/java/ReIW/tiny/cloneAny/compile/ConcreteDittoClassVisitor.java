package ReIW.tiny.cloneAny.compile;

import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.RETURN;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import ReIW.tiny.cloneAny.asm7.DefaultClassVisitor;
import ReIW.tiny.cloneAny.utils.AccessFlag;

public class ConcreteDittoClassVisitor extends DefaultClassVisitor {

	private static final String dittoBase = Type.getInternalName(AbstractDitto.class);
	private final String className;

	ConcreteDittoClassVisitor(final String className, final ClassVisitor cv) {
		super(cv);
		this.className = className;
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		super.visit(version, AccessFlag.with(access).unset(ACC_ABSTRACT).set(ACC_SYNTHETIC).value(), className, null,
				dittoBase, null);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
			String[] exceptions) {
		if (AccessFlag.isAbstract(access)) {
			return super.visitMethod(AccessFlag.with(access).unset(ACC_ABSTRACT).set(ACC_SYNTHETIC).value(), name,
					descriptor, signature, exceptions);
		} else if (name.contentEquals("<init>") && descriptor.contentEquals("()V")) {
			MethodVisitor mv = super.visitMethod(AccessFlag.with(access).set(ACC_SYNTHETIC).value(), name, descriptor,
					signature, exceptions);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, dittoBase, "<init>", "()V", false);
			mv.visitInsn(RETURN);
			mv.visitEnd();
			mv.visitMaxs(1, 1);
			return null;
		} else {
			return null;
		}
	}
}
