package ReIW.tiny.cloneAny.impl;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;

import java.util.stream.Stream;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import ReIW.tiny.cloneAny.asm7.DefaultClassVisitor;
import ReIW.tiny.cloneAny.pojo.Operand;

final class ImplementCopyOrCloneVisitor extends DefaultClassVisitor {
	private final Stream<Operand> ops;

	ImplementCopyOrCloneVisitor(final Stream<Operand> ops, final ClassVisitor cv) {
		super(cv);
		this.ops = ops;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
			String[] exceptions) {
		if (name.contentEquals("copyOrClone")) {
			final MethodVisitor mv = super.visitMethod(access, name, descriptor, null, exceptions);
			mv.visitCode();
			ops.forEach(op -> {
				emitOperand(mv, op);
			});
			mv.visitVarInsn(ALOAD, 2);
			mv.visitInsn(ARETURN);
			mv.visitEnd();
			mv.visitMaxs(1, 3);
			return null;
		} else {
			return super.visitMethod(access, name, descriptor, signature, exceptions);
		}
	}

	private void emitOperand(final MethodVisitor mv, final Operand op) {
		mv.visitLabel(new Label());
		// FIXME オペランドの埋め込み
		// Map で "*" の場合は get でループ開始 put でループ終了にする

	}
}
