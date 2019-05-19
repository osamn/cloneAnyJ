package beans;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

public class SigRd extends SignatureVisitor {

	public void visitFormalTypeParameter(String name) {
		System.out.println("visitFormalTypeParameter(" + name +")");
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

	public SigRd() {
		super(Opcodes.ASM7);
	}

	public static void main(String[] args) {
		SignatureReader sr = new SignatureReader("<T:Ljava/lang/Object;>Lbeans/Typed1<Ljava/lang/String;TT;>;");
		sr.accept(new SigRd());
		System.out.println("-----------");

		SignatureReader sr1 = new SignatureReader("<T:Ljava/lang/Object;K:Ljava/lang/Object;V:Ljava/lang/Object;M::Ljava/util/Map<TK;TV;>;L::Ljava/util/List<+TV;>;S::Ljava/util/Set<-TV;>;>Ljava/lang/Object;");
		sr1.accept(new SigRd());
		System.out.println("-----------");
		SignatureReader sr2 = new SignatureReader("Lbeans/Typed2<Lbeans/Bean1;Ljava/lang/String;Lbeans/Bean2;Ljava/util/HashMap<Ljava/lang/String;Lbeans/Bean2;>;Ljava/util/ArrayList<Lbeans/Bean2;>;Ljava/util/HashSet<Lbeans/Bean2;>;>;");
		sr2.acceptType(new SigRd());
	}
}
