package ReIW.tiny.cloneAny.compile;

import ReIW.tiny.cloneAny.pojo.Slot;

public interface Operand {

	class New implements Operand {
		public final String owner;

		New(final String owner) {
			this.owner = owner;
		}
		// NEW
		// DUP
	}

	class Init implements Operand {
		public final String owner;
		public final String desctiptor;

		Init(final String owner, final String descriptor) {
			this.owner = owner;
			this.desctiptor = descriptor;
		}
		// INVOKESPECIAL owner <init> desctiptor
		// if (rhs == null) のなかでの場合は
		// ASTORE 2
		// も必要だけど、これは if 作る側でやる
	}

	class InvokeGet implements Operand {
		public final String owner;
		public final String name;
		public final String desctiptor;

		InvokeGet(final String owner, final String name, final String desctiptor) {
			this.owner = owner;
			this.name = name;
			this.desctiptor = desctiptor;
		}
		// ALOAD 1
		// INVOKEVIRTUAL owner name desctiptor
	}

	class InvokeSet implements Operand {
		// ALOAD 2
		// INVOKEVIRTUAL owner name desctiptor
	}

	class FieldGet implements Operand {
		// ALOAD 1
	}

	class FieldSet implements Operand {
		// ALOAD 2
	}

	class Convert implements Operand {
		final Slot lhs;
		final Slot rhs;

		// box unbox とか cast とか
		// Ditto とるコードに展開されるよ
		// box unbox は intValue とか Integer.valueOf とかになるんであとで考える
		Convert(Slot lhs, Slot rhs) {
			this.lhs = lhs;
			this.rhs = rhs;
		}
	}

	class ListToList implements Operand {
		Convert conv;
		// 右側を default ctor でつくる
		// で左の size() loop
	}

	class ListToArray implements Operand {
		Convert conv;
		// 左の size()
		// 右の配列 NEWARRAY
		// 左の size() loop
	}

	class ArrayToList implements Operand {
		Convert conv;

	}

	class ArrayToArray implements Operand {
		Convert conv;

	}

	class MapToMap implements Operand {
		Convert conv;

	}

	class ToStringConv implements Operand {
		
		// CharSequence -> String 専用
	}
}
