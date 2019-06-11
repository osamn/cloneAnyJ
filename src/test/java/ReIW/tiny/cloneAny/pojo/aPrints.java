package ReIW.tiny.cloneAny.pojo;

import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

public class aPrints {

	public static class Base<A, B, C, D, E> {

	}

	public static class Sample<b extends List<? super Integer>, d > extends Base<CharSequence, b, Integer, d, Long> {

	}

	public static void main(String[] argv) {
		TypeDef def = TypeDefBuilder.createTypeDef(Sample.class.getName());
		System.out.println(def.typeSlot);
		System.out.println(def.superType.typeSlot);
		
	}

	static void printSlots(Class<?> clazz) throws Exception {
		new ClassReader(clazz.getName()).accept(new ClassVisitor(Opcodes.ASM7) {
			@Override
			public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
				if ((access & Opcodes.ACC_SYNTHETIC) == 0) {
					System.out.println("Field:" + name);
					FieldSignatureParser.parse(descriptor, signature, slot -> System.out.println(slot));
				}
				return null;
			}

			@Override
			public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
					String[] exceptions) {
				if ((access & Opcodes.ACC_SYNTHETIC) == 0) {
					System.out.println("Method:" + name + descriptor);
					MethodSignatureParser.parseArgumentsAndReturn(descriptor, signature,
							slot -> System.out.println("param::" + slot),
							slot -> System.out.println("return::" + slot));
				}
				return null;
			}

			@Override
			public void visit(int version, int access, String name, String signature, String superName,
					String[] interfaces) {
				System.out.println(TypeSlotBuilder.createTypeSlot(signature));
			}
		}, 0);

	}

	static void printSignatures(Class<?> clazz) throws Exception {
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

	public static class PrintSignature extends SignatureVisitor {
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

}
