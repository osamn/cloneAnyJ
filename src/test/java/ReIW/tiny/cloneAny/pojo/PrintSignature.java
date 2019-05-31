package ReIW.tiny.cloneAny.pojo;

import java.io.IOException;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

public class PrintSignature extends SignatureVisitor {
	public static class Sample {
		
	}

	public static void main(String[] args) throws Exception {
		readSig(Sample.class);
	}

	public static void readSig(Class<?> clazz) throws IOException {
		new ClassReader(clazz.getName()).accept(new ClassVisitor(Opcodes.ASM7) {

			@Override
			public void visit(int version, int access, String name, String signature, String superName,
					String[] interfaces) {
				if (signature != null) {
					System.out.println(":: Class " + name);
					System.out.println(":: " + signature);
					new SignatureReader(signature).accept(new PrintSignature());
					System.out.println();
				}
			}

			@Override
			public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
				if (signature != null) {
					System.out.println(":: Field " + name);
					new SignatureReader(signature).acceptType(new PrintSignature());
					System.out.println();
				}
				return null;
			}

			@Override
			public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
					String[] exceptions) {
				if (signature != null) {
					System.out.println(":: Method " + name);
					new SignatureReader(signature).accept(new PrintSignature());
					System.out.println();
				}
				return null;
			}

		}, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
	}

	public void visitFormalTypeParameter(String name) {
		System.out.println("visitFormalTypeParameter(" + name + ")");
		super.visitFormalTypeParameter(name);
	}

	public SignatureVisitor visitClassBound() {
		System.out.println("visitClassBound()");
		return super.visitClassBound();
	}

	public SignatureVisitor visitInterfaceBound() {
		System.out.println("visitInterfaceBound()");
		return super.visitInterfaceBound();
	}

	public SignatureVisitor visitSuperclass() {
		System.out.println("visitSuperclass()");
		return super.visitSuperclass();
	}

	public SignatureVisitor visitInterface() {
		System.out.println("visitSuperclass()");
		return super.visitInterface();
	}

	public SignatureVisitor visitParameterType() {
		System.out.println("visitParameterType()");
		return super.visitParameterType();
	}

	public SignatureVisitor visitReturnType() {
		System.out.println("visitReturnType()");
		return super.visitReturnType();
	}

	public SignatureVisitor visitExceptionType() {
		System.out.println("visitExceptionType()");
		return super.visitExceptionType();
	}

	public void visitBaseType(char descriptor) {
		System.out.println("visitBaseType(" + descriptor + ")");
		super.visitBaseType(descriptor);
	}

	public void visitTypeVariable(String name) {
		System.out.println("visitTypeVariable(" + name + ")");
		super.visitTypeVariable(name);
	}

	public SignatureVisitor visitArrayType() {
		System.out.println("visitArrayType()");
		return super.visitArrayType();
	}

	public void visitClassType(String name) {
		System.out.println("visitClassType(" + name + ")");
		super.visitClassType(name);
	}

	public void visitInnerClassType(String name) {
		System.out.println("visitInnerClassType(" + name + ")");
		super.visitInnerClassType(name);
	}

	public void visitTypeArgument() {
		System.out.println("visitTypeArgument()");
		super.visitTypeArgument();
	}

	public SignatureVisitor visitTypeArgument(char wildcard) {
		System.out.println("visitTypeArgument(" + wildcard + ")");
		return super.visitTypeArgument(wildcard);
	}

	public void visitEnd() {
		System.out.println("visitEnd()");
		super.visitEnd();
	}

	public PrintSignature() {
		super(Opcodes.ASM7);
	}

}
