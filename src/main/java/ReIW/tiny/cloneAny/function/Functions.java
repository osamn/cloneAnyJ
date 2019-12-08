package ReIW.tiny.cloneAny.function;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public interface Functions {

	@FunctionalInterface
	interface ObjIntFunction<T, R> {

		R apply(T t, int u);

		default <V> ObjIntFunction<T, V> andThen(Function<? super R, ? extends V> after) {
			Objects.requireNonNull(after);
			return (T t, int u) -> after.apply(apply(t, u));
		}
	}

	static <T, R> Function<T, R> withIndex(final int from, final int step, final ObjIntFunction<T, R> function) {
		final AtomicInteger counter = new AtomicInteger();
		return element -> function.apply(element, counter.getAndAdd(step));
	}

	static <T, R> Function<T, R> withIndex(final int from, final ObjIntFunction<T, R> function) {
		return withIndex(from, 1, function);
	}

	static <T, R> Function<T, R> withIndex(final ObjIntFunction<T, R> function) {
		return withIndex(0, 1, function);
	}

}
