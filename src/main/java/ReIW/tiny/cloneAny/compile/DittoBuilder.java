package ReIW.tiny.cloneAny.compile;

import java.lang.reflect.InvocationTargetException;
import java.util.WeakHashMap;

import ReIW.tiny.cloneAny.Ditto;
import ReIW.tiny.cloneAny.core.AssemblyException;
import ReIW.tiny.cloneAny.pojo.TypeDef;

public class DittoBuilder implements Ditto.Builder {

	public static Ditto.Builder builder = new DittoBuilder();

	public static Ditto.Builder getInstance() {
		return builder;
	}

	private final WeakHashMap<CKey, Object> cacheRef = new WeakHashMap<>();

	private DittoBuilder() {
	}

	@Override
	public <L> Ditto<L, L> get(final Class<L> clazz) {
		return get(clazz, clazz);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <L, R> Ditto<L, R> get(final Class<L> lhs, final Class<R> rhs) {
		final TypeDef lhsDef = TypeDef.createInstance(lhs);
		final TypeDef rhsDef = TypeDef.createInstance(rhs);
		return (Ditto<L, R>) get(lhsDef, rhsDef);
	}

	// こいつは内部的に使うやつなんで generic を明示する必要はないの
	@SuppressWarnings("rawtypes")
	protected Ditto get(final TypeDef lhsDef, final TypeDef rhsDef) {
		final CKey key = new CKey(lhsDef, rhsDef);
		return (Ditto) cacheRef.computeIfAbsent(key, DittoBuilder::compute);
	}

	private static final Object compute(final CKey key) {
		final Class<?> clazz = new DittoClassAssembler(key).loadLocalClass();
		try {
			return clazz.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new AssemblyException(e);
		}
	}
}