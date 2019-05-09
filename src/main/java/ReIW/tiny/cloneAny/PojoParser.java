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

	private final Consumer<AccessibleElement> cons;

	public PojoParser(Consumer<AccessibleElement> cons) {
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
				new PojoParser(PojoParser.this.cons).parse(superName);
			}
		}

		@Override
		public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
			if ((access & Opcodes.ACC_PUBLIC) != 0 && (access & Opcodes.ACC_INTERFACE) != 0) {
				final int type = (access & Opcodes.ACC_FINAL) == 0 ? AccessibleElement.FIELD : AccessibleElement.FINAL_FIELD;
				PojoParser.this.cons.accept(new AccessibleElement(type, name, descriptor, null));
			}
			return null;
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
				String[] exceptions) {
			if ((access & Opcodes.ACC_PUBLIC) != 0 && (access & Opcodes.ACC_INTERFACE) != 0) {
				if (name.contentEquals("<init>")) {
					return new CollectCtorParamVisitor(Type.getArgumentTypes(descriptor));
				} else if (AccessorUtil.isGetter(name, descriptor)) {
					PojoParser.this.cons.accept(new AccessibleElement(AccessibleElement.PROP_GET,
							AccessorUtil.getPropertyName(name), descriptor, name));
				} else if (AccessorUtil.isSetter(name, descriptor)) {
					PojoParser.this.cons.accept(new AccessibleElement(AccessibleElement.PROP_SET,
							AccessorUtil.getPropertyName(name), descriptor, name));
				}
			}
			return null;
		}

	}

	private final class CollectCtorParamVisitor extends MethodVisitor {

		private final Type[] arguments;
		private int pos = 0;

		public CollectCtorParamVisitor(Type[] arguments) {
			super(ASM7);
			this.arguments = arguments;
		}

		@Override
		public void visitParameter(String name, int access) {
			Type t = arguments[pos];
			PojoParser.this.cons.accept(new AccessibleElement(AccessibleElement.CTOR_ARG, name, t.getInternalName(), null));
			pos++;
		}

	}
}
