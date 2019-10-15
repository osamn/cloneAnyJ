package ReIW.tiny.cloneAny.pojo.impl;

final class SlotValueBuilder extends SlotLikeSignatureParser<SlotValue> {
	private SlotValue slot;

	SlotValueBuilder(final String typeParam) {
		super.typeParamName = typeParam;
		super.slotCons = (val) -> {
			this.slot = val;
		};
	}

	SlotValue build(final String signature) {
		this.accept(signature);
		return slot;
	}

	@Override
	protected SlotValue newSlotLike(final String typeParam, final String descriptor) {
		return new SlotValue(typeParam, descriptor);
	}

}
