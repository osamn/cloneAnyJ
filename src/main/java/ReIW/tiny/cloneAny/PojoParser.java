package ReIW.tiny.cloneAny;

import static org.objectweb.asm.Opcodes.ASM7;

import java.io.IOException;
import java.util.function.Consumer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class PojoParser {

	private final Consumer<AccessElement> cons;

	public PojoParser(Consumer<AccessElement> cons) {
		this.cons = cons;
	}

	public void parse(String beanClassName) {
		try {
			ClassReader cr = new ClassReader(beanClassName);
			cr.accept(new CollectPropClassVisitor(), ClassReader.SKIP_CODE);
		} catch (IOException e) {
			throw new AssemblyException(e);
		}
		// TODO BeanInfo をみにいって property を上書きしたる
	}

	private final class CollectPropClassVisitor extends ClassVisitor {

		public CollectPropClassVisitor() {
			super(ASM7);
		}

		@Override
		public void visit(int version, int access, String name, String signature, String superName,
				String[] interfaces) {
			if (superName != null) {
				// 親クラスのプロパティを見に行く
				new PojoParser(PojoParser.this.cons).parse(superName.replace('.', '/'));
			}
		}

		@Override
		public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
			if ((access & Opcodes.ACC_PUBLIC) != 0 && (access & Opcodes.ACC_STATIC) == 0) {
				final int type = (access & Opcodes.ACC_FINAL) == 0 ? AccessElement.FIELD : AccessElement.FINAL_FIELD;
				PojoParser.this.cons.accept(new AccessElement(type, name, descriptor, signature, null));
			}
			return null;
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
				String[] exceptions) {
			if ((access & Opcodes.ACC_PUBLIC) != 0 && (access & Opcodes.ACC_STATIC) == 0) {
				if (name.contentEquals("<init>")) {
					return new CollectCtorParamVisitor(descriptor, signature);
				} else if (AccessorUtil.isGetter(name, descriptor)) {
					PojoParser.this.cons
							.accept(new AccessElement(AccessElement.PROP_GET, AccessorUtil.getPropertyName(name),
									Type.getReturnType(descriptor).getInternalName(), name, signature));
				} else if (AccessorUtil.isSetter(name, descriptor)) {
					PojoParser.this.cons
							.accept(new AccessElement(AccessElement.PROP_SET, AccessorUtil.getPropertyName(name),
									Type.getArgumentTypes(descriptor)[0].getInternalName(), name, signature));
				}
			}
			return null;
		}

	}

	private final class CollectCtorParamVisitor extends MethodVisitor {

		public CollectCtorParamVisitor(String descriptor, String signature) {
			super(ASM7);
		}

		@Override
		public void visitParameter(String name, int access) {
			// TODO
		}

	}
}
