package ReIW.tiny.cloneAny.compile;

import static java.util.Map.entry;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import ReIW.tiny.cloneAny.pojo.Slot;

// クラス間の変換規則
public interface ConversionRule {

	// ClassType までおっかけなくてもいいやつたち
	// こいつらは基本 String と相互変換可能
	final Map<String, Predicate<String>> SYSTEM_COMPAT = Map.ofEntries(
			/*
			 * primitive types
			 */
			entry("Z", compat("Ljava/lang/Boolean;", "Ljava/lang/String;")), // boolean
			entry("C", compat("Ljava/lang/Character;", "Ljava/lang/String;")), // char
			entry("B",
					compat("S", "I", "J", "F", "D", "Ljava/lang/Byte;", "Ljava/lang/Short;", "Ljava/lang/Integer;",
							"Ljava/lang/Long;", "Ljava/lang/Float;", "Ljava/lang/Double;", "Ljava/math/BigDecimal;",
							"Ljava/math/BigInteger;", "Ljava/lang/String;")), // byte
			entry("S",
					compat("I", "J", "F", "D", "Ljava/lang/Short;", "Ljava/lang/Integer;", "Ljava/lang/Long;",
							"Ljava/lang/Float;", "Ljava/lang/Double;", "Ljava/math/BigDecimal;",
							"Ljava/math/BigInteger;", "Ljava/lang/String;")), // short
			entry("I", compat("J", "F", "D", "Ljava/lang/Integer;", "Ljava/lang/Long;", "Ljava/lang/Float;",
					"Ljava/lang/Double;", "Ljava/math/BigDecimal;", "Ljava/math/BigInteger;", "Ljava/lang/String;")), // int
			entry("J",
					compat("F", "D", "Ljava/lang/Long;", "Ljava/lang/Float;", "Ljava/lang/Double;",
							"Ljava/math/BigDecimal;", "Ljava/math/BigInteger;", "Ljava/lang/String;")), // long
			entry("F",
					compat("D", "Ljava/lang/Float;", "Ljava/lang/Double;", "Ljava/math/BigDecimal;",
							"Ljava/lang/String;")), // float
			entry("D", compat("Ljava/lang/Double;", "Ljava/math/BigDecimal;", "Ljava/lang/String;")), // double
			/*
			 * Boxing types
			 */
			entry("Ljava/lang/Boolean;", compat("Z", "Ljava/lang/String;")),
			entry("Ljava/lang/Character;", compat("C", "Ljava/lang/String;")),
			entry("Ljava/lang/Byte;",
					compat("B", "S", "I", "J", "F", "D", "Ljava/lang/Short;", "Ljava/lang/Integer;", "Ljava/lang/Long;",
							"Ljava/lang/Float;", "Ljava/lang/Double;", "Ljava/math/BigDecimal;",
							"Ljava/math/BigInteger;", "Ljava/lang/String;")),
			entry("Ljava/lang/Short;",
					compat("S", "I", "J", "F", "D", "Ljava/lang/Integer;", "Ljava/lang/Long;", "Ljava/lang/Float;",
							"Ljava/lang/Double;", "Ljava/math/BigDecimal;", "Ljava/math/BigInteger;",
							"Ljava/lang/String;")),
			entry("Ljava/lang/Integer;",
					compat("I", "J", "F", "D", "Ljava/lang/Long;", "Ljava/lang/Float;", "Ljava/lang/Double;",
							"Ljava/math/BigDecimal;", "Ljava/math/BigInteger;", "Ljava/lang/String;")),
			entry("Ljava/lang/Long;",
					compat("J", "F", "D", "Ljava/lang/Float;", "Ljava/lang/Double;", "Ljava/math/BigDecimal;",
							"Ljava/math/BigInteger;", "Ljava/lang/String;")),
			entry("Ljava/lang/Float;",
					compat("F", "D", "Ljava/lang/Double;", "Ljava/math/BigDecimal;", "Ljava/lang/String;")),
			entry("Ljava/lang/Double;", compat("D", "Ljava/math/BigDecimal;", "Ljava/lang/String;")),
			/*
			 * math types
			 */
			entry("Ljava/math/BigDecimal;", compat("Ljava/math/BigDecimal;", "Ljava/lang/String;")),
			entry("Ljava/math/BigInteger;",
					compat("Ljava/math/BigInteger;", "Ljava/math/BigDecimal;", "Ljava/lang/String;")),
			/*
			 * Locations
			 */
			entry("Ljava/net/URI;",
					compat("Ljava/net/URL;", "Ljava/io/File;", "Ljava/nio/file/Path;", "Ljava/lang/String;")),
			entry("Ljava/net/URL;",
					compat("Ljava/net/URI;", "Ljava/io/File;", "Ljava/nio/file/Path;", "Ljava/lang/String;")),
			entry("Ljava/io/File;",
					compat("Ljava/nio/file/Path;", "Ljava/net/URL;", "Ljava/net/URI;", "Ljava/lang/String;")),
			entry("Ljava/nio/file/Path;",
					compat("Ljava/io/File;", "Ljava/net/URL;", "Ljava/net/URI;", "Ljava/lang/String;")),
			/*
			 * Date and time
			 */
			entry("Ljava/util/Date;",
					compat("Ljava/sql/Date;", "Ljava/time/Instant;", "Ljava/time/LocalDateTime;",
							"Ljava/time/LocalDate;", "Ljava/time/LocalTime;", "Ljava/time/OffsetDateTime;",
							"Ljava/time/OffsetTime;", "Ljava/time/YearMonth;", "Ljava/time/Year;",
							"Ljava/time/ZonedDateTime;", "Ljava/lang/String;")),
			entry("Ljava/sql/Date;",
					compat("Ljava/util/Date;", "Ljava/time/Instant;", "Ljava/time/LocalDateTime;",
							"Ljava/time/LocalDate;", "Ljava/time/LocalTime;", "Ljava/time/OffsetDateTime;",
							"Ljava/time/OffsetTime;", "Ljava/time/YearMonth;", "Ljava/time/Year;",
							"Ljava/time/ZonedDateTime;", "Ljava/lang/String;")),
			entry("Ljava/time/Instant;",
					compat("Ljava/util/Date;", "Ljava/sql/Date;", "Ljava/time/LocalDateTime;", "Ljava/time/LocalDate;",
							"Ljava/time/LocalTime;", "Ljava/time/OffsetDateTime;", "Ljava/time/OffsetTime;",
							"Ljava/time/YearMonth;", "Ljava/time/Year;", "Ljava/time/ZonedDateTime;",
							"Ljava/lang/String;")),
			entry("Ljava/time/LocalDateTime;",
					compat("Ljava/util/Date;", "Ljava/sql/Date;", "Ljava/time/Instant;", "Ljava/time/LocalDate;",
							"Ljava/time/LocalTime;", "Ljava/time/OffsetDateTime;", "Ljava/time/OffsetTime;",
							"Ljava/time/YearMonth;", "Ljava/time/Year;", "Ljava/time/ZonedDateTime;",
							"Ljava/lang/String;")),
			entry("Ljava/time/LocalDate;",
					compat("Ljava/util/Date;", "Ljava/sql/Date;", "Ljava/time/Instant;", "Ljava/time/LocalDateTime;",
							"Ljava/time/LocalTime;", "Ljava/time/OffsetDateTime;", "Ljava/time/OffsetTime;",
							"Ljava/time/YearMonth;", "Ljava/time/Year;", "Ljava/time/ZonedDateTime;",
							"Ljava/lang/String;")),
			entry("Ljava/time/LocalTime;",
					compat("Ljava/util/Date;", "Ljava/sql/Date;", "Ljava/time/Instant;", "Ljava/time/LocalDateTime;",
							"Ljava/time/LocalDate;", "Ljava/time/LocalTime;", "Ljava/time/OffsetDateTime;",
							"Ljava/time/OffsetTime;", "Ljava/time/YearMonth;", "Ljava/time/Year;",
							"Ljava/time/ZonedDateTime;", "Ljava/lang/String;")),
			entry("Ljava/time/OffsetDateTime;",
					compat("Ljava/util/Date;", "Ljava/sql/Date;", "Ljava/time/Instant;", "Ljava/time/LocalDateTime;",
							"Ljava/time/LocalDate;", "Ljava/time/LocalTime;", "Ljava/time/OffsetTime;",
							"Ljava/time/YearMonth;", "Ljava/time/Year;", "Ljava/time/ZonedDateTime;",
							"Ljava/lang/String;")),
			entry("Ljava/time/OffsetTime;",
					compat("Ljava/util/Date;", "Ljava/sql/Date;", "Ljava/time/Instant;", "Ljava/time/LocalDateTime;",
							"Ljava/time/LocalDate;", "Ljava/time/LocalTime;", "Ljava/time/OffsetDateTime;",
							"Ljava/time/YearMonth;", "Ljava/time/Year;", "Ljava/time/ZonedDateTime;",
							"Ljava/lang/String;")),
			entry("Ljava/time/YearMonth;",
					compat("Ljava/util/Date;", "Ljava/sql/Date;", "Ljava/time/Instant;", "Ljava/time/LocalDateTime;",
							"Ljava/time/LocalDate;", "Ljava/time/LocalTime;", "Ljava/time/OffsetDateTime;",
							"Ljava/time/OffsetTime;", "Ljava/time/Year;", "Ljava/time/ZonedDateTime;",
							"Ljava/lang/String;")),
			entry("Ljava/time/Year;",
					compat("Ljava/util/Date;", "Ljava/sql/Date;", "Ljava/time/Instant;", "Ljava/time/LocalDateTime;",
							"Ljava/time/LocalDate;", "Ljava/time/LocalTime;", "Ljava/time/OffsetDateTime;",
							"Ljava/time/OffsetTime;", "Ljava/time/YearMonth;", "Ljava/time/ZonedDateTime;",
							"Ljava/lang/String;")),
			entry("Ljava/time/ZonedDateTime;",
					compat("Ljava/util/Date;", "Ljava/sql/Date;", "Ljava/time/Instant;", "Ljava/time/LocalDateTime;",
							"Ljava/time/LocalDate;", "Ljava/time/LocalTime;", "Ljava/time/OffsetDateTime;",
							"Ljava/time/OffsetTime;", "Ljava/time/YearMonth;", "Ljava/time/Year;",
							"Ljava/lang/String;")),
			/*
			 * else
			 */
			entry("Ljava/nio/charset/Charset;", compat("Ljava/lang/String;")),
			entry("Ljava/util/Locale;", compat("Ljava/lang/String;")),
			entry("Ljava/util/Currency;", compat("Ljava/lang/String;")),
			entry("Ljava/util/UUID;", compat("Ljava/lang/String;")),
			/*
			 * String はなんにでも変換できる
			 */
			entry("Ljava/lang/String;",
					compat("Z", "C", "B", "S", "I", "J", "F", "D", "Ljava/lang/Boolean;", "Ljava/lang/Character;",
							"Ljava/lang/Byte;", "Ljava/lang/Short;", "Ljava/lang/Integer;", "Ljava/lang/Long;",
							"Ljava/lang/Float;", "Ljava/lang/Double;", "Ljava/math/BigDecimal;",
							"Ljava/math/BigInteger;", "Ljava/util/Date;", "Ljava/sql/Date;", "Ljava/time/Instant;",
							"Ljava/time/LocalDateTime;", "Ljava/time/LocalDate;", "Ljava/time/LocalTime;",
							"Ljava/time/OffsetDateTime;", "Ljava/time/OffsetTime;", "Ljava/time/YearMonth;",
							"Ljava/time/Year;", "Ljava/time/ZonedDateTime;")));

