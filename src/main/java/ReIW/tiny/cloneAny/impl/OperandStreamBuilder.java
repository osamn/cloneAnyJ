package ReIW.tiny.cloneAny.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import ReIW.tiny.cloneAny.pojo.AbortCallException;
import ReIW.tiny.cloneAny.pojo.AccessEntry;

// TODO ここで転送可能かみて、ストリームを調整する

final class OperandStreamBuilder {

	/*
	static OperandStreamBuilder builder(final Class<?> lhs, final Class<?> rhs) {
		return new OperandStreamBuilder(TypeDefBuilder.createTypeDef(lhs), TypeDefBuilder.createTypeDef(rhs));
	}

	static OperandStreamBuilder builder(final Slot lhs, final Slot rhs) {
		final String lhsName = Type.getType(lhs.descriptor).getInternalName();
		final String rhsName = Type.getType(lhs.descriptor).getInternalName();
		return new OperandStreamBuilder(TypeDefBuilder.createTypeDef(lhsName).bind(lhs.slotList),
				TypeDefBuilder.createTypeDef(rhsName).bind(rhs.slotList));
	}

	// コピー元
	private final TypeAccessDef_ provider;
	// コピー先
	private final TypeAccessDef_ consumer;

	OperandStreamBuilder(final TypeAccessDef_ lhs, final TypeAccessDef_ rhs) {
		this.provider = lhs;
		this.consumer = rhs;
	}

	Stream<Operand> operands(final boolean requireNew) {
		// 副作用として計算されるコンストラクタの計算結果を保持しておくリスト
		final List<List<OperandStreamBuilder.Ops>> ctorList = new ArrayList<>();

		// 一旦 AccessEntry のペア(=Ops)のストリームを計算する
		final List<OperandStreamBuilder.Ops> copyOps = calcCopyAndInit(ctorList)
				// 終端操作を明示的にしないと ctorList が計算されないので注意
				.collect(Collectors.toList());

		// 必要に応じてコンストラクタ呼び出しオペランドのストリームを作る
		final Stream<Operand> ctor;
		if (requireNew) {
			// 対象とするコンストラクタを決定して
			final List<OperandStreamBuilder.Ops> exactCtor = findProbablyConstructor(ctorList);
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

	// load/get
	// push
	// load/get
	// push
	// ...
	// ctor
	private Stream<Operand> buildCtorStream(final List<OperandStreamBuilder.Ops> ctor) {
		final Stream.Builder<Operand> builder = Stream.builder();

		// コンストラクタの引数のコピー操作をストリームに
		for (OperandStreamBuilder.Ops op : ctor) {
			// TODO ここで変換できるか確認して変換できなかったらスキップ

			final AccessEntry src = op.lhs;
			final AccessEntry dst = op.rhs;

			// push prop value
			switch (src.elementType) {
			case AccessEntry.ACE_FIELD:
			case AccessEntry.ACE_FINAL_FIELD:
				builder.accept(new Load(provider.getInternalName(), src.name, src.slot.descriptor));
				break;
			case AccessEntry.ACE_PROP_GET:
				if (src.name.contentEquals("*")) {
					// Map 関連はプロパティ名持ってないので相手側の名前を使う
					builder.accept(new GetKey(dst.name));
				} else {
					builder.accept(new Get(provider.getInternalName(), src.rel, src.slot.descriptor));
				}
				break;
			default:
				throw new IllegalStateException();
			}

			// convert stack top
			builder.accept(new ConvTop(src.slot, dst.slot));
		}

		final String desc = ctor.get(0).rhs.rel;
		// 最後にコンストラクタ呼び出しをストリームに流す
		builder.accept(new Ctor(desc));

		return builder.build();
	}

	// load/get
	// move
	// store/set
	// ...
	private Stream<Operand> buildCopyStream(final List<OperandStreamBuilder.Ops> ops) {
		final Stream.Builder<Operand> builder = Stream.builder();

		// コピー操作をストリームに
		ops.forEach(op -> {
			// TODO ここで変換できるか確認して変換できなかったらスキップ

			final AccessEntry src = op.lhs;
			final AccessEntry dst = op.rhs;

			// push prop value
			switch (src.elementType) {
			case AccessEntry.ACE_FIELD:
			case AccessEntry.ACE_FINAL_FIELD:
				builder.accept(new Load(provider.getInternalName(), src.name, src.slot.descriptor));
				break;
			case AccessEntry.ACE_PROP_GET:
				if (src.name.contentEquals("*")) {
					// Map 関連はプロパティ名持ってないので相手側の名前を使う
					builder.accept(new GetKey(dst.name));
				} else {
					builder.accept(new Get(provider.getInternalName(), src.rel, src.slot.descriptor));
				}
				break;
			default:
				throw new IllegalStateException();
			}

			// convert stack top
			builder.accept(new ConvTop(src.slot, dst.slot));

			// set stack top value to dst property
			switch (dst.elementType) {
			case AccessEntry.ACE_FIELD:
				builder.accept(new Store(consumer.getInternalName(), dst.name, dst.slot.descriptor));
				break;
			case AccessEntry.ACE_PROP_SET:
				if (dst.name.contentEquals("*")) {
					// Map 関連はプロパティ名持ってないので相手側の名前を使う
					builder.accept(new SetKey(src.name));
				} else {
					builder.accept(new Set(consumer.getInternalName(), dst.rel, dst.slot.descriptor));
				}
				break;
			default:
				throw new IllegalStateException();
			}
		});

		return builder.build();
	}
	*/

