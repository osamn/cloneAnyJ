package ReIW.tiny.cloneAny;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class Copycat {

	private static final ConcurrentHashMap<DKey, Object> hive = new ConcurrentHashMap<>();

	private Copycat() {
	}

	public static final <L> Ditto<L, L> get(final Class<L> lhs) {
		return get(lhs, lhs);
	}

	@SuppressWarnings("unchecked")
	public static final <L, R> Ditto<L, R> get(final Class<L> lhs, final Class<R> rhs) {
		return (Ditto<L, R>) hive.computeIfAbsent(new DKey(lhs, rhs), (final DKey key) -> {
			return compute(key.lhs, key.rhs);
		});
	}

	private static final <L, R> Ditto<L, R> compute(Class<L> lhs, Class<R> rhs) {
		return null;
	}

	private static class DKey {

		private final Class<?> lhs;
		private final Class<?> rhs;
		private final int hashCode;

		private DKey(final Class<?> lhs, final Class<?> rhs) {
			this.lhs = lhs;
			this.rhs = rhs;
			hashCode = Objects.hash(lhs, rhs);
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

			final DKey other = (DKey) dst;
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

	}

}
