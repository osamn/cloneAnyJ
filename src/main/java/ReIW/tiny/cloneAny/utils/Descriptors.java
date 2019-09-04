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
				// TODO native 呼び出しがはいるので、オーバーヘッド確認する
				// 場合によっては map にしたほうがいいかもしれんので
				return Class.forName(Type.getType(descriptor).getClassName());
			} catch (ClassNotFoundException e) {
				// class のバイナリベースから呼び出されるのでありえないはず
				throw new IllegalArgumentException(e);
			}
		default:
			// 基本的に配列 '[' メソッド '(' は来ないはずなので例外にしとく
			throw new IllegalArgumentException();
		}
	}
}
