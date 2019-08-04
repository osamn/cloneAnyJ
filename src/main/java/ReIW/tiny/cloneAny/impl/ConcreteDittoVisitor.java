package ReIW.tiny.cloneAny.impl;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.RETURN;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import ReIW.tiny.cloneAny.asm7.DefaultClassVisitor;

public class ConcreteDittoVisitor extends DefaultClassVisitor {

	private static final String dittoBase = AbstractDitto.class.getName().replace('.', '/');
	private final String className;

	ConcreteDittoVisitor(final String className, final ClassVisitor cv) {
		super(cv);
		this.className = className;
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		super.visit(version, access & ~ACC_ABSTRACT | ACC_SYNTHETIC, className, null, dittoBase, null);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
			String[] exceptions) {
		if ((access & ACC_FINAL) != 0) {
			return null;
		}
		if (name.contentEquals("<init>") && descriptor.contentEquals("()V")) {
			MethodVisitor mv = super.visitMethod(access | ACC_SYNTHETIC, name, descriptor, signature, exceptions);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, dittoBase, "<init>", "()V", false);
			mv.visitInsn(RETURN);
			mv.visitEnd();
			mv.visitMaxs(1, 1);
			return null;
		}
		return super.visitMethod(access, name, descriptor, signature, exceptions);
	}
}
