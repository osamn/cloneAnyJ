package ReIW.tiny.cloneAny;

public class AccessElement {

	public static final int FIELD = 0;
	public static final int FINAL_FIELD = 1;
	public static final int CTOR_ARG = 2;
	public static final int PROP_GET = 3;
	public static final int PROP_SET = 4;

	AccessElement(int elementType, String name, String typeName, String rel, String signature) {
		this.elementType = elementType;
		this.name = name;
		this.typeName = typeName;
		this.canGet = (elementType == FIELD || elementType == FINAL_FIELD || elementType == PROP_GET);
		this.canSet = (elementType == FIELD || elementType == CTOR_ARG || elementType == PROP_SET);
		this.signature = signature;
		this.rel = rel;
	}

	public final int elementType;
	public final String name;
	public final String typeName;
	public final boolean canGet;
	public final boolean canSet;
	public final String rel;
	public final String signature;

	@Override
	public String toString() {
		return "[" + elementType + " " + (canGet ? "r" : "-") + (canSet ? "w" : "-") + ": Name=" + name + ", Type="
				+ typeName + ", Signature=" + signature + ", Rel=" + rel + "]";
	}

}
