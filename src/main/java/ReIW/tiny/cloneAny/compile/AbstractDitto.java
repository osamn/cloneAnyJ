package ReIW.tiny.cloneAny.compile;

import ReIW.tiny.cloneAny.Ditto;

public abstract class AbstractDitto<L, R> implements Ditto<L, R> {

	protected AbstractDitto() {
	}

	@SuppressWarnings("unchecked")
	@Override
	public final R clone(L lhs) {
		return (R) copyOrClone(lhs, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final R copyTo(L lhs, R rhs) {
		if (lhs == null || rhs == null) {
			throw new IllegalArgumentException("lhs:" + getLhsClass() + " or rhs:" + getRhsClass() + " is null.");
		}
		return (R) copyOrClone(lhs, rhs);
	}

	protected abstract String getLhsClass();

	protected abstract String getRhsClass();

	protected abstract Object copyOrClone(Object lhs, Object rhs);

}
