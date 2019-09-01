package ReIW.tiny.cloneAny.pojo.impl;

import java.util.ArrayList;
import java.util.Map;

import ReIW.tiny.cloneAny.pojo.Accessor;
import ReIW.tiny.cloneAny.pojo.Slot;

interface SlotAccessor extends Accessor {

	SlotAccessor chown(String owner);

	SlotAccessor rebind(Map<String, String> binds);

	class SingleSlotAccessor implements SlotAccessor {

		private final Accessor.Type type;
		private final String owner;
		private final String name;
		private final String descriptor;
		private final Slot slot;
		private final boolean readable;
		private final boolean writable;

		public SingleSlotAccessor(final Accessor.Type type, final String owner, final String name,
				final String descriptor, final Slot slot) {
			this.type = type;
			this.owner = owner;
			this.name = name;
			this.descriptor = descriptor;
			this.slot = slot;
			switch (type) {
			case Field:
				readable = true;
				writable = true;
				break;
			case ReadonlyField:
				readable = true;
				writable = false;
				break;
			case Get:
				readable = true;
				writable = false;
				break;
			case Set:
				readable = false;
				writable = true;
				break;
			default:
				throw new IllegalArgumentException();
			}
		}

		@Override
		public Accessor.Type getType() {
			return type;
		}

		@Override
		public boolean canRead() {
			return readable;
		}

		@Override
		public boolean canWrite() {
			return writable;
		}

		@Override
		public String getOwner() {
			return owner;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getDescriptor() {
			return descriptor;
		}

		public Slot getSlot() {
			return slot;
		}

		@Override
		public SlotAccessor chown(final String newOwner) {
			if (this.owner.contentEquals(owner)) {
				return this;
			}
			return new SingleSlotAccessor(this.type, newOwner, this.name, this.descriptor, this.slot);
		}

		@Override
		public SlotAccessor rebind(final Map<String, String> binds) {
			if (binds.size() == 0) {
				return this;
			}
			return new SingleSlotAccessor(this.type, this.owner, this.name, this.descriptor, this.slot.rebind(binds));
		}

	}

	class MultiSlotAccessor implements SlotAccessor {

		private final Accessor.Type type;
		private final String owner;
		private final String name;
		private final String descriptor;

		public final ArrayList<String> names = new ArrayList<>();
		public final ArrayList<Slot> slots = new ArrayList<>();

		public MultiSlotAccessor(String owner, String name, String descriptor) {
			this.type = Accessor.Type.LumpSet;
			this.owner = owner;
			this.name = name;
			this.descriptor = descriptor;
		}

		@Override
		public Accessor.Type getType() {
			return type;
		}

		@Override
		public boolean canRead() {
			return false;
		}

		@Override
		public boolean canWrite() {
			return true;
		}

		@Override
		public String getOwner() {
			return owner;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getDescriptor() {
			return descriptor;
		}

		@Override
		public SlotAccessor chown(String owner) {
			if (this.owner.contentEquals(owner)) {
				return this;
			}
			final MultiSlotAccessor me = new MultiSlotAccessor(this.owner, this.name, this.descriptor);
			me.names.addAll(this.names);
			me.slots.addAll(this.slots);
			return me;
		}

		@Override
		public SlotAccessor rebind(Map<String, String> binds) {
			if (binds.size() == 0) {
				return this;
			}
			final MultiSlotAccessor me = new MultiSlotAccessor(this.owner, this.name, this.descriptor);
			me.names.addAll(this.names);
			this.slots.forEach(slot -> me.slots.add(slot.rebind(binds)));
			return me;
		}

	}

}
