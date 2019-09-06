package ReIW.tiny.cloneAny.pojo.impl;

import java.util.Map;

import ReIW.tiny.cloneAny.pojo.Accessor;
import ReIW.tiny.cloneAny.pojo.Slot;

public final class SingleSlotAccessor extends SlotAccessor {

	private final Accessor.Type type;
	private final String owner;
	private final String name;
	private final String rel;
	private final String descriptor;

	public final Slot slot;

	SingleSlotAccessor(final Accessor.Type type, final String owner, final String name, final String rel,
			final String descriptor, final Slot slot) {
		this.type = type;
		this.owner = owner;
		this.name = name;
		this.rel = rel;
		this.descriptor = descriptor;
		this.slot = slot;
	}

	@Override
	public Accessor.Type getType() {
		return type;
	}

	@Override
	public boolean canRead() {
		return type == Accessor.Type.Get || type == Accessor.Type.Field || type == Accessor.Type.ReadonlyField;
	}

	@Override
	public boolean canWrite() {
		return type == Accessor.Type.Set || type == Accessor.Type.Field || type == Accessor.Type.LumpSet;
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
	public String getRel() {
		return rel;
	}

	@Override
	public String getDescriptor() {
		return descriptor;
	}

	@Override
	SlotAccessor chown(final String newOwner) {
		if (this.owner.contentEquals(owner)) {
			return this;
		}
		return new SingleSlotAccessor(this.type, newOwner, this.name, this.rel, this.descriptor, this.slot);
	}

	@Override
	SlotAccessor rebind(final Map<String, String> binds) {
		if (binds.size() == 0) {
			return this;
		}
		return new SingleSlotAccessor(this.type, this.owner, this.name, this.rel, this.descriptor,
				this.slot.rebind(binds));
	}

}