package ReIW.tiny.cloneAny.stream;

import java.util.Spliterator;
import java.util.function.BiPredicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface StreamExtention<T> extends Stream<T> {

	static <U> StreamExtention<U> of(final Stream<U> stream) {
		return new StreamWrapper<>(stream);
	}

	@FunctionalInterface
	interface ThrowableFunction<U, R> {
		R apply(U value) throws Exception;
	}

	@SuppressWarnings("serial")
	final class UncheckedStreamException extends RuntimeException {
		private UncheckedStreamException(final Exception e) {
			super(e);
		}
	}

	Stream<T> stream();

	default <R> StreamExtention<R> tryMap(final ThrowableFunction<? super T, ? extends R> mapper) {
		return tryMap(mapper, StreamExtention::resumeNext);
	}

	default <R> StreamExtention<R> tryUntilMap(final ThrowableFunction<? super T, ? extends R> mapper) {
		return tryMap(mapper, StreamExtention::stalled);
	}

	default <R> StreamExtention<R> tryMap(final ThrowableFunction<? super T, ? extends R> mapper,
			final BiPredicate<? super Throwable, ? super T> exceptionHandler) {
		final Spliterator<T> spliterator = new TrySpliterator<T>(unwrap(stream().spliterator()), exceptionHandler);
		return new StreamWrapper<R>(StreamSupport.stream(spliterator, isParallel()).map(v -> {
			try {
				return mapper.apply(v);
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new UncheckedStreamException(e);
			}
		}));
	}

	private static <U> boolean resumeNext(final Throwable e, final U val) {
		return true;
	}

	private static <U> boolean stalled(final Throwable e, final U val) {
		return false;
	}

	private static <U> Spliterator<U> unwrap(final Spliterator<U> spliterator) {
		if (spliterator instanceof TrySpliterator) {
			return ((TrySpliterator<U>) spliterator).getSourceSpliterator();
		} else {
			return spliterator;
		}
	}

}
