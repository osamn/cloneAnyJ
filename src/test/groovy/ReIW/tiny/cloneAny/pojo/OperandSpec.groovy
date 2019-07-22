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
		Operand.Load.isCase(ops[0])
		ops[0].name == "foo"
		ops[0].slot.typeClass == "I"

		and:
		Operand.Move.isCase(ops[1])
		ops[1].src.typeClass == "I"
		ops[1].dst.typeClass == "java/lang/Long"

		and:
		Operand.Load.isCase(ops[2])
		ops[2].name == "bar"
		ops[2].slot.typeClass == "java/lang/String"

		and:
		Operand.Move.isCase(ops[3])
		ops[3].src.typeClass == "java/lang/String"
		ops[3].dst.typeClass == "java/lang/Number"

		and:
		Operand.Get.isCase(ops[4])
		ops[4].rel == "isBuz"
		ops[4].slot.typeClass == "Z"

		and:
		Operand.Move.isCase(ops[5])
		ops[5].src.typeClass == "Z"
		ops[5].dst.typeClass == "java/lang/Boolean"

		and:
		Operand.Ctor.isCase(ops[6])
		ops[6].descriptor == "(Ljava/lang/Long;Ljava/lang/Number;Ljava/lang/Boolean;)V"
	}

	def "setter で注入"() {
		setup:
		def ops = Operand.builder(Operand_Lhs.class, Operand_Rhs_Setter.class)
				.operands(false).toArray()

		expect:
		Operand.Get.isCase(ops[0])
		ops[0].rel == "isBuz"
		ops[0].slot.typeClass == "Z"

		and:
		Operand.Move.isCase(ops[1])
		ops[1].src.typeClass == "Z"
		ops[1].dst.typeClass == "java/lang/Boolean"

		and:
		Operand.Set.isCase(ops[2])
		ops[2].rel == "setBuz"
		ops[2].slot.typeClass == "java/lang/Boolean"

		and:
		Operand.Get.isCase(ops[3])
		ops[3].rel == "getHoge"
		ops[3].slot.typeClass == "java/lang/Long"

		and:
		Operand.Move.isCase(ops[4])
		ops[4].src.typeClass == "java/lang/Long"
		ops[4].dst.typeClass == "J"

		and:
		Operand.Set.isCase(ops[5])
		ops[5].rel == "setHoge"
		ops[5].slot.typeClass == "J"
	}

	def "フィールドに注入"() {
		setup:
		def ops = Operand.builder(Operand_Lhs.class, Operand_Rhs_Field.class)
				.operands(true).toArray()

		expect:
		Operand.Ctor.isCase(ops[0])

		and:
		Operand.Load.isCase(ops[1])
		ops[1].name == "bar"
		ops[1].slot.typeClass == "java/lang/String"

		and:
		Operand.Move.isCase(ops[2])
		ops[2].src.typeClass == "java/lang/String"
		ops[2].dst.typeClass == "I"

		and:
		Operand.Store.isCase(ops[3])
		ops[3].name == "bar"
		ops[3].slot.typeClass == "I"

		and:
		Operand.Get.isCase(ops[4])
		ops[4].rel == "getHoge"
		ops[4].slot.typeClass == "java/lang/Long"

		and:
		Operand.Move.isCase(ops[5])
		ops[5].src.typeClass == "java/lang/Long"
		ops[5].dst.typeClass == "java/lang/String"

		and:
		Operand.Store.isCase(ops[6])
		ops[6].name == "hoge"
		ops[6].slot.typeClass == "java/lang/String"
	}

	def "あいまいでコンストラクタを決定できない"() {
		when:
		Operand.builder(Operand_Lhs.class, Operand_Rhs_AmbiguousCtor.class)
				.operands(true)

		then:
		thrown(AbortCallException)
	}
}
