package ReIW.tiny.cloneAny.pojo;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.WeakHashMap;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import ReIW.tiny.cloneAny.asm7.DefaultClassVisitor;
import ReIW.tiny.cloneAny.util.AccessorUtil;

public final class Types {

	public static final int ACC_FIELD = 1;
	public static final int ACC_FINAL_FIELD = 2;
	public static final int ACC_CTOR_ARG = 3;
	public static final int ACC_PROP_GET = 4;
	public static final int ACC_PROP_SET = 5;

	// TODO ConcurrentHashMap と Reference つかってなんとかできないもんか
	private static final Map<String, TypeDef> hive = Collections.synchronizedMap(new WeakHashMap<>());

	public static Resolver getResolver(final String className) {
		return new Resolver(getTypeChain(className));
	}

	// 親から子の順でならんでるリストを作るよ
	// ルートは Object じゃないけどね
	private static LinkedList<TypeDef> getTypeChain(final String className) {
		LinkedList<TypeDef> list = new LinkedList<>();
		TypeDef t = hive.computeIfAbsent(className, Types::createTypeDef);
		list.addFirst(t);
		while (t.superType != null) {
			t = hive.computeIfAbsent(t.superType, Types::createTypeDef);
			list.addFirst(t);
		}
		return list;
	}

	private static TypeDef createTypeDef(final String className) {
		try {
			DeclaredAccessParser decl = new DeclaredAccessParser();
			new ClassReader(className).accept(decl,
					ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
			return decl.typeDef;
		} catch (IOException e) {
			throw new RuntimeException("Unhandled", e);
		}
	}

	private static class DeclaredAccessParser extends DefaultClassVisitor {
		private TypeDef typeDef;

		@Override
		public void visit(int version, int access, String name, String signature, String superName,
				String[] interfaces) {
			typeDef = new TypeDef(superName, TypeSlot.accept(signature));
		}

		@Override
		public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
			// public なインスタンスフィールドだけみてあげる
			if ((access & Opcodes.ACC_PUBLIC) != 0 && (access & Opcodes.ACC_STATIC) == 0) {
				final int type = (access & Opcodes.ACC_FINAL) == 0 ? ACC_FIELD : ACC_FINAL_FIELD;
				typeDef.access.add(new PartialEntry(type, name, null /* FIXME slot */, null));
			}
			return null;
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
				String[] exceptions) {
			if ((access & Opcodes.ACC_PUBLIC) != 0 && (access & Opcodes.ACC_STATIC) == 0) {
				if (name.contentEquals("<init>")) {
					return null /* FIXME parameter でつくるやつ */;
				} else if (AccessorUtil.isGetter(name, descriptor)) {
					typeDef.access.add(new PartialEntry(ACC_PROP_GET, name, null /* FIXME slot */, null));
				} else if (AccessorUtil.isSetter(name, descriptor)) {
					typeDef.access.add(new PartialEntry(ACC_PROP_SET, name, null /* FIXME slot */, null));
				}
			}
			return null;
		}
	}
}
