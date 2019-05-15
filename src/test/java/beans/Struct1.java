package beans;

import java.util.ArrayList;

public class Struct1 {

	public Struct1(int intVal, String strVal, ArrayList<String> stringList) {
		this.intVal = intVal;
		this.strVal = strVal;
		this.stringList = stringList;
	}

	public final int intVal;
	public final String strVal;
	public final ArrayList<String> stringList;
}
