package ReIW.tiny.cloneAny.compile;

public interface Operand {

	class LoadLhs implements Operand {
	}

	class LoadRhs implements Operand {
	}

	class StoreRhs implements Operand {

	}

	class New implements Operand {

	}

	class Dup implements Operand {

	}

	class InvokeSpecial implements Operand {

	}

	class InvokeGet implements Operand {

	}

	class InvokeSet implements Operand {

	}

	class FieldGet implements Operand {

	}

	class FieldSet implements Operand {

	}

	class MapGet implements Operand {

	}

	class MapPut implements Operand {

	}

	class StartIndexLoop implements Operand {

	}

	class EndIndexLoop implements Operand {

	}

	class StartKeyLoop implements Operand {

	}

	class EndKeyLoop implements Operand {

	}

	class ArrayGet implements Operand {

	}

	class ArraySet implements Operand {

	}

	class ListGet implements Operand {

	}

	class ListSet implements Operand {

	}

	class Convert implements Operand {
		// Ditto とるコードに展開されるよ

	}

}
