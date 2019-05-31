package ReIW.tiny.cloneAny.pojo

import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

class MethodSignatureParserTest extends Specification {

	@Unroll
	def "parseArgumentsAndReturn 戻り値スロット (#descriptor, #signature)"() {
		setup:
		Slot slot
		MethodSignatureParser.parseArgumentsAndReturn(descriptor, signature, {}, {s -> slot = s})

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
		"()Ljava/lang/Integer;"	| null						|| null		| "java/lang/Integer"
		null					| "(BTV1;)J" 				|| null 	| "J"
		null					| "()TV1;"					|| "V1"		| null
		null					| "()Ljava/util/List<Ljava/util/Map<TT1;Ljava/lang/String;>;>;" \
															|| null		| "java/util/List" // List<Map<T1,String>>

		and:
		nestedParam << [null, null, null, "="]
		nestedClass << [null, null, null, "java/util/Map"]

		and:
		nestedNestedParam1 << [null, null, null, "T1"]
		nestedNestedClass1 << [null, null, null, null]

		and:
		nestedNestedParam2 << [null, null, null, "="]
		nestedNestedClass2 << [null, null, null, "java/lang/String"]
	}

	@Unroll
	def "parseArgumentsAndReturn 単一引数スロット (#descriptor, #signature)"() {
		setup:
		Slot slot
		MethodSignatureParser.parseArgumentsAndReturn(descriptor, signature, {s -> slot = s}, {})

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
		"(Ljava/lang/Integer;)V"| null						|| null		| "java/lang/Integer"
		null					| "(TV1;)V"					|| "V1"		| null
		null					| "(Ljava/util/List<Ljava/util/Map<TT1;Ljava/lang/String;>;>;)V" \
															|| null		| "java/util/List" // List<Map<T1,String>>

		and:
		nestedParam << [null, null, "="]
		nestedClass << [null, null, "java/util/Map"]

		and:
		nestedNestedParam1 << [null, null, "T1"]
		nestedNestedClass1 << [null, null, null]

		and:
		nestedNestedParam2 << [null, null, "="]
		nestedNestedClass2 << [null, null, "java/lang/String"]
	}

	def "parseArgumentsAndReturn 複数引数 long,T1,List<Map<T1,String>>,boolean"() {
		setup:
		def args = []

		when:
		MethodSignatureParser.parseArgumentsAndReturn(null, 
			"(JTV1;Ljava/util/List<Ljava/util/Map<TT1;Ljava/lang/String;>;>;B)V", {s -> args << s; println s}, {})

		then:
		args.size() == 4
		args[0].typeParam == null
		args[0].typeClass == "J"

		args[1].typeParam == "V1"
		args[1].typeClass == null

		args[2].typeParam == null
		args[2].typeClass == "java/util/List"
		args[2].slotList.size() == 1
		args[2].slotList[0].typeParam == "="
		args[2].slotList[0].typeClass == "java/util/Map"
		args[2].slotList[0].slotList.size() == 2
		args[2].slotList[0].slotList[0].typeParam == "T1"
		args[2].slotList[0].slotList[0].typeClass == null
		args[2].slotList[0].slotList[1].typeParam == "="
		args[2].slotList[0].slotList[1].typeClass == "java/lang/String"

		args[3].typeParam == null
		args[3].typeClass == "B"
	}

	def "parseArgumentsAndReturn descriptor のみ"() {
		setup:
		def args = []
		def retSlot

		when:
		MethodSignatureParser.parseArgumentsAndReturn("(JLjava/lang/Double;I)Ljava/lang/Boolean;", 
			null, {s -> args << s}, { s -> retSlot = s})
		
		then:
		args.size() == 3
		args[0].typeParam == null
		args[0].typeClass == "J"
		args[1].typeParam == null
		args[1].typeClass == "java/lang/Double"
		args[2].typeParam == null
		args[2].typeClass == "I"
		retSlot.typeParam == null
		retSlot.typeClass == "java/lang/Boolean"
		
	}

	def "parseArgumentsAndReturn 型引数ありメソッド"() {
		when:
		MethodSignatureParser.parseArgumentsAndReturn(null, 
			"<X:Ljava/lang/Object;>()Ljava/util/Map<TV1;TX;>;", {}, {})
		
		then:
		thrown(UnboundFormalTypeParameterException)
	}
}
