package ReIW.tiny.cloneAny.pojo

trait SlotTestHelper {

	Slot getSlot(final String descriptor, final String signature) {
		if (signature != null) {
			return new Slot.SlotBuilder(null).build(signature);
		} else {
			return new Slot.SlotBuilder(null).build(descriptor);
		}
	}

}
