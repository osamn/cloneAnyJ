package ReIW.tiny.cloneAny.pojo;

import java.util.stream.Stream;

public interface Accessor {

	enum Type {
		Field, ReadonlyField, Get, Set, LumpSet,
	}

	static class ParamInfo {
		public final String name;
		public final Slot slot;
		public ParamInfo(String name, Slot slot) {
			this.name = name;
			this.slot = slot;
		}
	}

	Type getType();

	boolean canRead();

	boolean canWrite();

	String getOwner();

	String getName();

	String getRel();

	String getDescriptor();

	Slot getSlot();

	Stream<ParamInfo> parameters();
	
}
