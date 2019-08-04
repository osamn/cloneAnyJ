package ReIW.tiny.cloneAny;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;

import ReIW.tiny.cloneAny.core.AssemblyException;
import ReIW.tiny.cloneAny.impl.CKey;
import ReIW.tiny.cloneAny.impl.DittoClassAssembler;

public interface Ditto<Lhs, Rhs> {

	Rhs clone(Lhs lhs);

	void copyTo(Lhs lhs, Rhs rhs);

	static Builder builder() {
		return new Builder();
	}

	static class Builder {

		private static final ConcurrentHashMap<CKey, Object> hive = new ConcurrentHashMap<>();

		private Builder() {
		}

		public <L> Ditto<L, L> get(final Class<L> clazz) {
			return get(clazz, clazz);
		}

		@SuppressWarnings("unchecked")
		public <L, R> Ditto<L, R> get(final Class<L> lhs, final Class<R> rhs) {
			return (Ditto<L, R>) hive.computeIfAbsent(new CKey(lhs, rhs), this::compute);
		}

		private final Object compute(final CKey key) {
			final Class<?> clazz = new DittoClassAssembler(key).createClass();
			try {
				return clazz.getDeclaredConstructor().newInstance();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				throw new AssemblyException(e);
			}
		}
	}
}
