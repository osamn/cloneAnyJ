package ReIW.tiny.cloneAny.pojo.impl

import spock.lang.Specification

class FieldSignatureParserSpec extends Specification {

	def "non generic なフィールド"() {
		setup:
		def actual

		when:
		new FieldSignatureParser({actual = it}).parse('I', null)

		then:
		actual.typeParam == null
		actual.descriptor == 'I'
		actual.slotList == []
	}

	def "配列フィールド"() {
		setup:
		def actual

		when:
		new FieldSignatureParser({actual = it}).parse('[Ljava/lang/Long;', null)

		then:
		actual.typeParam == null
		actual.@descriptor == '['
		actual.slotList.size() == 1
		actual.slotList[0].descriptor == 'Ljava/lang/Long;'
	}

	def "generic フィールド"() {
		setup:
		def actual

		when:
		new FieldSignatureParser({actual = it}).parse(null, 'Ljava/util/List<Ljava/lang/Long;>;')

		then:
		actual.typeParam == null
		actual.descriptor == 'Ljava/util/List;'
		actual.slotList.size() == 1
		actual.slotList[0].typeParam == '='
		actual.slotList[0].descriptor == 'Ljava/lang/Long;'
		actual.slotList[0].slotList.size() == 0
	}
	
}
