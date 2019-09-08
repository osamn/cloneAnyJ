package ReIW.tiny.cloneAny.compile;

import java.util.List;

import ReIW.tiny.cloneAny.pojo.Accessor;

public class Operand {

	public List<Accessor> lhsList;

	public Accessor rhs;
	
	public boolean loopSet() {
		return rhs.getSlot().indexed();
	}
}
