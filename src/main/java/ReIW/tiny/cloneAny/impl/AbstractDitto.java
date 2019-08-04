package ReIW.tiny.cloneAny.impl;

import ReIW.tiny.cloneAny.Ditto;

public abstract class AbstractDitto<L, R> implements Ditto<L, R> {

	@SuppressWarnings("unchecked")
	@Override
	public final R clone(L lhs) {
		return (R) copyOrClone(lhs, null);
	}

	@Override
	public final void copyTo(L lhs, R rhs) {
		if (lhs == null || rhs == null) {
			throw new NullPointerException("Copy \"" + getLhsClass() + "\" to \"" + getRhsClass() + "\" fail.");
		}
		copyOrClone(lhs, rhs);
	}

	protected abstract String getLhsClass();

	protected abstract String getRhsClass();

	protected abstract Object copyOrClone(Object lhs, Object rhs);

}
