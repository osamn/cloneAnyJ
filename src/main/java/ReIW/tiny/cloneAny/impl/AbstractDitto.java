package ReIW.tiny.cloneAny.impl;

import ReIW.tiny.cloneAny.Ditto;

public abstract class AbstractDitto<L, R> implements Ditto<L, R> {

	@Override
	public R clone(L lhs) {
		R rhs = newInstanceFrom(lhs);
		copyTo(lhs, rhs);
		return rhs;
	}

	@Override
	public abstract void copyTo(L lhs, R rhs);

	protected abstract R newInstanceFrom(L lhs);

}
