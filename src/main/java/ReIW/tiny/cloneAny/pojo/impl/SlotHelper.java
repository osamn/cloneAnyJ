package ReIW.tiny.cloneAny.pojo.impl;

import ReIW.tiny.cloneAny.pojo.Slot;

final class SlotHelper {

	private SlotHelper() {
	}

	/** 配列を考慮して Slot をつくる */
	static Slot buildSlot(final String descriptor) {
		if (descriptor.startsWith("[")) {
			String desc = descriptor.substring(1);
			final Slot root = new Slot(null, "[");
			Slot curr = root;
			while (desc.startsWith("[")) {
				Slot s = new Slot(null, "[");
				curr.slotList.add(s);
				curr = s;
				desc = desc.substring(1);
			}
			curr.slotList.add(new Slot(null, desc));
			return root;
		} else {
			return new Slot(null, descriptor);
		}
	}

}
