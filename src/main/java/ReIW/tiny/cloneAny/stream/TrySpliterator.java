package ReIW.tiny.cloneAny.stream;

import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import ReIW.tiny.cloneAny.stream.StreamExtention.UncheckedStreamException;

final class TrySpliterator<T> implements Spliterator<T> {

	private final Spliterator<T> spliterator;
	private final BiPredicate<? super Throwable, ? super T> handler;

	TrySpliterator(final Spliterator<T> spliterator, final BiPredicate<? super Throwable, ? super T> exceptionHandler) {
		assert !(spliterator instanceof TrySpliterator);

		this.spliterator = spliterator;
		this.handler = exceptionHandler;
	}

	Spliterator<T> getSourceSpliterator() {
		return spliterator;
	}

	@Override
	public boolean tryAdvance(final Consumer<? super T> action) {
		final AtomicBoolean resumeNext = new AtomicBoolean(true);
		final boolean hasNext = spliterator.tryAdvance(actionDelegate(action, resumeNext));
		if (hasNext) {
			// delegate で例外時には handler から継続、中止が返されるよ
			// 例外が発生してない場合は初期値のままなので true だよ
			return resumeNext.get();
		}
		return hasNext;
	}

	private Consumer<? super T> actionDelegate(final Consumer<? super T> action, final AtomicBoolean resumeNext) {
		return val -> {
			try {
				action.accept(val);
			} catch (UncheckedStreamException e) {
				final boolean result = this.handler.test(e.getCause(), val);
				resumeNext.set(result);
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
