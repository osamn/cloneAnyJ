package ReIW.tiny.cloneAny.pojo;

import java.util.ArrayList;

class TypeDef {

	final String superType;
	final TypeSlot slot;
	final ArrayList<PartialEntry> access = new ArrayList<>();

	TypeDef(final String superType, final TypeSlot slot) {
		this.superType = superType;
		this.slot = slot;
	}
}
