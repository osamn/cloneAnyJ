package ReIW.tiny.cloneAny.compile;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;

import ReIW.tiny.cloneAny.Ditto;
import ReIW.tiny.cloneAny.core.AssemblyException;
import ReIW.tiny.cloneAny.pojo.Slot;

public class DittoBuilder implements Ditto.Builder {

	public static Ditto.Builder builder = new DittoBuilder();

	public static Ditto.Builder getInstance() {
		return builder;
	}

	private DittoBuilder() {
	}

	private final ConcurrentHashMap<CKey, Object> hive = new ConcurrentHashMap<>();

	@Override
	public <L> Ditto<L, L> get(final Class<L> clazz) {
		return get(clazz, clazz);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <L, R> Ditto<L, R> get(final Class<L> lhs, final Class<R> rhs) {
		return (Ditto<L, R>) hive.computeIfAbsent(new CKey(lhs, rhs), this::compute);
	}

	// こいつは内部的に使うやつなんで generic を明示する必要はないの
	@SuppressWarnings("rawtypes")
	protected Ditto get(final Slot lhs, final Slot rhs) {
		return (Ditto) hive.computeIfAbsent(new CKey(lhs, rhs), this::compute);
	}

	private final Object compute(final CKey key) {
		final Class<?> clazz = new DittoClassAssembler(key).createClass();
		try {
			return clazz.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new AssemblyException(e);
		}
	}
}