package ReIW.tiny.cloneAny.compile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.objectweb.asm.Type;

import com.thoughtworks.qdox.parser.structs.TypeDef;

import ReIW.tiny.cloneAny.pojo.Accessor;
import ReIW.tiny.cloneAny.pojo.Accessor.AccessType;
import ReIW.tiny.cloneAny.pojo.ClassTypeAccess;
import ReIW.tiny.cloneAny.pojo.Slot;

public class OperandGenerator {

	private final ClassTypeAccess lhs;
	private final ClassTypeAccess rhs;

	// 左のアクセサのパラメタ名をキーにするマップ
	// ここにない場合は lhs の Map#get からとる感じになる
	private final Map<String, Accessor> sourceAccMap;

	private final Accessor effectiveCtor;
	private final List<Accessor> effectiveDst;

	OperandGenerator(final ClassTypeAccess lhs, final ClassTypeAccess rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
		this.sourceAccMap = lhs.accessors().filter(acc -> acc.canRead())
				.collect(Collectors.toUnmodifiableMap(acc -> acc.getName(), acc -> acc));

		final OperandGenerator.MaxArgsAccessor ctorSelector = new OperandGenerator.MaxArgsAccessor();
		this.effectiveDst = this.rhs.accessors().filter(acc -> acc.canWrite()).filter(this::selectDstWithEffectiveSrc)
				.filter(acc -> {
					// コンストラクタを別途よせておいて
					if (acc.getType() == AccessType.LumpSet) {
						ctorSelector.set(acc);
						return false;
					}
					return true;
				}).collect(Collectors.toList());
		// 一番引数の長いコンストラクタをとる
		this.effectiveCtor = ctorSelector.get();
	}

	/**
	 * 対応する lhs が存在しかつ rhs に変換可能である rhs を選択する.
	 * 
	 * これにより、以降の処理で lhs の存在や変換可能性を見る必要がなくなるよ
	 */
	private boolean selectDstWithEffectiveSrc(final Accessor rhsAcc) {
		// 右側のスロット情報すべてについて左側に move 可能なソースがあるか調べる
		return rhsAcc.slotInfo().allMatch(rhsInfo -> {
			// 左側にパラメタ名前が一致するアクセサがあるかみて
			if (sourceAccMap.containsKey(rhsInfo.param)) {
				final Accessor lhsAcc = sourceAccMap.create(rhsInfo.param);
				if (canMove(asSingle(lhsAcc).slot, rhsInfo.slot)) {
					// スロットが move 可能なのでおｋ
					return true;
				}
			}
			// じゃなければ左側がマップで、その value 型で move 可能か
			if (lhs.isMap()) {
				return canMove(lhs.valueSlot(), rhsInfo.slot);
			}
			return false;
		});
	}

	// Map からコピーするときにすでにコピーしてるものは抑止するために名前をとっておく
	private final HashSet<String> used = new HashSet<>();

	public Stream<Operand> initStream() {
		if (effectiveCtor == null) {
			return null;
		}
		final ArrayList<Stream<Operand>> streams = new ArrayList<>();
		streams.add(Stream.of(new New()));

		effectiveCtor.slotInfo().forEach(rhsInfo -> {
			final String name = rhsInfo.param;
			if (sourceAccMap.containsKey(name)) {
				final Accessor rhsAcc = sourceAccMap.create(name);
				streams.add(readOps(rhsAcc));
				streams.add(convOps(asSingle(rhsAcc).slot, rhsInfo.slot));
			} else {
				// 対応する field or getter がないので Map からの転記
				streams.add(Stream.of(new CheckMapKeyExists(), new MapGet()));
				streams.add(convOps(lhs.valueSlot(), rhsInfo.slot));
			}
			used.add(name);
		});

		streams.add(Stream.of(new InvokeSpecial(), new StoreRhs()));
		return streams.stream().flatMap(Function.identity());
	}

