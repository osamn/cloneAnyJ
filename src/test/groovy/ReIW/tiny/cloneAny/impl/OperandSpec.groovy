package ReIW.tiny.cloneAny.impl

import ReIW.tiny.cloneAny.pojo_.AbortCallException
import spock.lang.Specification

class OperandSpec extends Specification{

	def "コンストラクタで注入"() {
		setup:
		def ops = OperandStreamBuilder.builder(Operand_Lhs.class, Operand_Rhs_Ctor.class)
				.operands(true).toArray()

		expect:
		Operand.Load.isCase(ops[0])
		ops[0].name == "foo"

		and:
		Operand.ConvTop.isCase(ops[1])
		ops[1].src.typeClass == "I"
		ops[1].dst.typeClass == "Ljava/lang/Long;"

		and:
		Operand.Load.isCase(ops[2])
		ops[2].name == "bar"

		and:
		Operand.ConvTop.isCase(ops[3])
		ops[3].src.typeClass == "Ljava/lang/String;"
		ops[3].dst.typeClass == "Ljava/lang/Number;"

		and:
		Operand.Get.isCase(ops[4])
		ops[4].rel == "isBuz"

		and:
		Operand.ConvTop.isCase(ops[5])
		ops[5].src.typeClass == "Z"
		ops[5].dst.typeClass == "Ljava/lang/Boolean;"

		and:
		Operand.Ctor.isCase(ops[6])
		ops[6].descriptor == "(Ljava/lang/Long;Ljava/lang/Number;Ljava/lang/Boolean;)V"
	}

	def "setter で注入"() {
		setup:
		def ops = OperandStreamBuilder.builder(Operand_Lhs.class, Operand_Rhs_Setter.class)
				.operands(false).toArray()

		expect:
		Operand.Get.isCase(ops[0])
		ops[0].rel == "isBuz"

		and:
		Operand.ConvTop.isCase(ops[1])
		ops[1].src.typeClass == "Z"
		ops[1].dst.typeClass == "Ljava/lang/Boolean;"

		and:
		Operand.Set.isCase(ops[2])
		ops[2].rel == "setBuz"

		and:
		Operand.Get.isCase(ops[3])
		ops[3].rel == "getHoge"

		and:
		Operand.ConvTop.isCase(ops[4])
		ops[4].src.typeClass == "Ljava/lang/Long;"
		ops[4].dst.typeClass == "J"

		and:
		Operand.Set.isCase(ops[5])
		ops[5].rel == "setHoge"
	}

	def "フィールドに注入"() {
		setup:
		def ops = OperandStreamBuilder.builder(Operand_Lhs.class, Operand_Rhs_Field.class)
				.operands(true).toArray()

		expect:
		Operand.Ctor.isCase(ops[0])

		and:
		Operand.Load.isCase(ops[1])
		ops[1].name == "bar"

		and:
		Operand.ConvTop.isCase(ops[2])
		ops[2].src.typeClass == "Ljava/lang/String;"
		ops[2].dst.typeClass == "I"

		and:
		Operand.Store.isCase(ops[3])
		ops[3].name == "bar"

		and:
		Operand.Get.isCase(ops[4])
		ops[4].rel == "getHoge"

		and:
		Operand.ConvTop.isCase(ops[5])
		ops[5].src.typeClass == "Ljava/lang/Long;"
		ops[5].dst.typeClass == "Ljava/lang/String;"

		and:
		Operand.Store.isCase(ops[6])
		ops[6].name == "hoge"
	}

	def "あいまいでコンストラクタを決定できない"() {
		when:
		OperandStreamBuilder.builder(Operand_Lhs.class, Operand_Rhs_AmbiguousCtor.class)
				.operands(true)

		then:
		thrown(AbortCallException)
	}


	def "Map から POJO へ"() {
		when:
		def ops = OperandStreamBuilder.builder(Operand_Map.class, Operand_Rhs_Field.class)
				.operands(false).toArray()

		then:
		Operand.GetKey.isCase(ops[0])
		ops[0].name == "bar"
		Operand.ConvTop.isCase(ops[1])
		Operand.Store.isCase(ops[2])

		and:
		Operand.GetKey.isCase(ops[3])
		ops[3].name == "hoge"
		Operand.ConvTop.isCase(ops[4])
		Operand.Store.isCase(ops[5])
	}

	def "POJO から Map へ" () {
		when:
		def ops = OperandStreamBuilder.builder(Operand_Lhs.class, Operand_Map.class)
				.operands(false)
				.toArray()

		then:
		Operand.Load.isCase(ops[0])
		Operand.ConvTop.isCase(ops[1])
		Operand.SetKey.isCase(ops[2])
		ops[2].name == "bar"

		and:
		Operand.Get.isCase(ops[3])
		Operand.ConvTop.isCase(ops[4])
		Operand.SetKey.isCase(ops[5])
		ops[5].name == "hoge"

		and:
		Operand.Get.isCase(ops[6])
		Operand.ConvTop.isCase(ops[7])
		Operand.SetKey.isCase(ops[8])
		ops[8].name == "buz"

		and:
		Operand.Load.isCase(ops[9])
		Operand.ConvTop.isCase(ops[10])
		Operand.SetKey.isCase(ops[11])
		ops[11].name == "foo"

	}

	def "Map から Map へ" () {
		when:
		def ops = OperandStreamBuilder.builder(Operand_Map.class, Operand_Map.class)
				.operands(false).toArray()

		then:
		Operand.GetKey.isCase(ops[0])
		ops[0].name == "*"

		and:
		Operand.ConvTop.isCase(ops[1])

		and:
		Operand.SetKey.isCase(ops[2])
		ops[2].name == "*"
	}
}
