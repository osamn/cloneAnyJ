package ReIW.tiny.cloneAny.pojo.impl;

final class SlotValueBuilder extends SlotLikeSignatureParser {
	private SlotValue slot;

	SlotValueBuilder(final String wildcard) {
		super.wildcard = wildcard;
		super.slotCons = (val) -> {
			this.slot = val;
		};
	}

	SlotValue build(final String signature) {
		this.accept(signature);
		return slot;
	}

	@Override
	protected SlotValue newSlotLike(final String wildcard, final String typeParam, final String descriptor) {
		return new SlotValue(wildcard, typeParam, descriptor);
	}

}
