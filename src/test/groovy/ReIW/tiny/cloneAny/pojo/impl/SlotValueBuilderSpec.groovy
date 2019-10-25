package ReIW.tiny.cloneAny.pojo.impl

import spock.lang.Specification
import spock.lang.Unroll

class SlotValueBuilderSpec extends Specification {

	// @Unroll
	def "単純なクラスと配列のルート : #descriptor"() {
		expect:
		def actual = new SlotValueBuilder().build(descriptor)
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
		def actual = new SlotValueBuilder().build(signature);
		
		then:
		actual.slotList.size() ==  1
		actual.slotList[0].slotList.size() ==  2
		actual.slotList[0].slotList[0].slotList == []
		actual.slotList[0].slotList[1].slotList == []
		
		when:
		def list_slot = actual
		def map_slot = list_slot.slotList[0]
		def key_slot = map_slot.slotList[0]
		def val_slot = map_slot.slotList[1]

		then:
		list_slot.wildcard == null
		list_slot.typeParam == null
		list_slot.descriptor == 'Ljava/util/List;'

		then:
		map_slot.wildcard == '='
		map_slot.typeParam == null
		map_slot.descriptor == 'Ljava/util/Map;'

		then:
		key_slot.wildcard == '='
		key_slot.typeParam == null
		key_slot.descriptor == 'Ljava/lang/String;'

		then:
		val_slot.wildcard == '='
		val_slot.typeParam == 'X'
		val_slot.descriptor == 'Ljava/lang/Object;'
	}

	def "配列"() {
		// long[][]
		setup:
		def signature = '[[J'

		when:
		def actual = new SlotValueBuilder().build(signature);
		
		then:
		actual.slotList.size() ==  1
		actual.slotList[0].slotList.size() ==  1
		actual.slotList[0].slotList[0].slotList == []
		
		when:
		def array_slot = actual
		def array_array_slot = array_slot.slotList[0]
		def long_slot = array_array_slot.slotList[0]

		then:
		array_slot.wildcard == null
		array_slot.typeParam == null
		array_slot.@descriptor == '['
		array_slot.descriptor == '[[J'
		array_slot.arrayType == true

		then:
		array_array_slot.wildcard == null
		array_array_slot.typeParam == null
		array_array_slot.@descriptor == '['
		array_array_slot.descriptor == '[J'
		array_array_slot.arrayType == true

		then:
		long_slot.wildcard == null
		long_slot.typeParam == null
		long_slot.@descriptor == 'J'
		long_slot.descriptor == 'J'
		long_slot.arrayType == false
	}

	def "generic クラスの配列"() {
		// Map<String,X>[]
		setup:
		def signature = '[Ljava/util/Map<Ljava/lang/String;TX;>;'

		when:
		def actual = new SlotValueBuilder().build(signature);
		
		then:
		actual.slotList.size() ==  1
		actual.slotList[0].slotList.size() == 2
		actual.slotList[0].slotList[0].slotList == []
		actual.slotList[0].slotList[1].slotList == []
		
		when:
		def array_slot = actual
		def map_slot = array_slot.slotList[0]
		def key_slot = map_slot.slotList[0]
		def val_slot = map_slot.slotList[1]

		then:
		array_slot.typeParam == null
		array_slot.arrayType == true
		array_slot.descriptor == '[Ljava/util/Map;'
		array_slot.@descriptor == '['

		then:
		map_slot.wildcard == null
		map_slot.typeParam == null
		map_slot.descriptor == 'Ljava/util/Map;'

		then:
		key_slot.wildcard == '='
		key_slot.typeParam == null
		key_slot.descriptor == 'Ljava/lang/String;'

		then:
		val_slot.wildcard == '='
		val_slot.typeParam == 'X'
		val_slot.descriptor == 'Ljava/lang/Object;'
	}

	def "型パラメタに配列を持つ generic なクラス"() {
		// List<long[]>
		setup:
		def signature = 'Ljava/util/List<[J>;'

		when:
		def actual = new SlotValueBuilder().build(signature);

		then:
		actual.slotList.size() ==  1
		actual.slotList[0].slotList.size() == 1
		actual.slotList[0].slotList[0].slotList == []
		
		when:
		def list_slot = actual
		def array_slot = list_slot.slotList[0]
		def long_slot = array_slot.slotList[0]

		then:
		list_slot.wildcard == null
		list_slot.typeParam == null
		list_slot.descriptor == 'Ljava/util/List;'

		then:
		array_slot.wildcard == '='
		array_slot.typeParam == null
		array_slot.descriptor == '[J'
		array_slot.@descriptor == '['

		then:
		long_slot.wildcard == null
		long_slot.typeParam == null
		long_slot.descriptor == 'J'
	}

}