	// TODO Enum は ClassType で Ljava/lang/Enum; みないといかんみたい
	// TODO EnumSet は Set としてみなせるよ

	// OK NG 不明にしたいので Optional にしとく
	// lenient は数値の拡大方向と同系の相互変換をゆるす感じ
	static Optional<Boolean> canMove(final Slot lhs, final Slot rhs, final boolean lenient) {
		final String lhsSig = lhs.getSignature();
		final String rhsSig = rhs.getSignature();

		if (lhsSig.contentEquals(rhsSig)) {
			// signature レベルで完全に一致していたらとりあえずOK
			return Optional.of(Boolean.TRUE);
		}

		final String lhsDesc = lhs.getDescriptor();
		final String rhsDesc = rhs.getDescriptor();

		// 配列がらみを判定する
		if (lhs.isArray() && rhs.isArray()) {
			// ここで element slot は評価しない
			// ClassType から indexed のアクセサみて再評価する
			return Optional.empty();
		} else if (lhs.isArray()) {
			if (SYSTEM_COMPAT.containsKey(rhsDesc)) {
				// 配列 -> leaf type なので変換不可
				return Optional.of(Boolean.FALSE);
			} else {
				// 配列 -> List, Set の可能性があるよ
				return Optional.empty();
			}
		} else if (rhs.isArray()) {
			if (SYSTEM_COMPAT.containsKey(lhsDesc)) {
				// leaf type -> 配列なので変換不可
				return Optional.of(Boolean.FALSE);
			} else {
				// List, Set -> 配列の可能性がある
				return Optional.empty();
			}
		}

		if (lenient) {
			// ゆるい変換の場合は compatible か定義のほうからしらべる
			if (SYSTEM_COMPAT.containsKey(lhsDesc)) {
				return Optional.of(SYSTEM_COMPAT.get(lhsDesc).test(rhsDesc));
			}
		}

		// CharSequence -> String, Enum -> String の可能性をしらべる
		if (rhsDesc.contentEquals("Ljava/lang/String;")) {
			// TODO けすよ
			return Optional.empty();
		}

		// String -> Enum の可能性をしらべる
		if (lhsDesc.contentEquals("Ljava/lang/String;")) {
			// TODO けすよ
			return Optional.empty();
		}

		return Optional.empty();
	}

	private static Predicate<String> compat(final String... compatList) {
		Arrays.sort(compatList);
		return val -> {
			return (Arrays.binarySearch(compatList, val) >= 0);
		};
	}

}
