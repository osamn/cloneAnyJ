package ReIW.tiny.cloneAny;

import org.objectweb.asm.Type;

public final class AccessorUtil {

	private AccessorUtil() {
	}

	public static final String getPropertyName(String methodName) {
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

	// setter getter は indexed property には対応しないよ
	// Bean の実装次第でエラーになったりするとおもうんで

	public static boolean isGetter(String name, String descriptor) {
		Type t = Type.getMethodType(descriptor);
		if (t.getArgumentTypes().length != 0) {
			return false;
		}
		return (name.startsWith("get") && !name.contentEquals("get") && t.getReturnType() != Type.VOID_TYPE)
				|| (name.startsWith("is") && !name.contentEquals("is") && t.getReturnType() == Type.BOOLEAN_TYPE);
	}

	public static boolean isSetter(String name, String descriptor) {
		Type t = Type.getMethodType(descriptor);
		if (t.getArgumentTypes().length != 1) {
			return false;
		}
		return name.startsWith("set") && !name.contentEquals("set") && t.getReturnType() == Type.VOID_TYPE;
	}

}
