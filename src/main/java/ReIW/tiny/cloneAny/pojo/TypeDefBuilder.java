package ReIW.tiny.cloneAny.pojo;

import java.io.IOException;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import ReIW.tiny.cloneAny.asm7.DefaultClassVisitor;
import ReIW.tiny.cloneAny.util.AccessorUtil;

final class TypeDefBuilder extends DefaultClassVisitor {

	static TypeDef createTypeDef(final String className) {
		try {
			final TypeDefBuilder decl = new TypeDefBuilder();
			new ClassReader(className).accept(decl,
					ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
			return decl.typeDef;
		} catch (IOException e) {
			throw new RuntimeException("Unhandled", e);
		}
	}

	private TypeDef typeDef;

	private TypeDefBuilder() {
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		typeDef = new TypeDef(name, superName, TypeSlotBuilder.createTypeSlot(signature));
	}

	@Override
	public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
		if ((access & Opcodes.ACC_PUBLIC) != 0 && (access & Opcodes.ACC_STATIC) == 0) {
			// public で not final なインスタンスフィールドだけ
			int type = (access & Opcodes.ACC_FINAL) == 0 ? Types.ACC_FIELD : Types.ACC_FINAL_FIELD;
			FieldSignatureParser.parse(descriptor, signature, slot -> {
				typeDef.access.add(new AccessEntry(type, name, slot, null));
			});
		}
		return null;
	}

	@Override
	public MethodVisitor visitMethod(final int access, final String name, final String descriptor,
			final String signature, String[] exceptions) {
		if ((access & Opcodes.ACC_PUBLIC) != 0 && (access & Opcodes.ACC_STATIC) == 0) {
			if (name.contentEquals("<init>")) {
				return MethodSignatureParser.parameterParserVisitor(descriptor, signature, (paramName, slot) -> {
					typeDef.access.add(new AccessEntry(Types.ACC_CTOR_ARG, paramName, slot, descriptor));
				});
			} else {
				final String propName = AccessorUtil.getPropertyName(name);
				if (AccessorUtil.isGetter(name, descriptor)) {
					MethodSignatureParser.parseArgumentsAndReturn(descriptor, signature, slot -> {
						typeDef.access.add(new AccessEntry(Types.ACC_PROP_GET, propName, slot, name));
					}, null);
				} else if (AccessorUtil.isSetter(name, descriptor)) {
					MethodSignatureParser.parseArgumentsAndReturn(descriptor, signature, null, slot -> {
						typeDef.access.add(new AccessEntry(Types.ACC_PROP_GET, propName, slot, name));
					});
				}
			}
		}
		return null;
	}
}