package ReIW.tiny.cloneAny.pojo;

import java.util.stream.Stream;

public interface Accessor {

	enum Kind {
		Field, ReadonlyField, Get, Set, LumpSet,
	}

	static class SlotInfo {
		public final String param;
		public final Slot slot;

		public SlotInfo(final String paramName, final Slot slot) {
			this.param = paramName;
			this.slot = slot;
		}
	}

	Kind getType();

	boolean canRead();

	boolean canWrite();

	String getOwner();

	String getName();

	String getRel();

	String getDescriptor();

	Stream<SlotInfo> slotInfo();

	static SlotInfo asSingle(Accessor acc) {
		if (acc.getType() == Kind.LumpSet) {
			throw new IllegalArgumentException();
		}
		return acc.slotInfo().findFirst().get();
	}

}
