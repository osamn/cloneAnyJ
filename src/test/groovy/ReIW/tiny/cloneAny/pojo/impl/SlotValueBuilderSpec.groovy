package ReIW.tiny.cloneAny.pojo.impl

import spock.lang.Specification
import spock.lang.Unroll

class SlotValueBuilderSpec extends Specification {

	// @Unroll
	def "子要素のない単純なクラス : #descriptor"() {
		expect:
		def actual = new SlotValueBuilder(wildcard).build(descriptor)
		actual.typeParam       == typeParam
		actual.arrayType       == isArray
		actual.boxingType      == isBoxing
		actual.primitiveType   == isPrimitive
		actual.slotList.size() == childCnt

		where:
		wildcard | descriptor            || isArray | isBoxing | isPrimitive  | childCnt | typeParam
		null     | 'Ljava/lang/String;'  || false   | false    | false        | 0        | null
		'='      | 'Ljava/lang/Integer;' || false   | true     | false        | 0        | '='
		'+'      | 'C'                   || false   | false    | true         | 0        | '+'
		'FOO'    | 'I'                   || false   | false    | true         | 0        | 'FOO'
	}

	def "型パラメタ付き generic クラス"() {
	}

	def "配列"() {
	}

	def "多次元の配列"() {
	}

	def "generic クラスの配列"() {
	}

	def "generic クラスの配列の配列"() {
	}
}