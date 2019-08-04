package ReIW.tiny.cloneAny.impl;

import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ARETURN;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import ReIW.tiny.cloneAny.asm7.DefaultClassVisitor;

public class ConcreteGetRhsClassVisitor extends DefaultClassVisitor {

	private final String value;

	ConcreteGetRhsClassVisitor( final String value, final ClassVisitor cv) {
		super(cv);
		this.value = value;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
			String[] exceptions) {
		if (name.contentEquals("getRhsClass")) {
			final MethodVisitor mv = super.visitMethod(access & ~ACC_ABSTRACT | ACC_SYNTHETIC, name, descriptor,
					signature, exceptions);
			mv.visitCode();
			mv.visitLdcInsn(value);
			mv.visitInsn(ARETURN);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
			return null;
		}
		return super.visitMethod(access, name, descriptor, signature, exceptions);
	}
}
