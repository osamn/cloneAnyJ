package ReIW.tiny.cloneAny.pojo;

import java.util.ArrayList;
import java.util.List;

final class TypeDef {

	final String name;
	final String superName;
	final TypeSlot typeSlot;
	final List<AccessEntry> access;

	TypeDef(final String name ,final String superName, final TypeSlot typeSlot) {
		this.name = name;
		this.superName = superName;
		this.typeSlot = typeSlot;
		access = new ArrayList<>();
	}

}
