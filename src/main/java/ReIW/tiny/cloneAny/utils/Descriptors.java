package ReIW.tiny.cloneAny.utils;

import org.objectweb.asm.Type;

// TODO あとでテスト
public interface Descriptors {

	static Class<?> toClass(final String descriptor) {
		// LOOKUPSWITCH なんであんまり変わらんかもしれんけど、なんとなく出現頻度順にしておく
		switch (descriptor.charAt(0)) {
		case 'L':
			try {
				return Class.forName(Type.getType(descriptor).getClassName());
			} catch (ClassNotFoundException e) {
				// class のバイナリベースから呼び出されるのでありえないはず
				throw new IllegalArgumentException(e);
			}
		case 'I':
			return int.class;
		case 'Z':
			return boolean.class;
		case 'C':
			return char.class;
		case 'B':
			return byte.class;
		case 'D':
			return double.class;
		case 'J':
			return long.class;
		case 'S':
			return short.class;
		case 'F':
			return float.class;
		case 'V':
			return void.class;
		case '[':
			final String desc = descriptor.substring(1);
			return toClass(desc).arrayType();
		case '(':
			// 基本的に method descriptor の '(' は例外におとしておく
		default:
			throw new IllegalArgumentException();
		}
	}

	static boolean isBoxingType(String descriptor) {
		if (descriptor == null) {
			return false;
		}
		if (descriptor.startsWith("Ljava/lang/")) {
			if (descriptor.contentEquals("Ljava/lang/String;")) {
				return false;
			}
			return descriptor.contentEquals("Ljava/lang/Integer;") || descriptor.contentEquals("Ljava/lang/Boolean;")
					|| descriptor.contentEquals("Ljava/lang/Character;") || descriptor.contentEquals("Ljava/lang/Byte;")
					|| descriptor.contentEquals("Ljava/lang/Double;") || descriptor.contentEquals("Ljava/lang/Long;")
					|| descriptor.contentEquals("Ljava/lang/Short;") || descriptor.contentEquals("Ljava/lang/Float;");
		}
		return false;
	}
}
