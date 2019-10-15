package ReIW.tiny.cloneAny.compile;

import java.lang.reflect.InvocationTargetException;
import java.util.WeakHashMap;

import ReIW.tiny.cloneAny.Ditto;
import ReIW.tiny.cloneAny.core.AssemblyException;
import ReIW.tiny.cloneAny.pojo_.Slot;
import ReIW.tiny.cloneAny.pojo_.TypeDef;

public final class DittoBuilder implements Ditto.Builder {

	private static Ditto.Builder builder = new DittoBuilder();

	public static Ditto.Builder getInstance() {
		return builder;
	}

	private final WeakHashMap<String, Object> cacheRef = new WeakHashMap<>();

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
		return (Ditto<L, R>) cacheRef.computeIfAbsent(getClassName(lhsDef, rhsDef),
				name -> compute(name, lhsDef, rhsDef));
	}

	private static final Object compute(final String className, final TypeDef lhsDef, final TypeDef rhsDef) {
		final Class<?> clazz = new DittoClassAssembler(className, lhsDef, rhsDef).ensureDittoClass();
		try {
			return clazz.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new AssemblyException(e);
		}
	}

	private static String getClassName(TypeDef lhs, TypeDef rhs) {
		return "$ditto." + getTypeIdentifier(lhs) + "_" + getTypeIdentifier(lhs);
	}

	private static String getTypeIdentifier(final TypeDef type) {
		Slot slot = type.toSlot();
		final StringBuilder buf = new StringBuilder();
		buildSignaturedName(buf, slot);
		return buf.toString();
	}

	private static void buildSignaturedName(final StringBuilder buf, final Slot slot) {
		buf.append(slot.descriptor.replace('/', '_'));
		slot.slotList.forEach(s -> {
			buf.append('$');
			buildSignaturedName(buf, slot);
		});
	}

}