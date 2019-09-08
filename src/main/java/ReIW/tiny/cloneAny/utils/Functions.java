package ReIW.tiny.cloneAny.utils;

import java.util.Objects;
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
		final int[] counter = { from };
		// +1 -1 の場合は後置演算子で
		if (step == 1) {
			return element -> function.apply(element, counter[0]++);
		} else if (step == -1) {
			return element -> function.apply(element, counter[0]--);
		}

		// それ以外
		return element -> {
			R ret = function.apply(element, counter[0]);
			counter[0] += step;
			return ret;
		};
	}

	static <T, R> Function<T, R> withIndex(final int from, final ObjIntFunction<T, R> function) {
		return withIndex(from, 1, function);
	}

	static <T, R> Function<T, R> withIndex(final ObjIntFunction<T, R> function) {
		return withIndex(0, 1, function);
	}

}
