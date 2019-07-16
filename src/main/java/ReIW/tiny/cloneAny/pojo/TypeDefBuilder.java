package ReIW.tiny.cloneAny.pojo;

import static ReIW.tiny.cloneAny.pojo.AccessEntry.ACE_CTOR_ARG;
import static ReIW.tiny.cloneAny.pojo.AccessEntry.ACE_FIELD;
import static ReIW.tiny.cloneAny.pojo.AccessEntry.ACE_FINAL_FIELD;
import static ReIW.tiny.cloneAny.pojo.AccessEntry.ACE_PROP_GET;
import static ReIW.tiny.cloneAny.pojo.AccessEntry.ACE_PROP_SET;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import ReIW.tiny.cloneAny.asm7.DefaultClassVisitor;
import ReIW.tiny.cloneAny.utils.MethodUtil;

public final class TypeDefBuilder {

	private static WeakReference<TypeDefBuilder> cacheRef = new WeakReference<>(new TypeDefBuilder());

	public static TypeDef createTypeDef(final String className) {
		if (className.contentEquals("java/lang/Object")) {
			return null;
		}
		TypeDefBuilder builder;
		synchronized (TypeDefBuilder.class) {
			builder = cacheRef.get();
			if (builder == null) {
				builder = new TypeDefBuilder();
				cacheRef = new WeakReference<>(builder);
			}
		}
		return builder.computeIfAbsent(className);
	}

	private final ConcurrentHashMap<String, TypeDef> hive = new ConcurrentHashMap<>();

	private TypeDef computeIfAbsent(final String className) {
		TypeDef type = hive.computeIfAbsent(className, (final String src) -> {
			try {
				final TypeDefCreator decl = new TypeDefCreator();
				new ClassReader(src).accept(decl, 0);
				return decl.typeDef;
			} catch (IOException e) {
				throw new RuntimeException("Unhandled", e);
			}
		});
		// TypeDef#complete は super の TypeDef をとるんで computeIfAbsent の中でやると
		// IllegalStateException: Recursive update って叱られる
		// なんでそとにだしとく
		type.complete();
		return type;
	}

	private static class TypeDefCreator extends DefaultClassVisitor {

		private TypeDefCreator() {
		}

		private TypeDef typeDef;

		@Override
		public void visit(int version, int access, String name, String signature, String superName,
				String[] interfaces) {
			if ((access & Opcodes.ACC_PUBLIC) == 0) {
				throw new UnsupportedOperationException("Class shoud have public scope.");
			}
			final TypeSlot typeSlot = TypeSlotBuilder.createTypeSlot(signature);
			typeDef = new TypeDef(name, superName, typeSlot);
		}

		@Override
		public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
			if (myOwn(access)) {
				// final なものは読み取り専用になるよ
				// その場合、コンストラクタ引数に設定可能なものとしてあるかもね
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
					typeDef.ctors.add(descriptor);
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

	}

	private static boolean myOwn(int access) {
		// public で
		// instance で
		// コンパイラが生成したものじゃない
		return ((access & Opcodes.ACC_PUBLIC) != 0 && (access & Opcodes.ACC_STATIC) == 0
				&& (access & Opcodes.ACC_SYNTHETIC) == 0);
	}

}