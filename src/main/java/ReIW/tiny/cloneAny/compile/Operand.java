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
	}

	class LoadLhs implements Operand {
		// ALOAD 1
	}

	class LoadRhs implements Operand {
		// ALOAD 2
	}

	class StoreRhs implements Operand {
		// ASTORE 2
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
		// INVOKEVIRTUAL owner name desctiptor
	}

	class InvokeSet implements Operand {
	}

	class FieldGet implements Operand {
	}

	class FieldSet implements Operand {
	}

	class Convert implements Operand {
		final Slot lhs;
		final Slot rhs;

		// box unbox とか cast とか
		// Ditto とるコードに展開されるよ
		Convert(Slot lhs, Slot rhs) {
			this.lhs = lhs;
			this.rhs = rhs;
		}
	}

	class ListToList implements Operand {
		Convert conv;

	}

	class ListToArray implements Operand {
		Convert conv;
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

}
