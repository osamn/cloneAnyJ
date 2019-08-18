package ReIW.tiny.cloneAny;

import ReIW.tiny.cloneAny.impl.DittoBuilder;

public interface Ditto<Lhs, Rhs> {

	Rhs clone(Lhs lhs);

	Rhs copyTo(Lhs lhs, Rhs rhs);

	static Builder builder() {
		return DittoBuilder.getInstance();
	}

	interface Builder {
		<L> Ditto<L, L> get(final Class<L> clazz);

		<L, R> Ditto<L, R> get(final Class<L> lhs, final Class<R> rhs);
	}
}
