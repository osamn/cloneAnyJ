package ReIW.tiny.cloneAny.compile

import org.objectweb.asm.Type

import spock.lang.Shared
import spock.lang.Specification

class ConversionRuleSpec extends Specification {

	def "SYSTEM_COMPAT primitive 数値以外"() {
		expect:
		ConversionRule.SYSTEM_COMPAT.get(lhs).test(rhs) == compat

		where:
		lhs			| rhs			|| compat
		_Z			| _Z			|| false
		_Z			| _Boolean		|| true
		_Z			| _String		|| true

		_C			| _C			|| false
		_C			| _Character	|| true
		_C			| _String		|| true
	}

	def "SYSTEM_COMPAT primitive 数値型"() {
		expect:
		ConversionRule.SYSTEM_COMPAT.get(lhs).test(rhs) == compat

		where:
		lhs			| rhs			|| compat
		_B			| _B			|| false
		_B			| _S			|| true
		_B			| _I			|| true
		_B			| _J			|| true
		_B			| _F			|| true
		_B			| _D			|| true
		_B			| _Byte			|| true
		_B			| _Short		|| true
		_B			| _Integer		|| true
		_B			| _Long			|| true
		_B			| _Float		|| true
		_B			| _Double		|| true
		_B			| _BigDecimal	|| true
		_B			| _BigInteger	|| true
		_B			| _String		|| true

		_S			| _B			|| false
		_S			| _S			|| false
		_S			| _I			|| true
		_S			| _J			|| true
		_S			| _F			|| true
		_S			| _D			|| true
		_S			| _Byte			|| false
		_S			| _Short		|| true
		_S			| _Integer		|| true
		_S			| _Long			|| true
		_S			| _Float		|| true
		_S			| _Double		|| true
		_S			| _BigDecimal	|| true
		_S			| _BigInteger	|| true
		_S			| _String		|| true

		_I			| _B			|| false
		_I			| _S			|| false
		_I			| _I			|| false
		_I			| _J			|| true
		_I			| _F			|| true
		_I			| _D			|| true
		_I			| _Byte			|| false
		_I			| _Short		|| false
		_I			| _Integer		|| true
		_I			| _Long			|| true
		_I			| _Float		|| true
		_I			| _Double		|| true
		_I			| _BigDecimal	|| true
		_I			| _BigInteger	|| true
		_I			| _String		|| true

		_J			| _B			|| false
		_J			| _S			|| false
		_J			| _I			|| false
		_J			| _J			|| false
		_J			| _F			|| true
		_J			| _D			|| true
		_J			| _Byte			|| false
		_J			| _Short		|| false
		_J			| _Integer		|| false
		_J			| _Long			|| true
		_J			| _Float		|| true
		_J			| _Double		|| true
		_J			| _BigDecimal	|| true
		_J			| _BigInteger	|| true
		_J			| _String		|| true

		_F			| _B			|| false
		_F			| _S			|| false
		_F			| _I			|| false
		_F			| _J			|| false
		_F			| _F			|| false
		_F			| _D			|| true
		_F			| _Byte			|| false
		_F			| _Short		|| false
		_F			| _Integer		|| false
		_F			| _Long			|| false
		_F			| _Float		|| true
		_F			| _Double		|| true
		_F			| _BigDecimal	|| true
		_F			| _BigInteger	|| false
		_F			| _String		|| true

		_D			| _B			|| false
		_D			| _S			|| false
		_D			| _I			|| false
		_D			| _J			|| false
		_D			| _F			|| false
		_D			| _D			|| false
		_D			| _Byte			|| false
		_D			| _Short		|| false
		_D			| _Integer		|| false
		_D			| _Long			|| false
		_D			| _Float		|| false
		_D			| _Double		|| true
		_D			| _BigDecimal	|| true
		_D			| _BigInteger	|| false
		_D			| _String		|| true
	}

	def "SYSTEM_COMPAT boxing 数値以外"() {
		expect:
		ConversionRule.SYSTEM_COMPAT.get(lhs).test(rhs) == compat

		where:
		lhs			| rhs			|| compat
		_Boolean	| _Boolean		|| false
		_Boolean	| _Z			|| true
		_Boolean	| _String		|| true

		_Character	| _Character	|| false
		_Character	| _C			|| true
		_Character	| _String		|| true

	}

