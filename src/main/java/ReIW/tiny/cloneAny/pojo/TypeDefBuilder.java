package ReIW.tiny.cloneAny.pojo;

import static ReIW.tiny.cloneAny.pojo.AccessEntry.ACE_CTOR_ARG;
import static ReIW.tiny.cloneAny.pojo.AccessEntry.ACE_FIELD;
import static ReIW.tiny.cloneAny.pojo.AccessEntry.ACE_FINAL_FIELD;
import static ReIW.tiny.cloneAny.pojo.AccessEntry.ACE_PROP_GET;
import static ReIW.tiny.cloneAny.pojo.AccessEntry.ACE_PROP_SET;

import java.io.IOException;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import ReIW.tiny.cloneAny.asm7.DefaultClassVisitor;

final class TypeDefBuilder extends DefaultClassVisitor {

	static TypeDef createTypeDef(final String className) {
		try {
			final TypeDefBuilder decl = new TypeDefBuilder();
			new ClassReader(className).accept(decl, 0);
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
		if ((access & Opcodes.ACC_PUBLIC) == 0) {
			throw new UnsupportedOperationException("Class shoud have public scope.");
		}
		typeDef = new TypeDef(name, superName, TypeSlotBuilder.createTypeSlot(signature));
	}

	@Override
	public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
		if (myOwn(access)) {
			// public で not final なインスタンスフィールドだけ
			int type = (access & Opcodes.ACC_FINAL) == 0 ? ACE_FIELD : ACE_FINAL_FIELD;
			FieldSignatureParser.parse(descriptor, signature, slot -> {
				typeDef.access.add(new AccessEntry(type, name, slot, null));
			});
		}
		return null;
	}

	@Override
	public MethodVisitor visitMethod(final int access, final String name, final String descriptor,
			final String signature, String[] exceptions) {
		if (myOwn(access)) {
			if (name.contentEquals("<init>")) {
				return MethodSignatureParser.parameterParserVisitor(descriptor, signature, (paramName, slot) -> {
					typeDef.access.add(new AccessEntry(ACE_CTOR_ARG, paramName, slot, descriptor));
				});
			} else {
				try {
					if (MethodUtil.isGetter(name, descriptor)) {
						MethodSignatureParser.parseArgumentsAndReturn(descriptor, signature, slot -> {
							// nop
						}, slot -> {
							final String propName = MethodUtil.getPropertyName(name);
							typeDef.access.add(new AccessEntry(ACE_PROP_GET, propName, slot, name));
						});
					} else if (MethodUtil.isSetter(name, descriptor)) {
						MethodSignatureParser.parseArgumentsAndReturn(descriptor, signature, slot -> {
							final String propName = MethodUtil.getPropertyName(name);
							typeDef.access.add(new AccessEntry(ACE_PROP_SET, propName, slot, name));
						}, slot -> {
							// nop
						});
					}

				} catch (UnboundFormalTypeParameterException e) {
					// 型引数が定義されてるので無視する
				}
			}
		}
		return null;
	}

	private static boolean myOwn(int access) {
		// public で
		// instance で
		// コンパイラが生成したものじゃない
		return ((access & Opcodes.ACC_PUBLIC) != 0 && (access & Opcodes.ACC_STATIC) == 0
				&& (access & Opcodes.ACC_SYNTHETIC) == 0);
	}

}