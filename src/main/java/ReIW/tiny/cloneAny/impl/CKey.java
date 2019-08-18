package ReIW.tiny.cloneAny.impl;

import java.util.Objects;

import org.objectweb.asm.Type;

import ReIW.tiny.cloneAny.pojo.Slot;

public final class CKey {

	final Slot lhs;
	final Slot rhs;

	private final int hash;

	public CKey(final Class<?> lhs, final Class<?> rhs) {
		this(Slot.fromClass(lhs), Slot.fromClass(rhs));
	}

	public CKey(final Slot lhs, final Slot rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
		// 各スロットはここに来た時点でもう決定済み
		// なので hash 値は構築時ですべて決定できるはず
		hash = Objects.hash(lhs, rhs);
	}

	public String getInternalName() {
		return "$ditto$/" + slotName(lhs).replace('.', '_') + "$" + slotName(rhs).replace('.', '_');
	}

	private static String slotName(final Slot slot) {
		StringBuilder buf = new StringBuilder();
		writeSlotName(buf, slot);
		return buf.toString();
	}

	private static void writeSlotName(final StringBuilder buf, final Slot slot) {
		buf.append(Type.getType(slot.typeClass).getInternalName());
		if (slot.slotList.size() > 0) {
			for (Slot s : slot.slotList) {
				buf.append('.');
				writeSlotName(buf, s);
			}
		}
	}

	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CKey other = (CKey) obj;
		if (lhs == null) {
			if (other.lhs != null)
				return false;
		} else if (!lhs.equals(other.lhs))
			return false;
		if (rhs == null) {
			if (other.rhs != null)
				return false;
		} else if (!rhs.equals(other.rhs))
			return false;
		return true;
	}
}