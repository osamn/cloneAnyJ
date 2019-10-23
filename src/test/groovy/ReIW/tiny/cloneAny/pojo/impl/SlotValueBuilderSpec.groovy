package ReIW.tiny.cloneAny.pojo.impl

import spock.lang.Specification
import spock.lang.Unroll

class SlotValueBuilderSpec extends Specification {

	// @Unroll
	def "単純なクラスと配列のルート : #descriptor"() {
		expect:
		def actual = new SlotValueBuilder(null).build(descriptor)
		actual.arrayType       == isArray
		actual.boxingType      == isBoxing
		actual.primitiveType   == isPrimitive
		actual.slotList.size() == childCnt
		actual.@descriptor      == actualDesc

		where:
		descriptor            || isArray | isBoxing | isPrimitive  | childCnt | actualDesc
		'Ljava/lang/String;'  || false   | false    | false        | 0        | 'Ljava/lang/String;'
		'C'                   || false   | false    | true         | 0        | 'C'
		'I'                   || false   | false    | true         | 0        | 'I'
		'[I'                  || true    | false    | false        | 1        | '['
		'[Ljava/lang/Long;'   || true    | false    | false        | 1        | '['
	}

	def "generic クラス"() {
		// List<Map<String, X>> field;
		setup:
		def signature = 'Ljava/util/List<Ljava/util/Map<Ljava/lang/String;TX;>;>;'

		when:
		def actual = new SlotValueBuilder(null).build(signature);

		then:
		actual.typeParam == null
		actual.descriptor == 'Ljava/util/List;'
		actual.slotList.size() ==  1

		then:
		actual.slotList[0].typeParam == '='
		actual.slotList[0].descriptor == 'Ljava/util/Map;'
		actual.slotList[0].slotList.size() ==  2

		then:
		actual.slotList[0].slotList[0].typeParam == '='
		actual.slotList[0].slotList[0].descriptor == 'Ljava/lang/String;'
		actual.slotList[0].slotList[0].slotList == []

		then:
		actual.slotList[0].slotList[1].typeParam == 'X'
		actual.slotList[0].slotList[1].descriptor == 'Ljava/lang/Object;'
		actual.slotList[0].slotList[1].slotList == []
	}

	def "配列"() {
		// long[][]
		setup:
		def signature = '[[J'

		when:
		def actual = new SlotValueBuilder(null).build(signature);

		then:
		actual.typeParam == null
		actual.@descriptor == '['
		actual.arrayType == true
		actual.slotList.size() ==  1

		then:
		actual.slotList[0].typeParam == null
		actual.slotList[0].@descriptor == '['
		actual.slotList[0].arrayType == true
		actual.slotList[0].slotList.size() ==  1

		then:
		actual.slotList[0].slotList[0].typeParam == null
		actual.slotList[0].slotList[0].@descriptor == 'J'
		actual.slotList[0].slotList[0].arrayType == false
		actual.slotList[0].slotList[0].slotList == []
	}

	def "generic クラスの配列"() {
		// Map<String,X>[]
		setup:
		def signature = '[Ljava/util/Map<Ljava/lang/String;TX;>;'

		when:
		def actual = new SlotValueBuilder(null).build(signature);

		then:
		actual.typeParam == null
		actual.@descriptor == '['
		actual.arrayType == true
		actual.slotList.size() ==  1

		then:
		actual.slotList[0].typeParam == null
		actual.slotList[0].@descriptor == 'Ljava/util/Map;'
		actual.slotList[0].slotList.size() == 2

		then:
		actual.slotList[0].slotList[0].typeParam == '='
		actual.slotList[0].slotList[0].@descriptor == 'Ljava/lang/String;'
		actual.slotList[0].slotList[0].slotList == []

		then:
		actual.slotList[0].slotList[1].typeParam == 'X'
		actual.slotList[0].slotList[1].@descriptor == 'Ljava/lang/Object;'
		actual.slotList[0].slotList[1].slotList == []
	}

	def "型パラメタに配列を持つ generic なクラス"() {
		// List<long[]>
		setup:
		def signature = 'Ljava/util/List<[J>;'

		when:
		def actual = new SlotValueBuilder(null).build(signature);

		then:
		actual.typeParam == null
		actual.descriptor == 'Ljava/util/List;'
		actual.slotList.size() ==  1

		then:
		actual.slotList[0].typeParam == '='
		actual.slotList[0].@descriptor == '['
		actual.slotList[0].slotList.size() == 1

		then:
		actual.slotList[0].slotList[0].typeParam == null
		actual.slotList[0].slotList[0].@descriptor == 'J'
		actual.slotList[0].slotList[0].slotList == []
	}

}