	/**
	 * コンストラクタを除いたコピー操作のストリームを計算する
	 * 
	 * 副作用としてコンストラクタ操作を ctorList に設定する
	private Stream<OperandStreamBuilder.Ops> calcCopyAndInit(final List<List<OperandStreamBuilder.Ops>> ctorList) {
		// とりあえず getter 側のマップつくる
		// マップキーはプロパティ名
		final Map<String, AccessEntry> getters = provider.accessors().filter(acc -> acc.canGet)
				.collect(Collectors.toMap(get -> get.name, get -> get));
		// 最後の Map#put に残りを全部入れるために
		// getter 側で使用されたものの名前を持っておく
		final HashSet<String> processed = new HashSet<>();

		// Ops のマップを作る
		// マップキーはコンストラクタをシグネチャでまとめたいので rel つかう
		final Map<String, List<OperandStreamBuilder.Ops>> opsGroup = consumer.accessors().filter(acc -> acc.canSet)
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
						// (name or *) -> *
						final Stream.Builder<OperandStreamBuilder.Ops> rest = Stream.builder();
						getters.values().forEach(acc -> {
							if (!processed.contains(acc.name)) {
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
				// ・アクセッサ -> set* get* Map#get Map#put
				// ・フィールド -> name と同じ
				.collect(Collectors.groupingBy(op -> op.rhs.rel));

		return opsGroup.entrySet().stream().filter(entry -> {
			final String sig = entry.getKey();
			if (sig.startsWith("(")) {
				// コンストラクタの場合、メソッドシグネチャと get -> set をまとめた Ops の数が一致してるものだけ
				// ctorList にいれておく
				// ＝呼び出し時にすべての引数が設定可能なもの
				final List<OperandStreamBuilder.Ops> cc = entry.getValue().stream()// .filter(op ->
																					// !op.lhs.name.contentEquals("*"))
						.collect(Collectors.toList());
				final int argSize = (Type.getArgumentsAndReturnSizes(sig) >> 2) - 1// without this;
				if (argSize == cc.size()) {
					// exact matched constructor
					ctorList.add(cc);
				}
				return false;
			}
			return true;
		}).map(entry -> entry.getValue()).flatMap(ops -> ops.stream());
	}
	 */

	/** 一番確からしいコンストラクタをとる */
	private static List<OperandStreamBuilder.Ops> findProbablyConstructor(
			final List<List<OperandStreamBuilder.Ops>> ctorOps) {
		final Optional<Map.Entry<Integer, List<List<OperandStreamBuilder.Ops>>>> most = ctorOps.stream()
				// コンストラクタの引数の数でグルーピングして
				.collect(Collectors.groupingBy(List::size)).entrySet().stream()
				// 一番数が多いやつをとってくる
				.max(Comparator.comparing(entry -> entry.getKey()));

		if (most.isPresent()) {
			final List<List<OperandStreamBuilder.Ops>> nearest = most.get().getValue();
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