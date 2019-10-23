package ReIW.tiny.cloneAny.compile;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

/**
 * SignatureVisitor の呼ばれ方を確認するのにつかうもの
 */
public class TraceSignature extends SignatureVisitor {

	public static void main(String[] args) {
		final String signature = "Ljava/util/List<*>;";
		new SignatureReader(signature).accept(new TraceSignature());
	}

	public TraceSignature() {
		super(Opcodes.ASM7);
	}

	@Override
	public void visitFormalTypeParameter(String name) {
		System.out.println("visitFormalTypeParameter(" + name + ")");
		super.visitFormalTypeParameter(name);
	}

	@Override
	public SignatureVisitor visitClassBound() {
		System.out.println("visitClassBound()");
		return super.visitClassBound();
	}

	@Override
	public SignatureVisitor visitInterfaceBound() {
		System.out.println("visitInterfaceBound()");
		return super.visitInterfaceBound();
	}

	@Override
	public SignatureVisitor visitSuperclass() {
		System.out.println("visitSuperclass()");
		return super.visitSuperclass();
	}

	@Override
	public SignatureVisitor visitInterface() {
		System.out.println("visitInterface()");
		return super.visitInterface();
	}

	@Override
	public SignatureVisitor visitParameterType() {
		System.out.println("visitParameterType()");
		return super.visitParameterType();
	}

	@Override
	public SignatureVisitor visitReturnType() {
		System.out.println("visitReturnType()");
		return super.visitReturnType();
	}

	@Override
	public SignatureVisitor visitExceptionType() {
		System.out.println("visitExceptionType()");
		return super.visitExceptionType();
	}

	@Override
	public void visitBaseType(char descriptor) {
		System.out.println("visitBaseType(" + descriptor + ")");
		super.visitBaseType(descriptor);
	}

	@Override
	public void visitTypeVariable(String name) {
		System.out.println("visitTypeVariable(" + name + ")");
		super.visitTypeVariable(name);
	}

	@Override
	public SignatureVisitor visitArrayType() {
		System.out.println("visitArrayType()");
		return super.visitArrayType();
	}

	@Override
	public void visitClassType(String name) {
		System.out.println("visitClassType(" + name + ")");
		super.visitClassType(name);
	}

	@Override
	public void visitInnerClassType(String name) {
		System.out.println("visitInnerClassType(" + name + ")");
		super.visitInnerClassType(name);
	}

	@Override
	public void visitTypeArgument() {
		System.out.println("visitTypeArgument()");
		super.visitTypeArgument();
	}

	@Override
	public SignatureVisitor visitTypeArgument(char wildcard) {
		System.out.println("visitTypeArgument(" + wildcard + ")");
		return super.visitTypeArgument(wildcard);
	}

	@Override
	public void visitEnd() {
		System.out.println("visitEnd()");
		super.visitEnd();
	}

}
