package ReIW.tiny.cloneAny.pojo.impl;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Stream;

import ReIW.tiny.cloneAny.pojo.Accessor;
import ReIW.tiny.cloneAny.pojo.Slot;
import static ReIW.tiny.cloneAny.utils.Functions.withIndex;

public final class MultiSlotAccessor extends SlotAccessor {

	private final Accessor.Type type;
	private final String owner;
	private final String name;
	private final String rel;
	private final String descriptor;

	public final ArrayList<String> names = new ArrayList<>();

	public final ArrayList<Slot> slots = new ArrayList<>();

	MultiSlotAccessor(String owner, String name, String rel, String descriptor) {
		this.type = Accessor.Type.LumpSet;
		this.owner = owner;
		this.name = name;
		this.rel = rel;
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
		return rel;
	}

	@Override
	public String getDescriptor() {
		return descriptor;
	}

	@Override
	public Stream<SlotInfo> slotInfo() {
		return names.stream().map(withIndex((paramName, i) -> new SlotInfo(paramName, slots.get(i))));
	}

	@Override
	SlotAccessor chown(String owner) {
		if (this.owner.contentEquals(owner)) {
			return this;
		}
		final MultiSlotAccessor me = new MultiSlotAccessor(owner, this.name, this.rel, this.descriptor);
		me.names.addAll(this.names);
		me.slots.addAll(this.slots);
		return me;
	}

	@Override
	SlotAccessor rebind(Map<String, String> binds) {
		if (binds.size() == 0) {
			return this;
		}
		final MultiSlotAccessor me = new MultiSlotAccessor(this.owner, this.name, this.rel, this.descriptor);
		me.names.addAll(this.names);
		this.slots.forEach(slot -> me.slots.add(slot.rebind(binds)));
		return me;
	}

}