package ReIW.tiny.cloneAny.compile.operand;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.objectweb.asm.Type;

import ReIW.tiny.cloneAny.compile.ConversionRule;
import ReIW.tiny.cloneAny.pojo.Accessor;
import ReIW.tiny.cloneAny.pojo.Accessor.*;
import ReIW.tiny.cloneAny.pojo.Accessor.FieldAccess;
import ReIW.tiny.cloneAny.pojo.Accessor.LumpSetAccess;
import ReIW.tiny.cloneAny.pojo.Accessor.PropAccess;
import ReIW.tiny.cloneAny.pojo.Accessor.SequentialAccess;
import ReIW.tiny.cloneAny.pojo.ClassTypeAccess;
import ReIW.tiny.cloneAny.pojo.Slot;

public class OperandGenerator {
	private final ClassTypeAccess lhs;
	private final ClassTypeAccess rhs;
	private final boolean lenient;

	private final Map<String, Accessor> leftHandSide;
	private final Map<AccessType, List<Accessor>> rightGroupMap;

	public OperandGenerator(final ClassTypeAccess lhs, final ClassTypeAccess rhs, boolean lenient) {
		this.lhs = lhs;
		this.rhs = rhs;
		this.lenient = lenient;
		leftHandSide = lhs.accessors().filter(Accessor::canRead)
				.collect(Collectors.toUnmodifiableMap(Accessor::getName, Function.identity()));
		rightGroupMap = rhs.accessors().collect(Collectors.groupingBy(Accessor::getType));
	}

	public Stream<Operand> stream() {
		final Optional<LumpSetAccess> ctor = rightGroupMap.computeIfAbsent(AccessType.LumpSet, (type) -> List.of())
				.stream().map(LumpSetAccess.class::cast).filter(this::isEffectiveCtor).max(this::longestArguments);
		if (ctor.isEmpty()) {
			// no matching constroctor

		}

		rightGroupMap.entrySet().stream().filter(acc -> acc.getKey() != AccessType.LumpSet)
				.flatMap(acc -> acc.getValue().stream());

		return null;
	}

	private int longestArguments(final LumpSetAccess o1, final LumpSetAccess o2) {
		int i1 = Type.getArgumentsAndReturnSizes(o1.rel) >> 2;
		int i2 = Type.getArgumentsAndReturnSizes(o2.rel) >> 2;
		return i1 - i2;
	}

	private boolean isEffectiveCtor(final LumpSetAccess lump) {
		for (final var entry : lump.parameters.entrySet()) {
			if (leftHandSide.containsKey(entry.getKey())) {
				// @keyed ってパラメタ名はありえんので必ず single slot が取得できるはず
				final Slot fromSlot = getSingleSlot(leftHandSide.get(entry.getKey()));
				final Slot toSlot = entry.getValue();
				final Optional<Boolean> op = ConversionRule.canMove(fromSlot, toSlot, lenient);
				if (op.isPresent() && op.get() == false) {
					return false;
				}
			} else {
				// そもそもマッチするアクセサがない
				return false;
			}
		}
		return true;

	}

	private static Slot getSingleSlot(Accessor acc) {
		switch (acc.getType()) {
		case Setter:
		case Getter:
			final PropAccess prop = (PropAccess) acc;
			return prop.slot;
		case Field:
		case ReadonlyField:
			final FieldAccess fld = (FieldAccess) acc;
			return fld.slot;
		case ArrayType:
		case ListType:
		case SetType:
			final SequentialAccess seq = (SequentialAccess) acc;
			return seq.elementSlot;
		case MapType:
			final KeyedAccess keyled = (KeyedAccess) acc;
			return keyled.valueSlot;
		default:
			throw new IllegalArgumentException();
		}
	}

}
