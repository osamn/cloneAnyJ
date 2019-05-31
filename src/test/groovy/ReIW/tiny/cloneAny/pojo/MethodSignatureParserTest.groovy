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
		def slots = []

		when:
		MethodSignatureParser.parseArgumentsAndReturn("", 
			"(JTV1;Ljava/util/List<Ljava/util/Map<TT1;Ljava/lang/String;>;>;B)V", {s -> slots << s; println s}, {})

		then:
		slots.size() == 4
		slots[0].typeParam == null
		slots[0].typeClass == "J"

		slots[1].typeParam == "V1"
		slots[1].typeClass == null

		slots[2].typeParam == null
		slots[2].typeClass == "java/util/List"
		slots[2].slotList.size() == 1
		slots[2].slotList[0].typeParam == "="
		slots[2].slotList[0].typeClass == "java/util/Map"
		slots[2].slotList[0].slotList.size() == 2
		slots[2].slotList[0].slotList[0].typeParam == "T1"
		slots[2].slotList[0].slotList[0].typeClass == null
		slots[2].slotList[0].slotList[1].typeParam == "="
		slots[2].slotList[0].slotList[1].typeClass == "java/lang/String"

		slots[3].typeParam == null
		slots[3].typeClass == "B"
	}

}
