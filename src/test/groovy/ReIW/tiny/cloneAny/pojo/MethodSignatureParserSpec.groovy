package ReIW.tiny.cloneAny.pojo

import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

class MethodSignatureParserSpec extends Specification {

	//@Unroll
	def "parseArgumentsAndReturn 戻り値スロット (#descriptor, #signature)"() {
		setup:
		Slot slot
		MethodSignatureParser.parseArgumentsAndReturn(descriptor, signature, {}, { s ->
			slot = s
		})

		expect: "slot の値を検証"
		slot.typeParam == param
		slot.typeClass == clazz

		and: "slot の子を検証"
		def X = slot.slotList[0]
		X?.typeParam == nestedParam
		X?.typeClass == nestedClass

		and: "slot の孫を検証"
		def Y = X?.slotList?.getAt(0)
		def Z = X?.slotList?.getAt(1)
		Y?.typeParam == nestedNestedParam1
		Y?.typeClass == nestedNestedClass1
		Z?.typeParam == nestedNestedParam2
		Z?.typeClass == nestedNestedClass2

		where:
		descriptor				| signature					|| param	| clazz
		"()Ljava/lang/Integer;"	| null						|| null		| "Ljava/lang/Integer;"
		null					| "(BTV1;)J" 				|| null 	| "J"
		null					| "(Ljava/util/List<TT1;>;)TV1;"	\
															|| "V1"		| "Ljava/lang/Object;"
		null					| "(TV2;)Ljava/util/List<Ljava/util/Map<TT1;Ljava/lang/String;>;>;" \
															|| null		| "Ljava/util/List;" // List<Map<T1,String>>

		and:
		nestedParam << [null, null, null, "="]
		nestedClass << [null, null, null, "Ljava/util/Map;"]

		and:
		nestedNestedParam1 << [null, null, null, "T1"]
		nestedNestedClass1 << [null, null, null, "Ljava/lang/Object;"]

		and:
		nestedNestedParam2 << [null, null, null, "="]
		nestedNestedClass2 << [null, null, null, "Ljava/lang/String;"]
	}

	//@Unroll
	def "parseArgumentsAndReturn 単一引数スロット (#descriptor, #signature)"() {
		setup:
		Slot slot
		MethodSignatureParser.parseArgumentsAndReturn(descriptor, signature, { s ->
			slot = s
		}, {})

		expect: "slot の値を検証"
		slot.typeParam == param
		slot.typeClass == clazz

		and: "slot の子を検証"
		def X = slot.slotList[0]
		X?.typeParam == nestedParam
		X?.typeClass == nestedClass

		and: "slot の孫を検証"
		def Y = X?.slotList?.getAt(0)
		def Z = X?.slotList?.getAt(1)
		Y?.typeParam == nestedNestedParam1
		Y?.typeClass == nestedNestedClass1
		Z?.typeParam == nestedNestedParam2
		Z?.typeClass == nestedNestedClass2

		where:
		descriptor				| signature					|| param	| clazz
		"(Ljava/lang/Integer;)V"| null						|| null		| "Ljava/lang/Integer;"
		null					| "(TV1;)V"					|| "V1"		| "Ljava/lang/Object;"
		null					| "(Ljava/util/List<Ljava/util/Map<TT1;Ljava/lang/String;>;>;)V" \
															|| null		| "Ljava/util/List;" // List<Map<T1,String>>

		and:
		nestedParam << [null, null, "="]
		nestedClass << [null, null, "Ljava/util/Map;"]

		and:
		nestedNestedParam1 << [null, null, "T1"]
		nestedNestedClass1 << [null, null, "Ljava/lang/Object;"]

		and:
		nestedNestedParam2 << [null, null, "="]
		nestedNestedClass2 << [null, null, "Ljava/lang/String;"]
	}

	def "parseArgumentsAndReturn 複数引数 long,T1,List<Map<T1,String>>,boolean"() {
		setup:
		def args = []

		when:
		MethodSignatureParser.parseArgumentsAndReturn(null,
				"(JTV1;Ljava/util/List<Ljava/util/Map<TT1;Ljava/lang/String;>;>;B)V", { s ->
					args << s
				}, {})

		then:
		args.size() == 4
		args[0].typeParam == null
		args[0].typeClass == "J"

		args[1].typeParam == "V1"
		args[1].typeClass == "Ljava/lang/Object;"

		args[2].typeParam == null
		args[2].typeClass == "Ljava/util/List;"
		args[2].slotList.size() == 1
		args[2].slotList[0].typeParam == "="
		args[2].slotList[0].typeClass == "Ljava/util/Map;"
		args[2].slotList[0].slotList.size() == 2
		args[2].slotList[0].slotList[0].typeParam == "T1"
		args[2].slotList[0].slotList[0].typeClass == "Ljava/lang/Object;"
		args[2].slotList[0].slotList[1].typeParam == "="
		args[2].slotList[0].slotList[1].typeClass == "Ljava/lang/String;"

		args[3].typeParam == null
		args[3].typeClass == "B"
	}

	def "parseArgumentsAndReturn descriptor のみ"() {
		setup:
		def args = []
		def retSlot

		when:
		MethodSignatureParser.parseArgumentsAndReturn("(JLjava/lang/Double;I)Ljava/lang/Boolean;",
				null, { s ->
					args << s
				}, { s ->
					retSlot = s
				})

		then:
		args.size() == 3
		args[0].typeParam == null
		args[0].typeClass == "J"
		args[1].typeParam == null
		args[1].typeClass == "Ljava/lang/Double;"
		args[2].typeParam == null
		args[2].typeClass == "I"
		retSlot.typeParam == null
		retSlot.typeClass == "Ljava/lang/Boolean;"
	}

	def "parseArgumentsAndReturn メソッド定義に方引数がある場合は例外 <X> X hoge() みたいなやつ"() {
		when:
		MethodSignatureParser.parseArgumentsAndReturn(null,
				"<X:Ljava/lang/Object;>()Ljava/util/Map<TV1;TX;>;", {}, {})

		then:
		thrown(UnboundFormalTypeParameterException)
	}

	def "parameterParserVisitor"() {
		setup:
		def param;
		def visitor = MethodSignatureParser.parameterParserVisitor("(JLjava/lang/String;B)V", null, { name, slot ->
			param = [key:name, val:slot]
		})

		when:
		visitor.visitLocalVariable("a", null, null, null, null, 0)

		then:
		param["key"]=="a"
		param["val"].typeParam==null
		param["val"].typeClass=="J"

		when:
		visitor.visitLocalVariable("b", null, null, null, null, 0)

		then:
		param["key"]=="b"
		param["val"].typeParam==null
		param["val"].typeClass=="Ljava/lang/String;"

		when:
		visitor.visitLocalVariable("c", null, null, null, null, 0)

		then:
		param["key"]=="c"
		param["val"].typeParam==null
		param["val"].typeClass=="B"
	}
}
