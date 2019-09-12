package ReIW.tiny.cloneAny.pojo.impl

import org.objectweb.asm.Type

import spock.lang.Specification

class ClassSignatureParserSpec extends Specification {

	def formals
	def supers

	def setup() {
		formals = []
		supers = []
	}

	def "simple class extends"() {
		setup:
		def clazz = ClassSignatureParserTester.Simple.class;

		when:
		new ClassSignatureParser({formals << it}, {supers << it})
		.parse('ReIW/tiny/cloneAny/pojo/impl/ClassSignatureParserTester$Simple', null, null)

		then:
		formals.size() == 0
		supers.size() == 1

		then:
		supers[0].typeParam == null
		supers[0].descriptor == Type.getDescriptor(clazz)
		supers[0].slotList == []
	}

	def "extends generic class extends with bind"() {
		setup:
		def clazz = ClassSignatureParserTester.Generic.class

		when:
		new ClassSignatureParser({formals << it}, {supers << it})
		.parse(null, null, 'LReIW/tiny/cloneAny/pojo/impl/ClassSignatureParserTester$Generic<Ljava/lang/String;>;')

		then:
		formals.size() == 0
		supers.size() == 1

		then:
		supers[0].typeParam == null
		supers[0].descriptor == Type.getDescriptor(clazz)
		supers[0].slotList.size() == 1

		then:
		supers[0].slotList[0].typeParam == "="
		supers[0].slotList[0].descriptor == "Ljava/lang/String;"
		supers[0].slotList[0].slotList == []
	}

	def "implements non generic interfaces"() {
		setup:
		def clazz = ClassSignatureParserTester.Simple.class;
		def intf1 = ClassSignatureParserTester.Foo.class
		def intf2 = ClassSignatureParserTester.Bar.class

		when:
		new ClassSignatureParser({formals << it}, {supers << it}) .parse(
		'ReIW/tiny/cloneAny/pojo/impl/ClassSignatureParserTester$Simple', [
			'ReIW/tiny/cloneAny/pojo/impl/ClassSignatureParserTester$Foo',
			'ReIW/tiny/cloneAny/pojo/impl/ClassSignatureParserTester$Bar'] as String[], null)

		then:
		formals.size() == 0
		supers.size() == 3

		then:
		supers[0].typeParam == null
		supers[0].@descriptor == Type.getDescriptor(clazz)
		supers[0].slotList == []

		then:
		supers[1].typeParam == null
		supers[1].@descriptor == Type.getDescriptor(intf1)
		supers[1].slotList == []

		then:
		supers[2].typeParam == null
		supers[2].@descriptor == Type.getDescriptor(intf2)
		supers[2].slotList == []
	}

	def "implements generic interface"() {
		setup:
		def intf = ClassSignatureParserTester.GenericIntf.class

		when:
		new ClassSignatureParser({formals << it}, {supers << it}) .parse(null, null,
		'Ljava/lang/Object;LReIW/tiny/cloneAny/pojo/impl/ClassSignatureParserTester$GenericIntf<[Ljava/lang/String;>;')

		then:
		formals.size() == 0
		supers.size() == 2

		then:
		supers[0].typeParam == null
		supers[0].@descriptor == "Ljava/lang/Object;"
		supers[0].slotList == []

		then:
		supers[1].typeParam == null
		supers[1].@descriptor == Type.getDescriptor(intf)
		supers[1].slotList.size() == 1

		then:
		supers[1].slotList[0].typeParam == "="
		supers[1].slotList[0].@descriptor == "["
		supers[1].slotList[0].slotList.size() == 1

		then:
		supers[1].slotList[0].slotList[0].typeParam == null
		supers[1].slotList[0].slotList[0].@descriptor == "Ljava/lang/String;"
		supers[1].slotList[0].slotList[0].slotList == []
	}

	def "generic class"() {
		when:
		new ClassSignatureParser({formals << it}, {supers << it}).parse(null, null,
		'<K:Ljava/lang/Object;V:Ljava/lang/Object;>Ljava/lang/Object;')

		then:
		formals.size() == 2
		supers.size() == 1

		then:
		formals[0].typeParam == "K"
		formals[0].descriptor == "Ljava/lang/Object;"
		formals[0].slotList == []

		then:
		formals[1].typeParam == "V"
		formals[1].descriptor == "Ljava/lang/Object;"
		formals[1].slotList == []

		then:
		supers[0].typeParam == null
		supers[0].descriptor == "Ljava/lang/Object;"
		supers[0].slotList == []
	}

	def "partial bound generic classs"() {
		setup:
		def clazz = ReIW.tiny.cloneAny.pojo.impl.ClassSignatureParserTester$Unbound.class

		when:
		new ClassSignatureParser({formals << it}, {supers << it}).parse(null, null,
		'<A:Ljava/lang/Object;>LReIW/tiny/cloneAny/pojo/impl/ClassSignatureParserTester$Unbound<TA;[I>;')

		then:
		formals.size() == 1
		supers.size() == 1

		then:
		formals[0].typeParam == "A"
		formals[0].@descriptor == "Ljava/lang/Object;"
		formals[0].slotList == []

		then:
		supers[0].typeParam == null
		supers[0].@descriptor == Type.getDescriptor(clazz)
		supers[0].slotList.size() == 2

		then:
		supers[0].slotList[0].typeParam == "A"
		supers[0].slotList[0].@descriptor == "Ljava/lang/Object;"
		supers[0].slotList[0].slotList == []

		then:
		supers[0].slotList[1].typeParam == "="
		supers[0].slotList[1].@descriptor == "["
		supers[0].slotList[1].slotList.size() == 1

		then:
		supers[0].slotList[1].slotList[0].typeParam == null
		supers[0].slotList[1].slotList[0].@descriptor == "I"
		supers[0].slotList[1].slotList[0].slotList == []
	}

	def "extends nested generic class : class Foo extends Hoge<List<int[]>>"() {
		setup:
		def clazz = ClassSignatureParserTester.Generic.class

		when:
		new ClassSignatureParser({formals << it}, {supers << it})
		.parse(null, null, 'LReIW/tiny/cloneAny/pojo/impl/ClassSignatureParserTester$Generic<Ljava/util/List<[I>;>;')

		then:
		formals.size() == 0
		supers.size() == 1

		then:
		supers[0].typeParam == null
		supers[0].@descriptor == Type.getDescriptor(clazz)
		supers[0].slotList.size() == 1

		then:
		supers[0].slotList[0].typeParam == "="
		supers[0].slotList[0].@descriptor == "Ljava/util/List;"
		supers[0].slotList[0].slotList.size() == 1

		then:
		supers[0].slotList[0].slotList[0].typeParam == "="
		supers[0].slotList[0].slotList[0].@descriptor == '['
		supers[0].slotList[0].slotList[0].slotList.size() == 1
		
		then:
		supers[0].slotList[0].slotList[0].slotList[0].typeParam == null
		supers[0].slotList[0].slotList[0].slotList[0].@descriptor == 'I'
		supers[0].slotList[0].slotList[0].slotList[0].slotList == []
		
	}
}