	public Stream<Operand> copyStream() {
		final ArrayList<Stream<Operand>> streams = new ArrayList<>();

		effectiveDst.forEach(dstAcc -> {
			final String name = dstAcc.getName();
			if (sourceAccMap.containsKey(name)) {
				final Accessor srcAcc = sourceAccMap.get(name);
				streams.add(readOps(srcAcc));
				streams.add(convOps(asSingle(srcAcc).slot, asSingle(dstAcc).slot));
				streams.add(writeOps(dstAcc));
			} else {
				// 対応する field or getter がないので Map からの転記
				streams.add(Stream.of(new TestMapKeyExists(), new MapGet()));
				streams.add(convOps(lhs.valueSlot(), asSingle(dstAcc).slot));
				streams.add(writeOps(dstAcc));
				streams.add(Stream.of(new MapKeyNotExists()));
			}
			used.add(name);
		});

		/* rhs が Map の場合特別扱いで計算する */

		// any -> Map
		if (rhs.isMap()) {
			// any -> Map の場合、ここまでの処理でコピーしていない要素を右マップにコピーする
			final Slot valueSlot = rhs.valueSlot();
			sourceAccMap.entrySet().stream()
					// コピーの対象になっていない左側要素を選択して
					.filter(entry -> !used.contains(entry.getKey()))
					// そのアクセサをとって
					.map(Map.Entry::getValue)
					// 右の Map の値に変換可能なものだけ対象にして
					.filter(acc -> canMove(asSingle(acc).slot, valueSlot))
					// copy source property/filed to Map#put(name, val)
					.forEach(srcAcc -> {
						streams.add(readOps(srcAcc));
						streams.add(convOps(asSingle(srcAcc).slot, valueSlot));
						streams.add(Stream.of(new MapPut()));
					});
		}

		// Map -> Map
		if (lhs.isMap() && rhs.isMap()) {
			if (canMove(lhs.valueSlot(), rhs.valueSlot())) {
				// key loop copy Map#get(key) to Map#put(key, val) if not exists
				// ここでは実行時に入るので used による抑止がきかないけどしょうがないよねってことで
				streams.add(Stream.of(new StartKeyLoop(), new MapGet()));
				streams.add(convOps(lhs.valueSlot(), rhs.valueSlot()));
				streams.add(Stream.of(new MapPut(), new EndKeyLoop()));
			}
		}

		/* list -> list の場合も特別扱い */
		// top level で配列はありえないので List だけケアしてあげる

		if (lhs.isList() && rhs.isList()) {
			if (canMove(lhs.elementSlot(), rhs.elementSlot())) {
				streams.add(Stream.of(new StartIndexLoop(), new ListGet()));
				streams.add(convOps(lhs.elementSlot(), rhs.elementSlot()));
				streams.add(Stream.of(new ListSet(), new EndIndexLoop()));
			}
		}

		return streams.stream().flatMap(Function.identity());
	}

	private Stream<Operand> readOps(final Accessor src) {
		final Stream.Builder<Operand> ops = Stream.builder();
		ops.accept(new LoadLhs());
		final Operand read = src.getType() == Accessor.Kind.Get ? new InvokeGet() : new FieldGet();
		ops.accept(read);
		return ops.build();
	}

	private Stream<Operand> convOps(final Slot lhs, final Slot rhs) {
		final TypeDef lhsDef = TypeDef.createInstance(lhs);
		final TypeDef rhsDef = TypeDef.createInstance(rhs);
		if (lhsDef.isArrayType() || lhsDef.isList()) {
			final ArrayList<Stream<Operand>> streams = new ArrayList<>();
			final Operand getter = lhsDef.isArrayType() ? new ArrayGet() : new ListGet();
			final Operand setter = rhsDef.isArrayType() ? new ArraySet() : new ListSet();
			streams.add(Stream.of(new StartIndexLoop(), getter));
			streams.add(convOps(lhsDef.elementSlot(), rhsDef.elementSlot()));
			streams.add(Stream.of(setter, new EndIndexLoop()));
			return streams.stream().flatMap(Function.identity());
		} else {
			return Stream.of(new Convert());
		}
	}

	private Stream<Operand> writeOps(final Accessor dst) {
		final Stream.Builder<Operand> ops = Stream.builder();
		ops.accept(new LoadRhs());
		final Operand write = dst.getType() == Accessor.Kind.Set ? new InvokeGet() : new FieldGet();
		ops.accept(write);
		return ops.build();
	}

