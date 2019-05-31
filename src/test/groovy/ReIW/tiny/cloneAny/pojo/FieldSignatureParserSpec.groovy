package ReIW.tiny.cloneAny.pojo

import spock.lang.Shared
import spock.lang.Specification

class FieldSignatureParserSpec extends Specification {

	def "#descriptor, #signature "() {
		setup:
		def slot
		FieldSignatureParser.parse(descriptor, signature, {s -> slot = s })
		// println slot

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
		"Ljava/lang/Integer;"	| null						|| null		| "java/lang/Integer"
		"I"						| null 						|| null 	| "I"
		""						| "TV1;"					|| "V1"		| null
		""						| "Ljava/util/List<TV1;>;"	|| null 	| "java/util/List" // List<V1>
		""						| "Ljava/util/List<Ljava/util/Map<TT1;Ljava/lang/String;>;>;" \
															|| null		| "java/util/List" // List<Map<T1,String>>

		// 横に並べるとみにくいのでパイプにしてみる
		and:
		nestedParam << [null, null, null, "V1", "="]
		nestedClass << [null, null, null, null, "java/util/Map"]

		and:
		nestedNestedParam1 << [null, null, null, null, "T1"]
		nestedNestedClass1 << [null, null, null, null, null]

		and:
		nestedNestedParam2 << [null, null, null, null, "="]
		nestedNestedClass2 << [null, null, null, null, "java/lang/String"]
	}
}
