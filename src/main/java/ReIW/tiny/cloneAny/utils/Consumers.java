package ReIW.tiny.cloneAny.utils;

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
		final int[] counter = { from };
		// +1 -1 の場合は後置演算子で
		if (step == 1) {
			return element -> consumer.accept(element, counter[0]++);
		} else if (step == -1) {
			return element -> consumer.accept(element, counter[0]--);
		}

		// それ以外は accept + 複合代入で
		return element -> {
			consumer.accept(element, counter[0]);
			counter[0] += step;
		};
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
