package ReIW.tiny.cloneAny.pojo;

import java.util.List;

final class TypeSlot {

	final List<Slot> formalSlots;
	final Slot superSlot;
	final List<Slot> interfaceSlot;

	TypeSlot(List<Slot> formal, List<Slot> superSlot) {
		this.formalSlots = formal;
		this.superSlot = superSlot.get(0);
		this.interfaceSlot = superSlot.subList(1, superSlot.size());
	}

	@Override
	public String toString() {
		final StringBuilder buf = new StringBuilder();
		buf.append("TypeSlot [");
		for (Slot s : formalSlots) {
			buf.append('\n');
			buf.append(s.toString());
		}
		buf.append("::");
		buf.append(superSlot.toString());
		for (Slot s : interfaceSlot) {
			buf.append('\n');
			buf.append(s.toString());
		}
		buf.append(']');
		return buf.toString();
	}

}
