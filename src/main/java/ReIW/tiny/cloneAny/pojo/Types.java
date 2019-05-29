package ReIW.tiny.cloneAny.pojo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.WeakHashMap;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import ReIW.tiny.cloneAny.asm7.DefaultClassVisitor;
import ReIW.tiny.cloneAny.asm7.DefaultSignatureVisitor;
import ReIW.tiny.cloneAny.util.AccessorUtil;

public final class Types {

	public static final int ACC_FIELD = 1;
	public static final int ACC_FINAL_FIELD = 2;
	public static final int ACC_CTOR_ARG = 3;
	public static final int ACC_PROP_GET = 4;
	public static final int ACC_PROP_SET = 5;

	// TODO ConcurrentHashMap と Reference つかってなんとかできないもんか
	private static final Map<String, TypeDef> hive = Collections.synchronizedMap(new WeakHashMap<>());

	public static List<TypeDef> getTypeDefChain(final String className) {
		ArrayList<TypeDef> list = new ArrayList<>();
		TypeDef t = hive.computeIfAbsent(className, Types::createTypeDef);
		list.add(t);
		while (t.superName != null) {
			t = hive.computeIfAbsent(t.superName, Types::createTypeDef);
			list.add(t);
		}
		return list;
	}

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

	static TypeSlot createTypeSlot(final String signature, final String superName) {
		if (signature == null) {
			return new TypeSlot(new ArrayList<Slot>(0), new Slot("@", superName));
		} else {
			final TypeSignatureParser parser = new TypeSignatureParser();
			new SignatureReader(signature).accept(parser);
			return new TypeSlot(parser.formalSlots, parser.supersSlots.get(0));
		}

	}

	private final static class TypeSignatureParser extends DefaultSignatureVisitor {

		private final ArrayList<Slot> formalSlots = new ArrayList<>(5);
		private final ArrayList<Slot> supersSlots = new ArrayList<>(1);

		private final Stack<Slot> stack = new Stack<>();
		private String typeParam;

		private ArrayList<Slot> activeSlots = formalSlots;

		@Override
		public SignatureVisitor visitSuperclass() {
			activeSlots = supersSlots;
			typeParam = "@";
			return super.visitSuperclass();
		}

		@Override
		public void visitFormalTypeParameter(String name) {
			typeParam = name;
		}

		@Override
		public void visitClassType(String name) {
			stack.push(new Slot(typeParam, name));
		}

		@Override
		public SignatureVisitor visitTypeArgument(char wildcard) {
			typeParam = String.valueOf(wildcard);
			return super.visitTypeArgument(wildcard);
		}

		@Override
		public void visitTypeVariable(String name) {
			stack.peek().slotList.add(new Slot(name, null));
		}

		@Override
		public void visitEnd() {
			Slot slot = stack.pop();
			if (stack.isEmpty()) {
				activeSlots.add(slot);
			} else {
				stack.peek().slotList.add(slot);
			}
		}

	}

	private static final class TypeDefBuilder extends DefaultClassVisitor {

		private TypeDef typeDef;

		@Override
		public void visit(int version, int access, String name, String signature, String superName,
				String[] interfaces) {
			final TypeSlot slot = createTypeSlot(signature, superName);
			typeDef = new TypeDef(name, superName, slot);
		}

		@Override
		public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
			if ((access & Opcodes.ACC_PUBLIC) != 0 && (access & Opcodes.ACC_STATIC) == 0) {
				if ((access & Opcodes.ACC_FINAL) == 0) {
					// public で not final なインスタンスフィールドだけ
					FieldSignatureParser.accept(descriptor, signature, slot -> {
						typeDef.access.add(new AccessEntry(Types.ACC_FIELD, name, slot, null));
					});
				} else {
					// final な場合は ctor のパラメタ候補だけどここでは何もしないよ
				}
			}
			return null;
		}

		@Override
		public MethodVisitor visitMethod(final int access, final String name, final String descriptor,
				final String signature, String[] exceptions) {
			if ((access & Opcodes.ACC_PUBLIC) != 0 && (access & Opcodes.ACC_STATIC) == 0) {
				if (name.contentEquals("<init>")) {
					return MethodSignatureParser.acceptCtor(descriptor, signature, (paramName, slot) -> {
						typeDef.access.add(new AccessEntry(Types.ACC_CTOR_ARG, paramName, slot, descriptor));
					});
				} else {
					final String propName = AccessorUtil.getPropertyName(name);
					if (AccessorUtil.isGetter(name, descriptor)) {
						MethodSignatureParser.acceptGet(descriptor, signature, slot -> {
							typeDef.access.add(new AccessEntry(Types.ACC_PROP_GET, propName, slot, name));
						});
					} else if (AccessorUtil.isSetter(name, descriptor)) {
						MethodSignatureParser.acceptSet(descriptor, signature, slot -> {
							typeDef.access.add(new AccessEntry(Types.ACC_PROP_SET, propName, slot, name));
						});
					}
				}
			}
			return null;
		}
	}
}
