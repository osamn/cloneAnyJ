package ReIW.tiny.cloneAny.pojo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.objectweb.asm.Type;

public interface Operand {

	/** インスタンスフィールドのロード */
	static final class Load implements Operand {
		public final String name;
		public final String clazz;

		private Load(final String name, final String clazz) {
			this.name = name;
			this.clazz = clazz;
		}

		@Override
		public String toString() {
			return "Load \"" + name + "\"";
		}
	}

	/** プロパティ取得 */
	static final class Get implements Operand {
		public final String name;
		public final String rel;
		public final String clazz;

		private Get(final String name, final String rel, final String clazz) {
			this.name = name;
			this.rel = rel;
			this.clazz = clazz;
		}

		@Override
		public String toString() {
			return "Get " + rel + " \"" + name + "\"";
		}
	}

	/** インスタンスフィールドに設定 */
	static final class Store implements Operand {
		public final String name;
		public final String clazz;

		private Store(final String name, final String clazz) {
			this.name = name;
			this.clazz = clazz;
		}

		@Override
		public String toString() {
			return "Store \"" + name + "\"";
		}
	}

	/** プロパティ設定 */
	static final class Set implements Operand {
		public final String name;
		public final String rel;
		public final String clazz;

		private Set(final String name, final String rel, final String clazz) {
			this.name = name;
			this.rel = rel;
			this.clazz = clazz;
		}

		@Override
		public String toString() {
			return "Set " + rel + " \"" + name + "\"";
		}
	}

	/** 値変換 */
	static final class Move implements Operand {
		public final Slot src;
		public final Slot dst;

		private Move(final Slot src, final Slot dst) {
			this.src = src;
			this.dst = dst;
		}

		@Override
		public String toString() {
			return "Move " + src.typeClass + " -> " + dst.typeClass;
		}
	}

	/** コンストラクタ引数用の値変換 */
	static final class Push implements Operand {
		public final Slot src;
		public final Slot dst;

		public Push(final Slot src, final Slot dst) {
			this.src = src;
			this.dst = dst;
		}

		@Override
		public String toString() {
			return "Push " + src.typeClass + " -> " + dst.typeClass;
		}
	}

	/** コンストラクタ呼び出し */
	static final class Ctor implements Operand {
		public final String descriptor;

		private Ctor(final String descriptor) {
			this.descriptor = descriptor;
		}

		@Override
		public String toString() {
			return "<init>" + descriptor;
		}
	}

	static Builder builder(final Class<?> lhs, final Class<?> rhs) {
		return new Builder(TypeDefBuilder.createTypeDef(lhs), TypeDefBuilder.createTypeDef(rhs));
	}

	static Builder builder(final Slot lhs, final Slot rhs) {
		return new Builder(TypeDefBuilder.createTypeDef(lhs.typeClass).bind(lhs.slotList),
				TypeDefBuilder.createTypeDef(rhs.typeClass).bind(rhs.slotList));
	}

	static final class Builder {
		// コピー元
		private final TypeAccessDef provider;
		// コピー先
		private final TypeAccessDef consumer;

		private Builder(final TypeAccessDef lhs, final TypeAccessDef rhs) {
			this.provider = lhs;
			this.consumer = rhs;
		}

		public Stream<Operand> operands(final boolean requireNew) {
			// 副作用として計算されるコンストラクタの計算結果を保持しておくリスト
			final List<List<Ops>> ctorList = new ArrayList<>();

			// 一旦 AccessEntry のペア(=Ops)のストリームを計算する
			final List<Ops> copyOps = calcCopyAndInit(ctorList)
					// 終端操作を明示的にしないと ctorList が計算されないので注意
					.collect(Collectors.toList());

			// コンストラクタ呼び出しオペランドのストリームを作る
			final Stream<Operand> ctor;
			// requireNew によって clone か paste かを切り替える感じ
			if (requireNew) {
				// 対象とするコンストラクタを決定して
				final List<Ops> exactCtor = findProbablyConstructor(ctorList);
				// コンストラクタ呼び出しのストリームを計算する
				if (exactCtor == null) {
					// 全ての引数を設定可能なコンストラクタがない
					if (consumer.hasDefaultCtor()) {
						// デフォルトコンストラクタで生成的な
						ctor = Stream.of(new Ctor("()V"));
					} else {
						throw new AbortCallException("No default constructor.");
					}
				} else {
					ctor = buildCtorStream(exactCtor);
				}
			} else {
				// コンストラクタ呼び出さない
				ctor = Stream.empty();
			}

			// プロパティとかフィールドをコピーするオペランドのストリームを作る
			final Stream<Operand> copy = buildCopyStream(copyOps);

			return Stream.concat(ctor, copy);
		}

