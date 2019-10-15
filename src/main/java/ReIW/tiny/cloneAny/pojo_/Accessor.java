package ReIW.tiny.cloneAny.pojo_;

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

	/** Field/Method の持ち主の internalName */
	String getOwner();

	/** プロパティ名 */
	String getName();

	/** Field/Method 名 */
	String getRel();

	/** Field/Method の descriptor */
	// Field の場合は asSingle した Slot#getTypeDescriptor とおなじなはず
	String getDescriptor();

	Stream<SlotInfo> slotInfo();

	static Slot asSingle(Accessor acc) {
		if (acc.getType() == Kind.LumpSet) {
			throw new IllegalArgumentException();
		}
		return acc.slotInfo().findFirst().get().slot;
	}

}
