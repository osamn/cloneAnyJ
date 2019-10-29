package ReIW.tiny.cloneAny.pojo.impl;

final class SlotValueBuilder extends SlotLikeSignatureParser {
	private SlotValue slot;

	SlotValueBuilder() {
		this.slotCons = (val) -> {
			this.slot = val;
		};
	}

	// 最初の wildcard の値を指定の値にするよ
	SlotValueBuilder setPrimaryWildcard(final String wildcard) {
		this.wildcard = wildcard;
		return this;
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
