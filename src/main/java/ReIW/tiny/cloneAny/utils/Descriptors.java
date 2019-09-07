package ReIW.tiny.cloneAny.utils;

import org.objectweb.asm.Type;

public interface Descriptors {

	static Class<?> toClass(final String descriptor) {
		switch (descriptor.charAt(0)) {
		case 'V':
			return void.class;
		case 'Z':
			return boolean.class;
		case 'C':
			return char.class;
		case 'B':
			return byte.class;
		case 'S':
			return short.class;
		case 'I':
			return int.class;
		case 'F':
			return float.class;
		case 'J':
			return long.class;
		case 'D':
			return double.class;
		case 'L':
			try {
				return Class.forName(Type.getType(descriptor).getClassName());
			} catch (ClassNotFoundException e) {
				// class のバイナリベースから呼び出されるのでありえないはず
				throw new IllegalArgumentException(e);
			}
		case '[':
			final String desc = descriptor.substring(1);
			return toClass(desc).arrayType();
		case '(':
			// 基本的に method descriptor の '(' は例外におとしておく
		default:
			throw new IllegalArgumentException();
		}
	}
}
