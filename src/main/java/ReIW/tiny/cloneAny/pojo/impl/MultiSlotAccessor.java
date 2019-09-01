package ReIW.tiny.cloneAny.pojo.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ReIW.tiny.cloneAny.pojo.Accessor;
import ReIW.tiny.cloneAny.pojo.Slot;

public final class MultiSlotAccessor implements SlotAccessor {

	private final Accessor.Type type;
	private final String owner;
	private final String name;
	private final String descriptor;

	public final List<String> names = new ArrayList<>();
	public final List<Slot> slots = new ArrayList<>();

	MultiSlotAccessor(String owner, String name, String descriptor) {
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
	public String getRel() {
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