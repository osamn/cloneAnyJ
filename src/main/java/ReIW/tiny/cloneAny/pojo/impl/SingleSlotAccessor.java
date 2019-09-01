package ReIW.tiny.cloneAny.pojo.impl;

import java.util.Map;

import ReIW.tiny.cloneAny.pojo.Accessor;
import ReIW.tiny.cloneAny.pojo.Slot;

public final class SingleSlotAccessor implements SlotAccessor {

	private final Accessor.Type type;
	private final String owner;
	private final String name;
	private final String descriptor;
	private final boolean readable;
	private final boolean writable;

	public final Slot slot;

	SingleSlotAccessor(final Accessor.Type type, final String owner, final String name, final String descriptor,
			final Slot slot) {
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