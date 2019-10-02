package ReIW.tiny.cloneAny.pojo.impl;

import java.util.Map;
import java.util.stream.Stream;

import ReIW.tiny.cloneAny.pojo.Accessor;
import ReIW.tiny.cloneAny.pojo.Slot;

public final class SingleSlotAccessor implements SlotAccessor {

	private final Accessor.Kind type;
	private final String owner;
	private final String name;
	private final String rel;
	private final String descriptor;

	private final Slot slot;

	SingleSlotAccessor(final Accessor.Kind type, final String owner, final String name, final String rel,
			final String descriptor, final Slot slot) {
		this.type = type;
		this.owner = owner;
		this.name = name;
		this.rel = rel;
		this.descriptor = descriptor;
		this.slot = slot;
	}

	@Override
	public Accessor.Kind getType() {
		return type;
	}

	@Override
	public boolean canRead() {
		return type == Accessor.Kind.Get || type == Accessor.Kind.Field || type == Accessor.Kind.ReadonlyField;
	}

	@Override
	public boolean canWrite() {
		return type == Accessor.Kind.Set || type == Accessor.Kind.Field || type == Accessor.Kind.LumpSet;
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
	public Stream<SlotInfo> slotInfo() {
		return Stream.of(new SlotInfo(name,  slot));
	}

	@Override
	public SlotAccessor chown(final String newOwner) {
		if (this.owner.contentEquals(owner)) {
			return this;
		}
		return new SingleSlotAccessor(this.type, newOwner, this.name, this.rel, this.descriptor, this.slot);
	}

	@Override
	public SlotAccessor rebind(final Map<String, String> binds) {
		if (binds.size() == 0) {
			return this;
		}
		return new SingleSlotAccessor(this.type, this.owner, this.name, this.rel, this.descriptor,
				this.slot.rebind(binds));
	}

}