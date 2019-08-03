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

		and:
		Operand.Push.isCase(ops[1])
		ops[1].src.typeClass == "I"
		ops[1].dst.typeClass == "Ljava/lang/Long;"

		and:
		Operand.Load.isCase(ops[2])
		ops[2].name == "bar"

		and:
		Operand.Push.isCase(ops[3])
		ops[3].src.typeClass == "Ljava/lang/String;"
		ops[3].dst.typeClass == "Ljava/lang/Number;"

		and:
		Operand.PropGet.isCase(ops[4])
		ops[4].rel == "isBuz"

		and:
		Operand.Push.isCase(ops[5])
		ops[5].src.typeClass == "Z"
		ops[5].dst.typeClass == "Ljava/lang/Boolean;"

		and:
		Operand.Ctor.isCase(ops[6])
		ops[6].descriptor == "(Ljava/lang/Long;Ljava/lang/Number;Ljava/lang/Boolean;)V"
	}

	def "setter で注入"() {
		setup:
		def ops = Operand.builder(Operand_Lhs.class, Operand_Rhs_Setter.class)
				.operands(false).toArray()

		expect:
		Operand.PropGet.isCase(ops[0])
		ops[0].rel == "isBuz"

		and:
		Operand.Move.isCase(ops[1])
		ops[1].src.typeClass == "Z"
		ops[1].dst.typeClass == "Ljava/lang/Boolean;"

		and:
		Operand.PropSet.isCase(ops[2])
		ops[2].rel == "setBuz"

		and:
		Operand.PropGet.isCase(ops[3])
		ops[3].rel == "getHoge"

		and:
		Operand.Move.isCase(ops[4])
		ops[4].src.typeClass == "Ljava/lang/Long;"
		ops[4].dst.typeClass == "J"

		and:
		Operand.PropSet.isCase(ops[5])
		ops[5].rel == "setHoge"
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

		and:
		Operand.Move.isCase(ops[2])
		ops[2].src.typeClass == "Ljava/lang/String;"
		ops[2].dst.typeClass == "I"

		and:
		Operand.Store.isCase(ops[3])
		ops[3].name == "bar"

		and:
		Operand.PropGet.isCase(ops[4])
		ops[4].rel == "getHoge"

		and:
		Operand.Move.isCase(ops[5])
		ops[5].src.typeClass == "Ljava/lang/Long;"
		ops[5].dst.typeClass == "Ljava/lang/String;"

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


	def "Map から POJO へ"() {
		when:
		def ops = Operand.builder(Operand_Map.class, Operand_Rhs_Field.class)
				.operands(false).toArray()

		then:
		Operand.MapGet.isCase(ops[0])
		ops[0].name == "bar"
		Operand.Move.isCase(ops[1])
		Operand.Store.isCase(ops[2])

		and:
		Operand.MapGet.isCase(ops[3])
		ops[3].name == "hoge"
		Operand.Move.isCase(ops[4])
		Operand.Store.isCase(ops[5])
	}

	def "POJO から Map へ" () {
		when:
		def ops = Operand.builder(Operand_Lhs.class, Operand_Map.class)
				.operands(false)
				.toArray()

		then:
		Operand.Load.isCase(ops[0])
		Operand.Move.isCase(ops[1])
		Operand.MapPut.isCase(ops[2])
		ops[2].name == "bar"

		and:
		Operand.PropGet.isCase(ops[3])
		Operand.Move.isCase(ops[4])
		Operand.MapPut.isCase(ops[5])
		ops[5].name == "hoge"

		and:
		Operand.PropGet.isCase(ops[6])
		Operand.Move.isCase(ops[7])
		Operand.MapPut.isCase(ops[8])
		ops[8].name == "buz"

		and:
		Operand.Load.isCase(ops[9])
		Operand.Move.isCase(ops[10])
		Operand.MapPut.isCase(ops[11])
		ops[11].name == "foo"

	}

	def "Map から Map へ" () {
		when:
		def ops = Operand.builder(Operand_Map.class, Operand_Map.class)
				.operands(false).toArray()

		then:
		Operand.MapGet.isCase(ops[0])
		ops[0].name == "*"

		and:
		Operand.Move.isCase(ops[1])

		and:
		Operand.MapPut.isCase(ops[2])
		ops[2].name == "*"
	}
}
