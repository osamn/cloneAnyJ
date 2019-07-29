package ReIW.tiny.cloneAny.pojo

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

import spock.lang.Specification

class OperandSpec extends Specification{

	def "コンストラクタで注入"() {
		setup:
		def ops = Operand.builder(Operand_Lhs.class, Operand_Rhs_Ctor.class)
				.operands(true).toArray()

		expect:
		Operand.Push.isCase(ops[0])
		ops[0].src.typeClass == "I"
		ops[0].dst.typeClass == "java/lang/Long"

		and:
		Operand.Load.isCase(ops[1])
		ops[1].name == "foo"

		and:
		Operand.Push.isCase(ops[2])
		ops[2].src.typeClass == "java/lang/String"
		ops[2].dst.typeClass == "java/lang/Number"

		and:
		Operand.Load.isCase(ops[3])
		ops[3].name == "bar"

		and:
		Operand.Push.isCase(ops[4])
		ops[4].src.typeClass == "Z"
		ops[4].dst.typeClass == "java/lang/Boolean"

		and:
		Operand.Get.isCase(ops[5])
		ops[5].rel == "isBuz"

		and:
		Operand.Ctor.isCase(ops[6])
		ops[6].descriptor == "(Ljava/lang/Long;Ljava/lang/Number;Ljava/lang/Boolean;)V"
	}

	def "setter で注入"() {
		setup:
		def ops = Operand.builder(Operand_Lhs.class, Operand_Rhs_Setter.class)
				.operands(false).toArray()

		expect:
		Operand.Move.isCase(ops[0])
		ops[0].src.typeClass == "Z"
		ops[0].dst.typeClass == "java/lang/Boolean"

		and:
		Operand.Get.isCase(ops[1])
		ops[1].rel == "isBuz"

		and:
		Operand.Set.isCase(ops[2])
		ops[2].rel == "setBuz"

		and:
		Operand.Move.isCase(ops[3])
		ops[3].src.typeClass == "java/lang/Long"
		ops[3].dst.typeClass == "J"

		and:
		Operand.Get.isCase(ops[4])
		ops[4].rel == "getHoge"

		and:
		Operand.Set.isCase(ops[5])
		ops[5].rel == "setHoge"
	}

	def "フィールドに注入"() {
		setup:
		def ops = Operand.builder(Operand_Lhs.class, Operand_Rhs_Field.class)
				.operands(true).toArray()

		expect:
		Operand.Ctor.isCase(ops[0])

		and:
		Operand.Move.isCase(ops[1])
		ops[1].src.typeClass == "java/lang/String"
		ops[1].dst.typeClass == "I"

		and:
		Operand.Load.isCase(ops[2])
		ops[2].name == "bar"

		and:
		Operand.Store.isCase(ops[3])
		ops[3].name == "bar"

		and:
		Operand.Move.isCase(ops[4])
		ops[4].src.typeClass == "java/lang/Long"
		ops[4].dst.typeClass == "java/lang/String"

		and:
		Operand.Get.isCase(ops[5])
		ops[5].rel == "getHoge"

		and:
		Operand.Store.isCase(ops[6])
		ops[6].name == "hoge"
	}

	def "あいまいでコンストラクタを決定できない"() {
		when:
		Operand.builder(Operand_Lhs.class, Operand_Rhs_AmbiguousCtor.class)
				.operands(true)

		then:
		thrown(AbortCallException)
	}
}
