package ReIW.tiny.cloneAny.pojo;

import org.objectweb.asm.Type;

public final class PropertyUtil {

	private PropertyUtil() {
	}

	public static final String getPropertyName(final String methodName) {
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

	public static boolean isGetter(final String name, final String descriptor) {
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

	public static boolean isSetter(final String name, final String descriptor) {
		if (name.startsWith("set") && name.length() > 3) {
			final Type m = Type.getMethodType(descriptor);
			return m.getArgumentTypes().length == 1 && m.getReturnType() == Type.VOID_TYPE;
		}
		return false;
	}

	public static boolean isMapGet(final String name, final String descriptor) {
		if (name.contentEquals("get")) {
			final Type m = Type.getMethodType(descriptor);
			final Type[] args = m.getArgumentTypes();
			// java.util.Map<K,V> をアクセサとして抽出対象にするのは K が明に String の時だけとする
			return args.length == 1 && args[0].getDescriptor().contentEquals("Ljava/lang/String;")
					&& m.getReturnType() != Type.VOID_TYPE;
		}
		return false;
	}

	public static boolean isMapPut(final String name, final String descriptor) {
		if (name.contentEquals("put")) {
			final Type m = Type.getMethodType(descriptor);
			final Type[] args = m.getArgumentTypes();
			// java.util.Map<K,V> をアクセサとして抽出対象にするのは K が明に String の時だけとする
			return args.length == 2 && args[0].getDescriptor().contentEquals("Ljava/lang/String;")
					&& m.getReturnType() != Type.VOID_TYPE;
		}
		return false;
	}

}