		// push
		// load/get
		// push
		// load/get
		// ...
		// ctor
		private Stream<Operand> buildCtorStream(final List<Ops> ctor) {
			final Stream.Builder<Operand> builder = Stream.builder();

			// コンストラクタの引数のコピー操作をストリームに
			for (Ops op : ctor) {
				final AccessEntry src = op.lhs;
				final AccessEntry dst = op.rhs;

				// まずは変換可能かみるため push オペランドを配置
				//// コンストラクタ引数の場合は必ず設定可能じゃないと
				//// 実行時に不正なバイトコードで叱られると思う
				//// なんで bytecode 生成時に変換不可だったらエラーにできるように目印つけとく
				builder.accept(new Push(src.slot, dst.slot));

				// push prop value
				switch (src.elementType) {
				case AccessEntry.ACE_FIELD:
				case AccessEntry.ACE_FINAL_FIELD:
					builder.accept(new Load(src.name, src.slot.typeClass));
					break;
				case AccessEntry.ACE_PROP_GET:
					builder.accept(new Get(src.name, src.rel, src.slot.typeClass));
					break;
				default:
					throw new IllegalStateException();
				}
			}

			final String desc = ctor.get(0).rhs.rel;
			// 最後にコンストラクタ呼び出しをストリームに流す
			builder.accept(new Ctor(desc));

			return builder.build();
		}

		// move
		// load/get
		// store/set
		// ...
		private Stream<Operand> buildCopyStream(final List<Ops> ops) {
			final Stream.Builder<Operand> builder = Stream.builder();

			// コピー操作をストリームに
			ops.forEach(op -> {
				final AccessEntry src = op.lhs;
				final AccessEntry dst = op.rhs;

				// まずは move オペランドを
				// 変換可能かみるため
				builder.accept(new Move(src.slot, dst.slot));

				// push prop value
				switch (src.elementType) {
				case AccessEntry.ACE_FIELD:
				case AccessEntry.ACE_FINAL_FIELD:
					builder.accept(new Load(src.name, src.slot.typeClass));
					break;
				case AccessEntry.ACE_PROP_GET:
					builder.accept(new Get(src.name, src.rel, src.slot.typeClass));
					break;
				default:
					throw new IllegalStateException();
				}

				// set stack top value to dst property
				switch (dst.elementType) {
				case AccessEntry.ACE_FIELD:
					builder.accept(new Store(dst.name, dst.slot.typeClass));
					break;
				case AccessEntry.ACE_PROP_SET:
					builder.accept(new Set(dst.name, dst.rel, dst.slot.typeClass));
					break;
				default:
					throw new IllegalStateException();
				}
			});

			return builder.build();
		}

