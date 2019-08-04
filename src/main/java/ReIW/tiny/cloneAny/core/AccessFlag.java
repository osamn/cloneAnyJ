package ReIW.tiny.cloneAny.core;

import org.objectweb.asm.Opcodes;

public final class AccessFlag {
	private int access;

	private AccessFlag(final int access) {
		this.access = access;
	}

	public static AccessFlag with(final int access) {
		return new AccessFlag(access);
	}

	public AccessFlag set(final int flag) {
		access = access | flag;
		return this;
	}

	public AccessFlag unset(final int flag) {
		access = access & ~flag;
		return this;
	}

	public int value() {
		return access;
	}

	public static boolean isAbstract(int access) {
		return (access & Opcodes.ACC_ABSTRACT) != 0;
	}

	public static boolean isFinal(int access) {
		return (access & Opcodes.ACC_FINAL) != 0;
	}

	public static boolean isInterface(int access) {
		return (access & Opcodes.ACC_INTERFACE) != 0;
	}

	public static boolean isNative(int access) {
		return (access & Opcodes.ACC_NATIVE) != 0;
	}

	public static boolean isPrivate(int access) {
		return (access & Opcodes.ACC_PRIVATE) != 0;
	}

	public static boolean isProtected(int access) {
		return (access & Opcodes.ACC_PROTECTED) != 0;
	}

	public static boolean isPublic(int access) {
		return (access & Opcodes.ACC_PUBLIC) != 0;
	}

	public static boolean isStatic(int access) {
		return (access & Opcodes.ACC_STATIC) != 0;
	}

	public static boolean isStrict(int access) {
		return (access & Opcodes.ACC_STRICT) != 0;
	}

	public static boolean isSynchronized(int access) {
		return (access & Opcodes.ACC_SYNCHRONIZED) != 0;
	}

	public static boolean isTransient(int access) {
		return (access & Opcodes.ACC_TRANSIENT) != 0;
	}

	public static boolean isVolatile(int access) {
		return (access & Opcodes.ACC_VOLATILE) != 0;
	}

	public static boolean isSynthetic(int access) {
		return (access & Opcodes.ACC_SYNTHETIC) != 0;
	}

}
