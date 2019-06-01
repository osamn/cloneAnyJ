package ReIW.tiny.cloneAny.pojo;

final class AccessEntry {

	static final int ACE_FIELD = 1;
	static final int ACE_FINAL_FIELD = 2;
	static final int ACE_CTOR_ARG = 3;
	static final int ACE_PROP_GET = 4;
	static final int ACE_PROP_SET = 5;

	AccessEntry(final int elementType, final String name, final Slot slot, final String rel) {
		this.elementType = elementType;
		this.name = name;
		this.slot = slot;
		this.canGet = (elementType == ACE_FIELD || elementType == ACE_FINAL_FIELD || elementType == ACE_PROP_GET);
		this.canSet = (elementType == ACE_FIELD || elementType == ACE_CTOR_ARG || elementType == ACE_PROP_SET);
		this.rel = rel;
	}

	final int elementType;
	final String name; // property name
	final Slot slot;
	final boolean canGet;
	final boolean canSet;
	// getter setter name 今のところ対応してないけど BeanInfo で変わる場合もあるんで
	// あと ctor の descriptor とか
	final String rel;

}
