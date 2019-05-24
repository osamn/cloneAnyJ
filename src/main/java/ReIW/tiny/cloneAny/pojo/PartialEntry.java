package ReIW.tiny.cloneAny.pojo;

import static ReIW.tiny.cloneAny.pojo.Types.ACC_CTOR_ARG;
import static ReIW.tiny.cloneAny.pojo.Types.ACC_FIELD;
import static ReIW.tiny.cloneAny.pojo.Types.ACC_FINAL_FIELD;
import static ReIW.tiny.cloneAny.pojo.Types.ACC_PROP_GET;
import static ReIW.tiny.cloneAny.pojo.Types.ACC_PROP_SET;

class PartialEntry {

	PartialEntry(final int elementType, final String name, final Slot slot, final String rel) {
		this.elementType = elementType;
		this.name = name;
		this.slot = slot;
		this.canGet = (elementType == ACC_FIELD || elementType == ACC_FINAL_FIELD || elementType == ACC_PROP_GET);
		this.canSet = (elementType == ACC_FIELD || elementType == ACC_CTOR_ARG || elementType == ACC_PROP_SET);
		this.rel = rel;
	}

	final int elementType;
	final String name; // property name
	final Slot slot;
	final boolean canGet;
	final boolean canSet;
	// getter setter name 今のところ対応してないけど BeanInfo で変わる場合もあるんで
	final String rel;

}
