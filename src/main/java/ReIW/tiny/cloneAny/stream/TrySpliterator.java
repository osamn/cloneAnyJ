package ReIW.tiny.cloneAny.stream;

import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import ReIW.tiny.cloneAny.stream.StreamExtention.UncheckedStreamException;

class TrySpliterator<T> implements Spliterator<T> {

	private final Spliterator<T> spliterator;
	private final BiFunction<? super Throwable, ? super T, Boolean> handler;

	TrySpliterator(final Spliterator<T> spliterator,
			final BiFunction<? super Throwable, ? super T, Boolean> exceptionHandler) {
		assert !(spliterator instanceof TrySpliterator);

		this.spliterator = spliterator;
		this.handler = exceptionHandler;
	}

	Spliterator<T> getSourceSpliterator() {
		return spliterator;
	}

	@Override
	public boolean tryAdvance(final Consumer<? super T> action) {
		final AtomicBoolean continueStream = new AtomicBoolean(Boolean.TRUE);
		boolean next = spliterator.tryAdvance(actionDelegate(action, continueStream));
		if (next) {
			// delegate で例外時には handler から継続、中止が返されるよ
			// 例外が発生してない場合は初期値のままなので結局 true が返るよ
			return continueStream.get();
		}
		return next;
	}

	private Consumer<? super T> actionDelegate(final Consumer<? super T> action, final AtomicBoolean continueStream) {
		return val -> {
			try {
				action.accept(val);
			} catch (UncheckedStreamException e) {
				final boolean result = this.handler.apply(e.getCause(), val);
				continueStream.set(result);
			}
		};
	}

	@Override
	public Spliterator<T> trySplit() {
		// split されたらどう動くかわからんので split されないようにしておく
		return null;
	}

	@Override
	public long estimateSize() {
		return spliterator.estimateSize();
	}

	@Override
	public int characteristics() {
		return spliterator.characteristics();
	}

}
