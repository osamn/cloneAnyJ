package ReIW.tiny.cloneAny.stream;

import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface StreamExtention<T> extends Stream<T> {

	static <R> StreamExtention<R> of(final Stream<R> stream) {
		return new StreamWrapper<>(stream);
	}

	interface ThrowableFunction<TVal, R> {
		R apply(TVal value) throws Exception;
	}

	@SuppressWarnings("serial")
	static final class UncheckedStreamException extends RuntimeException {
		private UncheckedStreamException(final Exception e) {
			super(e);
		}
	}

	Stream<T> baseStream();

	default <R> StreamExtention<R> tryMap(final ThrowableFunction<? super T, ? extends R> mapper) {
		return tryMap(mapper, StreamExtention::resumeNext);
	}

	default <R> StreamExtention<R> tryMapUntil(final ThrowableFunction<? super T, ? extends R> mapper) {
		return tryMap(mapper, StreamExtention::stallStream);
	}

	default <R> StreamExtention<R> tryMap(final ThrowableFunction<? super T, ? extends R> mapper,
			final BiFunction<? super Throwable, ? super T, Boolean> handler) {
		final Spliterator<T> spliterator = new TrySpliterator<T>(unwrap(baseStream().spliterator()), handler);
		return new StreamWrapper<R>(StreamSupport.stream(spliterator, isParallel()).map((v -> {
			try {
				return mapper.apply(v);
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new UncheckedStreamException(e);
			}
		})));
	}

	private static <T> Boolean resumeNext(final Throwable e, final T val) {
		return Boolean.TRUE;
	}

	private static <T> Boolean stallStream(final Throwable e, final T val) {
		return Boolean.FALSE;
	}

	private static <V> Spliterator<V> unwrap(final Spliterator<V> spliterator) {
		if (spliterator instanceof TrySpliterator) {
			return ((TrySpliterator<V>) spliterator).getSourceSpliterator();
		} else {
			return spliterator;
		}
	}

}