	def "SYSTEM_COMPAT boxing 数値型"() {
		expect:
		ConversionRule.SYSTEM_COMPAT.get(lhs).test(rhs) == compat

		where:
		lhs			| rhs			|| compat
		_Byte		| _B			|| true
		_Byte		| _S			|| true
		_Byte		| _I			|| true
		_Byte		| _J			|| true
		_Byte		| _F			|| true
		_Byte		| _D			|| true
		_Byte		| _Byte			|| false
		_Byte		| _Short		|| true
		_Byte		| _Integer		|| true
		_Byte		| _Long			|| true
		_Byte		| _Float		|| true
		_Byte		| _Double		|| true
		_Byte		| _BigDecimal	|| true
		_Byte		| _BigInteger	|| true
		_Byte		| _String		|| true

		_Short		| _B			|| false
		_Short		| _S			|| true
		_Short		| _I			|| true
		_Short		| _J			|| true
		_Short		| _F			|| true
		_Short		| _D			|| true
		_Short		| _Byte			|| false
		_Short		| _Short		|| false
		_Short		| _Integer		|| true
		_Short		| _Long			|| true
		_Short		| _Float		|| true
		_Short		| _Double		|| true
		_Short		| _BigDecimal	|| true
		_Short		| _BigInteger	|| true
		_Short		| _String		|| true

		_Integer	| _B			|| false
		_Integer	| _S			|| false
		_Integer	| _I			|| true
		_Integer	| _J			|| true
		_Integer	| _F			|| true
		_Integer	| _D			|| true
		_Integer	| _Byte			|| false
		_Integer	| _Short		|| false
		_Integer	| _Integer		|| false
		_Integer	| _Long			|| true
		_Integer	| _Float		|| true
		_Integer	| _Double		|| true
		_Integer	| _BigDecimal	|| true
		_Integer	| _BigInteger	|| true
		_Integer	| _String		|| true

		_Long		| _B			|| false
		_Long		| _S			|| false
		_Long		| _I			|| false
		_Long		| _J			|| true
		_Long		| _F			|| true
		_Long		| _D			|| true
		_Long		| _Byte			|| false
		_Long		| _Short		|| false
		_Long		| _Integer		|| false
		_Long		| _Long			|| false
		_Long		| _Float		|| true
		_Long		| _Double		|| true
		_Long		| _BigDecimal	|| true
		_Long		| _BigInteger	|| true
		_Long		| _String		|| true

		_Float		| _B			|| false
		_Float		| _S			|| false
		_Float		| _I			|| false
		_Float		| _J			|| false
		_Float		| _F			|| true
		_Float		| _D			|| true
		_Float		| _Byte			|| false
		_Float		| _Short		|| false
		_Float		| _Integer		|| false
		_Float		| _Long			|| false
		_Float		| _Float		|| false
		_Float		| _Double		|| true
		_Float		| _BigDecimal	|| true
		_Float		| _BigInteger	|| false
		_Float		| _String		|| true

		_Double		| _B			|| false
		_Double		| _S			|| false
		_Double		| _I			|| false
		_Double		| _J			|| false
		_Double		| _F			|| false
		_Double		| _D			|| true
		_Double		| _Byte			|| false
		_Double		| _Short		|| false
		_Double		| _Integer		|| false
		_Double		| _Long			|| false
		_Double		| _Float		|| false
		_Double		| _Double		|| false
		_Double		| _BigDecimal	|| true
		_Double		| _BigInteger	|| false
		_Double		| _String		|| true

	}

	@Shared String _Z = Type.getDescriptor(boolean)
	@Shared String _C = Type.getDescriptor(char)

	@Shared String _B = Type.getDescriptor(byte)
	@Shared String _S = Type.getDescriptor(short)
	@Shared String _I = Type.getDescriptor(int)
	@Shared String _J = Type.getDescriptor(long)
	@Shared String _F = Type.getDescriptor(float)
	@Shared String _D = Type.getDescriptor(double)

	@Shared String _Boolean = Type.getDescriptor(java.lang.Boolean)
	@Shared String _Character = Type.getDescriptor(java.lang.Character)

	@Shared String _Byte = Type.getDescriptor(java.lang.Byte)
	@Shared String _Short = Type.getDescriptor(java.lang.Short)
	@Shared String _Integer = Type.getDescriptor(java.lang.Integer)
	@Shared String _Long = Type.getDescriptor(java.lang.Long)
	@Shared String _Float = Type.getDescriptor(java.lang.Float)
	@Shared String _Double = Type.getDescriptor(java.lang.Double)
	@Shared String _BigDecimal = Type.getDescriptor(java.math.BigDecimal)
	@Shared String _BigInteger = Type.getDescriptor(java.math.BigInteger)

	@Shared String _URI = Type.getDescriptor(java.net.URI)
	@Shared String _URL = Type.getDescriptor(java.net.URL)
	@Shared String _File = Type.getDescriptor(java.io.File)
	@Shared String _Path = Type.getDescriptor(java.nio.file.Path)

	@Shared String _Date = Type.getDescriptor(java.util.Date)
	@Shared String _SqlDate = Type.getDescriptor(java.sql.Date)
	@Shared String _Instant = Type.getDescriptor(java.time.Instant)
	@Shared String _LocalDateTime = Type.getDescriptor(java.time.LocalDateTime)
	@Shared String _LocalDate = Type.getDescriptor(java.time.LocalDate)
	@Shared String _LocalTime = Type.getDescriptor(java.time.LocalTime)
	@Shared String _OffsetDateTime = Type.getDescriptor(java.time.OffsetDateTime)
	@Shared String _OffsetTime = Type.getDescriptor(java.time.OffsetTime)
	@Shared String _YearMonth = Type.getDescriptor(java.time.YearMonth)
	@Shared String _Year = Type.getDescriptor(java.time.Year)
	@Shared String _ZonedDateTime = Type.getDescriptor(java.time.ZonedDateTime)

	@Shared String _Charset = Type.getDescriptor(java.nio.charset.Charset)
	@Shared String _Locale = Type.getDescriptor(java.util.Locale)
	@Shared String _Currency = Type.getDescriptor(java.util.Currency)
	@Shared String _UUID = Type.getDescriptor(java.util.UUID)

	@Shared String _String = Type.getDescriptor(java.lang.String)
}
