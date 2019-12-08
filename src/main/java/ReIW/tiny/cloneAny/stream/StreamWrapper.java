package ReIW.tiny.cloneAny.stream;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

final class StreamWrapper<T> implements StreamExtention<T> {

	private final Stream<T> stream;

	StreamWrapper(Stream<T> baseStream) {
		this.stream = baseStream;
	}

	@Override
	public Stream<T> stream() {
		return stream;
	}

	@Override
	public Iterator<T> iterator() {
		return stream.iterator();
	}

	@Override
	public Spliterator<T> spliterator() {
		return stream.spliterator();
	}

	@Override
	public boolean isParallel() {
		return stream.isParallel();
	}

	@Override
	public StreamExtention<T> sequential() {
		return new StreamWrapper<T>(stream.sequential());
	}

	@Override
	public StreamExtention<T> parallel() {
		return new StreamWrapper<T>(stream.parallel());
	}

	@Override
	public StreamExtention<T> unordered() {
		return new StreamWrapper<T>(stream.unordered());
	}

	@Override
	public StreamExtention<T> onClose(Runnable closeHandler) {
		return new StreamWrapper<T>(stream.onClose(closeHandler));
	}

	@Override
	public void close() {
		stream.close();
	}

	@Override
	public StreamExtention<T> filter(Predicate<? super T> predicate) {
		return new StreamWrapper<T>(stream.filter(predicate));
	}

	@Override
	public <R> StreamExtention<R> map(Function<? super T, ? extends R> mapper) {
		return new StreamWrapper<R>(stream.map(mapper));
	}

	@Override
	public IntStream mapToInt(ToIntFunction<? super T> mapper) {
		return stream.mapToInt(mapper);
	}

	@Override
	public LongStream mapToLong(ToLongFunction<? super T> mapper) {
		return stream.mapToLong(mapper);
	}

	@Override
	public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
		return stream.mapToDouble(mapper);
	}

	@Override
	public <R> StreamExtention<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
		return new StreamWrapper<R>(stream.flatMap(mapper));
	}

	@Override
	public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
		return stream.flatMapToInt(mapper);
	}

	@Override
	public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
		return stream.flatMapToLong(mapper);
	}

	@Override
	public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
		return stream.flatMapToDouble(mapper);
	}

	public StreamExtention<T> distinct() {
		return new StreamWrapper<T>(stream.distinct());
	}

	public StreamExtention<T> sorted() {
		return new StreamWrapper<T>(stream.sorted());
	}

	public StreamExtention<T> sorted(Comparator<? super T> comparator) {
		return new StreamWrapper<T>(stream.sorted(comparator));
	}

	public StreamExtention<T> peek(Consumer<? super T> action) {
		return new StreamWrapper<T>(stream.peek(action));
	}

	public StreamExtention<T> limit(long maxSize) {
		return new StreamWrapper<T>(stream.limit(maxSize));
	}

	public StreamExtention<T> skip(long n) {
		return new StreamWrapper<T>(stream.skip(n));
	}

	public StreamExtention<T> takeWhile(Predicate<? super T> predicate) {
		return new StreamWrapper<T>(stream.takeWhile(predicate));
	}

	public StreamExtention<T> dropWhile(Predicate<? super T> predicate) {
		return new StreamWrapper<T>(stream.dropWhile(predicate));
	}

	@Override
	public void forEach(Consumer<? super T> action) {
		stream.forEach(action);
	}

	@Override
	public void forEachOrdered(Consumer<? super T> action) {
		stream.forEachOrdered(action);
	}

	@Override
	public Object[] toArray() {
		return stream.toArray();
	}

	@Override
	public <A> A[] toArray(IntFunction<A[]> generator) {
		return stream.toArray(generator);
	}

	@Override
	public T reduce(T identity, BinaryOperator<T> accumulator) {
		return stream.reduce(identity, accumulator);
	}

	@Override
	public Optional<T> reduce(BinaryOperator<T> accumulator) {
		return stream.reduce(accumulator);
	}

	@Override
	public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
		return stream.reduce(identity, accumulator, combiner);
	}

	@Override
	public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
		return stream.collect(supplier, accumulator, combiner);
	}

	@Override
	public <R, A> R collect(Collector<? super T, A, R> collector) {
		return stream.collect(collector);
	}

	@Override
	public Optional<T> min(Comparator<? super T> comparator) {
		return stream.min(comparator);
	}

	@Override
	public Optional<T> max(Comparator<? super T> comparator) {
		return stream.max(comparator);
	}

	@Override
	public long count() {
		return stream.count();
	}

	@Override
	public boolean anyMatch(Predicate<? super T> predicate) {
		return stream.anyMatch(predicate);
	}

	@Override
	public boolean allMatch(Predicate<? super T> predicate) {
		return stream.allMatch(predicate);
	}

	@Override
	public boolean noneMatch(Predicate<? super T> predicate) {
		return stream.noneMatch(predicate);
	}

	@Override
	public Optional<T> findFirst() {
		return stream.findFirst();
	}

	@Override
	public Optional<T> findAny() {
		return stream.findAny();
	}

}
