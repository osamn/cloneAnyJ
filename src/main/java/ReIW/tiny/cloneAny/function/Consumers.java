package ReIW.tiny.cloneAny.function;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;

/**
 * stream 処理にインデックスを提供するため、ObjIntConsumer を生成するヘルパ.
 *
 * <pre>
 * 使用例：
 * import static jp.go.jpo.hitachi.common.utils.ConsumerHelper.withIndex;
 * ...
 *   Arraylist&lt;String&gt; list = ...
 *   lisit.forEach(withIndex(value, idx) -> {
 *      // value はリスト中の文字列
 *      // idx はリスト中のインデックス
 *      ...
 *   });
 * ...
 * </pre>
 */
public interface Consumers {

	public static <T> Consumer<T> nop() {
		return value -> {
		};
	}

	/**
	 * stream 処理にインデックス値を提供する Consumer を生成する.
	 *
	 * @param from     インデックス開始値
	 * @param step     インデックス増減ステップ
	 * @param consumer インデックスを使用する ObjectIntConcumer
	 * @param <T>      入力値の型
	 * @return Consumer
	 */
	public static <T> Consumer<T> withIndex(final int from, final int step, final ObjIntConsumer<T> consumer) {
		final AtomicInteger counter = new AtomicInteger(from);
		return element -> consumer.accept(element, counter.getAndAdd(step));
	}

	/**
	 * stream 処理にインデックス値を提供する Consumer を生成する.
	 *
	 * @param from     インデックス開始値
	 * @param consumer インデックスを使用する ObjectIntConcumer
	 * @param <T>      入力値の型
	 * @return Consumer
	 */
	public static <T> Consumer<T> withIndex(final int from, final ObjIntConsumer<T> consumer) {
		return withIndex(from, 1, consumer);
	}

	/**
	 * stream 処理にインデックス値を提供する Consumer を生成する.
	 *
	 * @param consumer インデックスを使用する ObjectIntConcumer
	 * @param <T>      入力値の型
	 * @return Consumer
	 */
	public static <T> Consumer<T> withIndex(final ObjIntConsumer<T> consumer) {
		return withIndex(0, 1, consumer);
	}

}
