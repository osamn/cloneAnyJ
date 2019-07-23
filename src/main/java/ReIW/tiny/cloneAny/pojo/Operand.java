package ReIW.tiny.cloneAny.pojo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.objectweb.asm.Type;

public interface Operand {

	static class Load implements Operand {
		public final String name;
		public final Slot slot;

		private Load(final String name, final Slot slot) {
			this.name = name;
			this.slot = slot;
		}

		@Override
		public String toString() {
			return "load \"" + name + "\"";
		}
	}

	static class Store implements Operand {
		public final String name;
		public final Slot slot;

		private Store(final String name, final Slot slot) {
			this.name = name;
			this.slot = slot;
		}

		@Override
		public String toString() {
			return "store \"" + name + "\"";
		}
	}

	static class Move implements Operand {
		public final Slot src;
		public final Slot dst;

		private Move(final Slot src, final Slot dst) {
			this.src = src;
			this.dst = dst;
		}

		@Override
		public String toString() {
			return "move " + src.typeClass + "->" + dst.typeClass;
		}
	}

	static class Get implements Operand {
		public final String rel;
		public final Slot slot;

		private Get(final String rel, final Slot slot) {
			this.rel = rel;
			this.slot = slot;
		}

		@Override
		public String toString() {
			return rel + ":" + slot.typeClass;
		}
	}

	static class Set implements Operand {
		public final String rel;
		public final Slot slot;

		private Set(final String rel, final Slot slot) {
			this.rel = rel;
			this.slot = slot;
		}

		@Override
		public String toString() {
			return rel + ":" + slot.typeClass;
		}
	}

	static class Ctor implements Operand {
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
		return new Builder(TypeDefBuilder.createTypeDef(lhs.getName()), TypeDefBuilder.createTypeDef(rhs.getName()));
	}

	static Builder builder(final Slot lhs, final Slot rhs) {
		return new Builder(TypeDefBuilder.createTypeDef(lhs.typeClass).bind(lhs.slotList),
				TypeDefBuilder.createTypeDef(rhs.typeClass).bind(rhs.slotList));
	}

	static class Builder {
		// コピー元
		private final TypeAccessDef provider;
		// コピー先
		private final TypeAccessDef consumer;

		private Builder(final TypeAccessDef lhs, final TypeAccessDef rhs) {
			this.provider = lhs;
			this.consumer = rhs;
		}

		public Stream<Operand> operands(final boolean requireNew) {
			final Stream.Builder<Operand> builder = Stream.builder();
			final List<List<Ops>> ctorList = new ArrayList<>();
			// copy 操作を計算する
			// ついでにコンストラクタのリストを copyOps ストリームの副作用として作ってもらう
			// 終端操作を明示的にしないと ctorList が計算されないので注意
			final List<Ops> copyOps = calcCopyOps(ctorList).collect(Collectors.toList());

			// で対象とするコンストラクタを決定する
			final List<Ops> ctor = findProbablyConstructor(ctorList);

			// clone か paste かを切り替える感じ
			if (requireNew) {
				// コンストラクタをストリームに
				if (ctor == null) {
					// 引数のあるコンストラクタがない場合
					if (consumer.hasDefaultCtor()) {
						// デフォルトコンストラクタで生成的な
						builder.accept(new Ctor("()V"));
					} else {
						throw new AbortCallException("No default constructor.");
					}
				} else {
					// コンストラクタの引数のコピー操作をストリームに
					for (Ops op : ctor) {
						final AccessEntry src = op.lhs;
						final AccessEntry dst = op.rhs;
						// push prop value
						switch (src.elementType) {
						case AccessEntry.ACE_FIELD:
						case AccessEntry.ACE_FINAL_FIELD:
							builder.accept(new Load(src.name, src.slot));
							break;
						case AccessEntry.ACE_PROP_GET:
							builder.accept(new Get(src.rel, src.slot));
							break;
						default:
							throw new IllegalStateException();
						}
						// convert and push
						builder.accept(new Move(src.slot, dst.slot));
					}

					final String desc = ctor.get(0).rhs.rel;
					// 最後にコンストラクタ呼び出しをストリームに流すよ
					builder.accept(new Ctor(desc));
				}
			}

			// コピー操作をストリームに
			copyOps.forEach(op -> {
				final AccessEntry src = op.lhs;
				final AccessEntry dst = op.rhs;

				// push prop value
				switch (src.elementType) {
				case AccessEntry.ACE_FIELD:
				case AccessEntry.ACE_FINAL_FIELD:
					builder.accept(new Load(src.name, src.slot));
					break;
				case AccessEntry.ACE_PROP_GET:
					builder.accept(new Get(src.rel, src.slot));
					break;
				default:
					throw new IllegalStateException();
				}

				// convert stack top to dst type and push
				builder.accept(new Move(src.slot, dst.slot));

				// set stack top value to dst property
				switch (dst.elementType) {
				case AccessEntry.ACE_FIELD:
					builder.accept(new Store(dst.name, dst.slot));
					break;
				case AccessEntry.ACE_PROP_SET:
					builder.accept(new Set(dst.rel, dst.slot));
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
		private Stream<Ops> calcCopyOps(final List<List<Ops>> ctorList) {
			// とりあえず getter 側のマップつくる
			// マップキーはプロパティ名
			final Map<String, AccessEntry> getter = provider.accessors().filter(acc -> acc.canGet)
					.collect(Collectors.toMap(acc -> acc.name, acc -> acc));
			// Ops のマップを作る
			// マップキーはコンストラクタをシグネチャでまとめたいので rel を使用する
			final Map<String, List<Ops>> opsGroup = consumer.accessors().filter(acc -> acc.canSet)
					// setter に対応する getter があるものだけにして
					.filter(acc -> getter.containsKey(acc.name))
					// getter -> setter で Ops を作って
					.map(acc -> new Ops(getter.get(acc.name), acc))
					// setter 側の rel でまとめ上げる
					// ちな rel は下のどれか
					//// コンストラクタ -> (...)V
					//// アクセッサ -> set* get*
					//// フィールド -> name と同じ
					.collect(Collectors.groupingBy(op -> op.rhs.rel));

			return opsGroup.entrySet().stream().filter(entry -> {
				final String sig = entry.getKey();
				if (sig.startsWith("(")) {
					// コンストラクタの場合、メソッドシグネチャと get -> set をまとめた Ops の数が一致してるものだけ
					// ctorList にいれておく
					final List<Ops> cc = entry.getValue();
					final int argSize = (Type.getArgumentsAndReturnSizes(sig) >> 2) - 1/* without this */;
					if (argSize == cc.size()) {
						// exact matched constructor
						ctorList.add(cc);
					}
					return false;
				}
				return true;
			}).map(entry -> entry.getValue()).flatMap(list -> list.stream());
		}

		/** 一番確からしいコンストラクタをとる */
		private static List<Ops> findProbablyConstructor(List<List<Ops>> ctorOps) {
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

		/** lhs -> rhs へのコピー操作 */
		private static class Ops {
			final AccessEntry lhs;
			final AccessEntry rhs;

			private Ops(AccessEntry lhs, AccessEntry rhs) {
				this.lhs = lhs;
				this.rhs = rhs;
			}
		}

	}
}