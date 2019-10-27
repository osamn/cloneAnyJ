package ReIW.tiny.cloneAny.pojo.impl

import spock.lang.Specification

class ClassSignatureParserSpec extends Specification {

	def "non generic な extends + implements"() {
		setup:
		def formals = []
		def supers = []

		when:
		new ClassSignatureParser({formals << it},  {supers << it}).parse(null,'foo/bar/Hoge', ['foo/bar/IPiyo', 'foo/bar/IFuga'] as String[])

		then:
		formals == []
		supers.collect { (it as SlotValue).descriptor } == ['Lfoo/bar/Hoge;', 'Lfoo/bar/IPiyo;', 'Lfoo/bar/IFuga;']
	}

	def "generic extends + implements"() {
		setup:
		def formals = []
		def supers = []
		//	class X1<A> extends Y<A> implements X<String, A> {
		def signature = '<A:Ljava/lang/Object;>Lfoo/Y<TA;>;Lfoo/X<Ljava/lang/String;TA;>;'

		when:
		new ClassSignatureParser({formals << it},  {supers << it}).parse(signature, null, null)

		then:
		formals.size() == 1
		supers.size() == 2

		when:
		SlotValue formal_slot = formals[0]
		SlotValue super_slot_1 = supers[0]
		SlotValue super_slot_2 = supers[1]

		then:
		formal_slot.wildcard == '#' // formal なやつなので
		formal_slot.typeParam == 'A'
		formal_slot.descriptor == 'Ljava/lang/Object;'
		formal_slot.slotList == []

		then:
		super_slot_1.wildcard == null
		super_slot_1.typeParam == null
		super_slot_1.descriptor == 'Lfoo/Y;'
		super_slot_1.slotList.size() == 1

		super_slot_1.slotList[0].wildcard == '='
		super_slot_1.slotList[0].typeParam == 'A'
		super_slot_1.slotList[0].descriptor == 'Ljava/lang/Object;'

		then:
		super_slot_2.typeParam == null
		super_slot_2.descriptor == 'Lfoo/X;'
		super_slot_2.slotList.size() == 2

		super_slot_2.slotList[0].wildcard == '='
		super_slot_2.slotList[0].typeParam == null
		super_slot_2.slotList[0].descriptor == 'Ljava/lang/String;'

		super_slot_2.slotList[1].wildcard == '='
		super_slot_2.slotList[1].typeParam == 'A'
		super_slot_2.slotList[1].descriptor == 'Ljava/lang/Object;'
	}
}
