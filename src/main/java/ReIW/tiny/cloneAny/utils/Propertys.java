package ReIW.tiny.cloneAny.utils;

import org.objectweb.asm.Type;

public interface Propertys {

	static String getPropertyName(final String methodName) {
		int off;

		if (methodName.startsWith("set") || methodName.startsWith("get")) {
			off = 3;
		} else if (methodName.startsWith("is")) {
			off = 2;
		} else {
			return null;
		}
		if (methodName.length() == off) {
			return null;
		}

		if (methodName.length() > (off + 1)) {
			// プロパティ名の２文字目が大文字の場合は setABxxx -> ABxxx のように１文字目を小文字にしない
			char c = methodName.charAt(off + 1);
			if (Character.isUpperCase(c)) {
				return methodName.substring(off);
			}
		}

		StringBuilder buf = new StringBuilder();
		buf.append(Character.toLowerCase(methodName.charAt(off)));
		buf.append(methodName.substring(off + 1));
		return buf.toString();
	}

	static boolean isGetter(final String name, final String descriptor) {
		if (name.startsWith("get") && name.length() > 3) {
			final Type m = Type.getMethodType(descriptor);
			return m.getArgumentTypes().length == 0 && m.getReturnType() != Type.VOID_TYPE;
		}
		if (name.startsWith("is") && name.length() > 2) {
			final Type m = Type.getMethodType(descriptor);
			return m.getArgumentTypes().length == 0 && m.getReturnType() == Type.BOOLEAN_TYPE;
		}
		return false;
	}

	static boolean isSetter(final String name, final String descriptor) {
		if (name.startsWith("set") && name.length() > 3) {
			final Type m = Type.getMethodType(descriptor);
			return m.getArgumentTypes().length == 1 && m.getReturnType() == Type.VOID_TYPE;
		}
		return false;
	}

}
