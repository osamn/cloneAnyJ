package ReIW.tiny.cloneAny.impl;

import java.util.Objects;

public final class CKey {

	final Class<?> lhs;
	final Class<?> rhs;

	private final int hashCode;

	public CKey(final Class<?> lhs, final Class<?> rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
		hashCode = Objects.hash(lhs, rhs);
	}

	public String toSignature() {
		// TODO lhs rhs からクラスの signature を作成する
		return null;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(final Object dst) {
		if (this == dst)
			return true;
		if (dst == null)
			return false;
		if (getClass() != dst.getClass())
			return false;

		final CKey other = (CKey) dst;
		if (hashCode != other.hashCode)
			return false;

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

	// クラス名として使うよ
	@Override
	public String toString() {
		return lhs.getName().replace('.', '_') + "." + rhs.getName().replace('.', '_');
	}
}