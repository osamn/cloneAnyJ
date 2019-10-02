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

	static boolean isBoxingType(final String descriptor) {
		if (descriptor == null) {
			return false;
		}
		if (descriptor.startsWith("Ljava/lang/")) {
			if (descriptor.contentEquals("Ljava/lang/String;")) {
				return false;
			}
			return descriptor.contentEquals("Ljava/lang/Integer;") // I
					|| descriptor.contentEquals("Ljava/lang/Boolean;") // Z
					|| descriptor.contentEquals("Ljava/lang/Character;") // C
					|| descriptor.contentEquals("Ljava/lang/Byte;") // B
					|| descriptor.contentEquals("Ljava/lang/Double;") // D
					|| descriptor.contentEquals("Ljava/lang/Long;") // J
					|| descriptor.contentEquals("Ljava/lang/Short;") // S
					|| descriptor.contentEquals("Ljava/lang/Float;") // F
			;
		}
		return false;
	}

	static boolean isPrimitiveType(final String descriptor) {
		return descriptor.contentEquals("I") // int
				|| descriptor.contentEquals("Z") // boolean
				|| descriptor.contentEquals("C") // char
				|| descriptor.contentEquals("B") // byte
				|| descriptor.contentEquals("D") // double
				|| descriptor.contentEquals("J") // long
				|| descriptor.contentEquals("S") // short
				|| descriptor.contentEquals("F") // float
		;
	}

	static String getBoxingType(final String primitiveDesc) {
		final char c = primitiveDesc.charAt(0);
		switch (c) {
		case 'I':
			return "Ljava/lang/Integer;";
		case 'Z':
			return "Ljava/lang/Boolean;";
		case 'C':
			return "Ljava/lang/Character;";
		case 'B':
			return "Ljava/lang/Byte;";
		case 'D':
			return "Ljava/lang/Double;";
		case 'J':
			return "Ljava/lang/Long;";
		case 'S':
			return "Ljava/lang/Short;";
		case 'F':
			return "Ljava/lang/Float;";
		default:
			return null;
		}
	}

	static String toInternalName(final String descriptor) {
		return Type.getType(descriptor).getInternalName();
	}
}
