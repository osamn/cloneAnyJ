package ReIW.tiny.cloneAny.pojo.impl

import static org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

import spock.lang.Specification

class ClassSignatureParserSpec extends Specification {

	def "non generic な extends + implements"() {
		setup:
		def formals = []
		def supers = []

		when:
		new ClassSignatureParser({formals << it},  {supers << it}).parse('foo/bar/Hoge', ['foo/bar/IPiyo', 'foo/bar/IFuga'] as String[], null)

		then:
		formals == []
		supers.collect { it.@descriptor } == ['Lfoo/bar/Hoge;', 'Lfoo/bar/IPiyo;', 'Lfoo/bar/IFuga;']
	}

	def "generic extends + implements"() {
		setup:
		def formals = []
		def supers = []
		//	class X1<A> extends Y<A> implements X<String, A> {
		def signature = '<A:Ljava/lang/Object;>Lfoo/Y<TA;>;Lfoo/X<Ljava/lang/String;TA;>;'

		when:
		new ClassSignatureParser({formals << it},  {supers << it}).parse(null, null, signature)
		
		then:
		formals.size() == 1
		supers.size() == 2

		then:
		formals[0].typeParam == 'A'
		formals[0].descriptor == 'Ljava/lang/Object;'
		formals[0].slotList == []
		
		then:
		supers[0].typeParam == null
		supers[0].descriptor == 'Lfoo/Y;'
		supers[0].slotList.size() == 1
		supers[0].slotList[0].typeParam == 'A'
		supers[0].slotList[0].descriptor == 'Ljava/lang/Object;'

		then:
		supers[1].typeParam == null
		supers[1].descriptor == 'Lfoo/X;'
		supers[1].slotList.size() == 2
		supers[1].slotList[0].typeParam == '='
		supers[1].slotList[0].descriptor == 'Ljava/lang/String;'
		supers[1].slotList[1].typeParam == 'A'
		supers[1].slotList[1].descriptor == 'Ljava/lang/Object;'
	}
}
