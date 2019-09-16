package ReIW.tiny.cloneAny.compile;

public interface Operand {

	class LoadLhs implements Operand {
	}

	class LoadRhs implements Operand {
	}

	class StoreRhs implements Operand {
	}

	class Dup implements Operand {
	}

	class New implements Operand {
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

	class StartIndexLoop implements Operand {
		// size をとるための情報が必要
	}

	class EndIndexLoop implements Operand {
	}

	class StartKeyLoop implements Operand {
		// Map#keySet のループになる
	}

	class EndKeyLoop implements Operand {
	}

	class checkKeyExists implements Operand {
		// なかったら例外なので終了側はいらないとおも
	}

	class ifKeyExists implements Operand {
		// if の開始
	}

	class fiKeyExists implements Operand {
		// if の終了
	}

	class MapGet implements Operand {
	}

	class MapPut implements Operand {
	}

	class ArrayGet implements Operand {
	}

	class ArraySet implements Operand {
	}

	class ListGet implements Operand {
	}

	class ListSet implements Operand {
	}

	class Cast implements Operand {
	}

	class Box implements Operand {
	}

	class Unbox implements Operand {
	}

	class Convert implements Operand {
		// Ditto とるコードに展開されるよ
	}

}
