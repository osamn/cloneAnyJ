package ReIW.tiny.cloneAny.pojo.impl

import spock.lang.Specification

class FieldSignatureParserSpec extends Specification {

	def "non generic なフィールド"() {
		setup:
		SlotValue actual

		when:
		new FieldSignatureParser({actual = it}).parse('I', null)

		then:
		actual.wildcard == null
		actual.typeParam == null
		actual.descriptor == 'I'
		actual.slotList == []
	}

	def "配列フィールド"() {
		setup:
		SlotValue actual

		when:
		new FieldSignatureParser({actual = it}).parse('[Ljava/lang/Long;', null)

		then:
		actual.slotList.size() == 1
		actual.slotList[0].slotList.size() == 0
		
		when:
		def array_slot = actual
		def elm_slot = array_slot.slotList[0]
		
		then:
		array_slot.wildcard == null
		array_slot.typeParam == null
		array_slot.@descriptor == '['
		array_slot.descriptor == '[Ljava/lang/Long;'
		array_slot.arrayType == true

		then:
		elm_slot.wildcard == null
		elm_slot.typeParam == null
		elm_slot.descriptor == 'Ljava/lang/Long;'
		elm_slot.arrayType == false
	}

	def "generic フィールド"() {
		setup:
		SlotValue actual

		when:
		new FieldSignatureParser({actual = it}).parse(null, 'Ljava/util/List<Ljava/lang/String;>;')

		then:
		actual.slotList.size() == 1
		actual.slotList[0].slotList.size() == 0
		
		when:
		def list_slot = actual
		def elm_slot = list_slot.slotList[0]
		
		then:
		list_slot.wildcard == null
		list_slot.typeParam == null
		list_slot.descriptor == 'Ljava/util/List;'

		then:
		elm_slot.wildcard == '='
		elm_slot.typeParam == null
		elm_slot.descriptor == 'Ljava/lang/String;'
	}
	
}