		/**
		 * コンストラクタを除いたコピー操作のストリームを計算する
		 * 
		 * 副作用としてコンストラクタ操作を ctorList に設定する
		 */
		private Stream<Ops> calcCopyAndInit(final List<List<Ops>> ctorList) {
			// とりあえず getter 側のマップつくる
			// マップキーはプロパティ名
			final Map<String, AccessEntry> getters = provider.accessors().filter(acc -> acc.canGet)
					.collect(Collectors.toMap(get -> get.name, get -> get));
			// 最後の Map#put に残りを全部入れるために
			// getter 側で使用されたものの名前を持っておく
			final HashSet<String> processed = new HashSet<>();

			// Ops のマップを作る
			// マップキーはコンストラクタをシグネチャでまとめたいので rel つかう
			final Map<String, List<Ops>> opsGroup = consumer.accessors().filter(acc -> acc.canSet)
					// setter のストリームから Ops を作れるものだけフィルタする
					.filter(set -> {
						return getters.containsKey(set.name) // setter に対応する getter がある
								|| getters.containsKey("*") // または provider が Map#get をもってる
								|| set.name.contentEquals("*"); // またはこの setter が Map#put
					})
					// getter -> setter で Ops を作る
					// Map の場合は複数の Ops を連結する必要があるので flatMap でやる
					.flatMap(set -> {
						if (set.name.contentEquals("*")) {
							// "*" は Map の場合のみで、かつ必ずストリームの最後のはずなので
							// processed に入っていないものをすべて移してあげる
							// name or * -> *
							final Stream.Builder<Ops> rest = Stream.builder();
							getters.values().forEach(acc -> {
								if (!processed.contains(acc.name)) {
									/*
									 * Map -> Map の場合 lhs#isEmpty が rhs.put("empty", val) で設定される
									 * なんとなく後段の型変換の可能性みるあたりでいなくなりそうだから気にしなくていいのかも
									 */
									// TODO Map#isEmpty の取り扱い考える
									// if (!getters.containsKey("*") || !acc.rel.contentEquals("isEmpty") ||
									// !acc.slot.typeClass.contentEquals("Z"))
									rest.accept(new Ops(acc, set));
								}
							});
							return rest.build();
						} else if (getters.containsKey(set.name)) {
							// name -> name
							processed.add(set.name);
							return Stream.of(new Ops(getters.get(set.name), set));
						} else if (getters.containsKey("*")) {
							// * -> name
							return Stream.of(new Ops(getters.get("*"), set));
						} else {
							throw new IllegalStateException();
						}
					})
					// setter 側の rel でまとめ上げる
					// rel は下のどれか
					// ・コンストラクタ -> (...)V
					// ・アクセッサ -> set* get*
					// ・フィールド -> name と同じ
					.collect(Collectors.groupingBy(op -> op.rhs.rel));

			return opsGroup.entrySet().stream().filter(entry -> {
				final String sig = entry.getKey();
				if (sig.startsWith("(")) {
					// コンストラクタの場合、メソッドシグネチャと get -> set をまとめた Ops の数が一致してるものだけ
					// ctorList にいれておく
					// ＝呼び出し時にすべての引数が設定可能なもの
					final List<Ops> cc = entry.getValue().stream()// .filter(op -> !op.lhs.name.contentEquals("*"))
							.collect(Collectors.toList());
					final int argSize = (Type.getArgumentsAndReturnSizes(sig) >> 2) - 1/* without this */;
					if (argSize == cc.size()) {
						// exact matched constructor
						ctorList.add(cc);
					}
					return false;
				}
				return true;
			}).map(entry -> entry.getValue()).flatMap(ops -> ops.stream());
		}

		/** 一番確からしいコンストラクタをとる */
		private static List<Ops> findProbablyConstructor(final List<List<Ops>> ctorOps) {
			final Optional<Map.Entry<Integer, List<List<Ops>>>> most = ctorOps.stream()
					// コンストラクタの引数の数でグルーピングして
					.collect(Collectors.groupingBy(List::size)).entrySet().stream()
					// 一番数が多いやつをとってくる
					.max(Comparator.comparing(entry -> entry.getKey()));

			if (most.isPresent()) {
				final List<List<Ops>> nearest = most.get().getValue();
				if (nearest.size() > 1) {
					// 一番引数の多いやつが複数見つかったときはエラーにしとく
					throw new AbortCallException("Ambiguous constructor call.");
				}
				return nearest.get(0);
			}
			return null;
		}

		/** lhs -> rhs へのコピー操作のオブジェクト */
		private static final class Ops {
			final AccessEntry lhs;
			final AccessEntry rhs;

			private Ops(AccessEntry lhs, AccessEntry rhs) {
				this.lhs = lhs;
				this.rhs = rhs;
			}
		}

	}
}
