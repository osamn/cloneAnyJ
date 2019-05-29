package ReIW.tiny.cloneAny.pojo;

import java.util.ArrayList;
import java.util.List;

final class TypeDef {

	final String name;
	final String superName;
	final TypeSlot slot;
	final List<AccessEntry> access;

	TypeDef(final String name ,final String superName, final TypeSlot slot) {
		this.name = name;
		this.superName = superName;
		this.slot = slot;
		access = new ArrayList<>();
	}

}