	// Accessor として抽出されたスロットの変換可能性をみる
	// Object -> Object の場合はここで true になっても右側のコンストラクタセッタあたりで
	// うまく転記できなかったら runtime 時に例外になったりするよ
	private static boolean canMove(final Slot lhs, final Slot rhs) {
		if (lhs == null || rhs == null) {
			return false;
		}
		/*
		 * if (lhs.getTypeDescriptor().contentEquals(rhs.getTypeDescriptor())) { //
		 * 型パラメタを除いて一緒なんでとりあえず true return true; }
		 */

		/* array が絡む場合は Slot#descriptor が型を示さないので先に処理する */

		// array -> array のチェック
		if (lhs.isArrayType && rhs.isArrayType) {
			return canMove(lhs.slotList.create(0), rhs.slotList.create(0));
		}

		// array -> List のチェック
		if (lhs.isArrayType) {
			final TypeDef rhsDef = TypeDef.createInstance(rhs);
			// 右側が List の場合 array と同等に生成できないといかんのでデフォルトコンストラクタが必要
			if (rhsDef.isList() && rhsDef.hasDefaultCtor()) {
				return canMove(lhs.slotList.create(0), rhsDef.elementSlot());
			}
			return false;
		}

		// List -> array のチェック
		if (rhs.isArrayType) {
			final TypeDef lhsDef = TypeDef.createInstance(lhs);
			if (lhsDef.isList()) {
				return canMove(lhsDef.elementSlot(), rhs.slotList.create(0));
			}
			return false;
		}

		/* array じゃない場合は getTypeDescriptor じゃなくて descriptor を直に扱えるよ */

		// キャスト操作のチェック
		if (lhs.isPrimitiveType && rhs.isPrimitiveType) {
			// boolean はキャストでほかの primitive に持っていけないので特別扱い
			// 片方だけ真偽値は NG
			if (lhs.descriptor.contentEquals("Z") && rhs.descriptor.contentEquals("Z")) {
				return true;
			} else if (lhs.descriptor.contentEquals("Z") || rhs.descriptor.contentEquals("Z")) {
				return false;
			}
			// それ以外はキャスト可能
			return true;
		}

		// Box 操作のチェック
		if (lhs.isPrimitiveType && rhs.isBoxingType) {
			if (lhs.descriptor.contentEquals("Z") && rhs.descriptor.contentEquals("Ljava/lang/Boolean;")) {
				return true;
			} else if (lhs.descriptor.contentEquals("Z") || rhs.descriptor.contentEquals("Ljava/lang/Boolean;")) {
				return false;
			}
			return true;
		}

		// Unbox 操作のチェック
		if (lhs.isBoxingType && rhs.isPrimitiveType) {
			if (lhs.descriptor.contentEquals("Ljava/lang/Boolean;") && rhs.descriptor.contentEquals("Z")) {
				return true;
			} else if (lhs.descriptor.contentEquals("Ljava/lang/Boolean;") || rhs.descriptor.contentEquals("Z")) {
				return false;
			}
			return true;
		}

		// * -> String のチェック
		if (rhs.descriptor.contentEquals("Ljava/lang/String;")) {
			if (lhs.isPrimitiveType || lhs.isBoxingType) {
				return true;
			}
			final TypeDef lhsDef = TypeDef.createInstance(lhs);
			if (lhsDef.isCharSequence()) {
				return true;
			}
			return false;
		}

		// CharSequence -> primitive/boxing のチェック
		if (rhs.isPrimitiveType || rhs.isBoxingType) {
			final TypeDef lhsDef = TypeDef.createInstance(lhs);
			if (lhsDef.isCharSequence()) {
				return true;
			}
			return false;
		}

		// あとは Operand 作るところで頑張る
		return true;
	}

	private static class MaxArgsAccessor {
		private Accessor curr;

		void set(final Accessor acc) {
			if (curr == null) {
				curr = acc;
				return;
			}
			final int curSize = Type.getArgumentsAndReturnSizes(curr.getDescriptor()) >> 2;
			final int accSize = Type.getArgumentsAndReturnSizes(acc.getDescriptor()) >> 2;
			if (curSize == accSize) {
				throw new IllegalArgumentException("Anbiguous constructor found.");
			}
			if (curSize < accSize) {
				curr = acc;
			}
		}

		Accessor get() {
			return curr;
		}
	}

}
