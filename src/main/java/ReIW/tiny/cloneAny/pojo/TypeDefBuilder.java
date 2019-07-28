package ReIW.tiny.cloneAny.pojo;

import static ReIW.tiny.cloneAny.pojo.AccessEntry.ACE_CTOR_ARG;
import static ReIW.tiny.cloneAny.pojo.AccessEntry.ACE_FIELD;
import static ReIW.tiny.cloneAny.pojo.AccessEntry.ACE_FINAL_FIELD;
import static ReIW.tiny.cloneAny.pojo.AccessEntry.ACE_PROP_GET;
import static ReIW.tiny.cloneAny.pojo.AccessEntry.ACE_PROP_SET;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import ReIW.tiny.cloneAny.asm7.DefaultClassVisitor;

final class TypeDefBuilder {

	// いつまでも hive 抱えててもいかんので GC で回収されるように弱参照をもっておく
	private static WeakReference<TypeDefBuilder> cacheRef = new WeakReference<>(new TypeDefBuilder());

	// TypeDef#complete で super を作るときに使う
	// なんで className は internalName になってるよ
	static TypeDef createTypeDef(final String className) {
		try {
			final Class<?> clazz = Class.forName(className.replace('/', '.'));
			return createTypeDef(clazz);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	static TypeDef createTypeDef(final Class<?> clazz) {
		TypeDefBuilder builder;
		synchronized (TypeDefBuilder.class) {
			builder = cacheRef.get();
			if (builder == null) {
				builder = new TypeDefBuilder();
				cacheRef = new WeakReference<>(builder);
			}
		}
		return builder.compute(clazz);
	}

	private final ConcurrentHashMap<Class<?>, TypeAccessDef> hive = new ConcurrentHashMap<>();

	private TypeDef compute(final Class<?> clazz) {
		TypeDef type = (TypeDef) hive.computeIfAbsent(clazz, (final Class<?> src) -> {
			final String cnm = Type.getInternalName(src);
			try {
				final TypeDefCreator decl = new TypeDefCreator(Map.class.isAssignableFrom(clazz));
				new ClassReader(cnm).accept(decl, 0);
				return decl.typeDef;
			} catch (IOException e) {
				// まあ、これはありえないから適当に
				throw new RuntimeException(e);
			}
		});
		// TypeDef#complete は super の TypeDef をとるんで computeIfAbsent の中でやると
		// IllegalStateException: Recursive update って叱られる
		// なんでそとにだしとく
		type.complete();
		return type;
	}

	private static class TypeDefCreator extends DefaultClassVisitor {

		private final boolean instanceOfMap;

		private TypeDefCreator(boolean instanceOfMap) {
			this.instanceOfMap = instanceOfMap;
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
				final int type = (access & Opcodes.ACC_FINAL) == 0 ? ACE_FIELD : ACE_FINAL_FIELD;
				FieldSignatureParser.parse(descriptor, signature, slot -> {
					typeDef.access.add(new AccessEntry(type, name, slot, name));
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
				} else if (name.contentEquals("get")) {
					if (instanceOfMap) {
						MethodSignatureParser.parseArgumentsAndReturn(descriptor, signature, MethodSignatureParser::nop,
								slot -> {
									typeDef.access.add(new AccessEntry(ACE_PROP_GET, "*", slot, name));
								});
					}
				} else if (name.contentEquals("put")) {
					if (instanceOfMap) {
						final List<Slot> params = new ArrayList<>();
						MethodSignatureParser.parseArgumentsAndReturn(descriptor, signature, slot -> {
							params.add(slot);
						}, MethodSignatureParser::nop);
						if (params.size() == 2) {
							// 引数が２つの put なんで追加する
							typeDef.access.add(new AccessEntry(ACE_PROP_SET, "*", params.get(1), name));
						}
					}
				} else {
					try {
						if (PropertyUtil.isGetter(name, descriptor)) {
							MethodSignatureParser.parseArgumentsAndReturn(descriptor, signature,
									MethodSignatureParser::nop, slot -> {
										final String propName = PropertyUtil.getPropertyName(name);
										typeDef.access.add(new AccessEntry(ACE_PROP_GET, propName, slot, name));
									});
						} else if (PropertyUtil.isSetter(name, descriptor)) {
							MethodSignatureParser.parseArgumentsAndReturn(descriptor, signature, slot -> {
								final String propName = PropertyUtil.getPropertyName(name);
								typeDef.access.add(new AccessEntry(ACE_PROP_SET, propName, slot, name));
							}, MethodSignatureParser::nop);
						}
					} catch (UnboundFormalTypeParameterException e) {
						// プロパティっぽいけど、メソッド自体に型パラメタがあるので無視する
						// public <X> X getHoge()
						// みたいなやつ
					}
				}
			}
			return null;
		}

	}

	private static boolean myOwn(int access) {
		return ((access & Opcodes.ACC_PUBLIC) != 0 // public で
				&& (access & Opcodes.ACC_STATIC) == 0 // instance で
				&& (access & Opcodes.ACC_ABSTRACT) == 0 // not abstract で
				&& (access & Opcodes.ACC_SYNTHETIC) == 0); // コンパイラが生成したものじゃない
	}

}