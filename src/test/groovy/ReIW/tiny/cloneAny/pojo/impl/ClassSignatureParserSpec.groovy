package ReIW.tiny.cloneAny.pojo.impl

import spock.lang.Specification

class ClassSignatureParserSpec extends Specification {

	def formals
	def supers

	def setup() {
		formals = []
		supers = []
	}

	def "simple class : class Foo extends Hoge"() {
		when:
		new ClassSignatureParser({formals << it}, {supers << it}) .parse("foo/bar/Hoge", null, null)

		then:
		formals.size() == 0
		supers.size() == 1

		then:
		supers[0].typeParam == null
		supers[0].descriptor == "Lfoo/bar/Hoge;"
		supers[0].slotList == []
	}

	def "extends generic class : class Foo extends Hoge<String>"() {
		when:
		new ClassSignatureParser({formals << it}, {supers << it}) .parse(null, null, "Lfoo/bar/Hoge<Ljava/lang/String;>;")

		then:
		formals.size() == 0
		supers.size() == 1

		then:
		supers[0].typeParam == null
		supers[0].descriptor == "Lfoo/bar/Hoge;"
		supers[0].slotList.size() == 1

		then:
		supers[0].slotList[0].typeParam == "="
		supers[0].slotList[0].descriptor == "Ljava/lang/String;"
		supers[0].slotList[0].slotList == []
	}

	def "implements non generic interfaces : class Foo implements Hoge, Piyo"() {
		when:
		new ClassSignatureParser({formals << it}, {supers << it}) .parse("java/lang/Object", ["foo/bar/Hoge", "foo/bar/Piyo"] as String[], null)

		then:
		formals.size() == 0
		supers.size() == 3

		then:
		supers[0].typeParam == null
		supers[0].descriptor == "Ljava/lang/Object;"
		supers[0].slotList == []

		then:
		supers[1].typeParam == null
		supers[1].descriptor == "Lfoo/bar/Hoge;"
		supers[1].slotList == []

		then:
		supers[2].typeParam == null
		supers[2].descriptor == "Lfoo/bar/Piyo;"
		supers[2].slotList == []
	}

	def "implements generic interface : class Foo implements Hoge<String[]>"() {
		when:
		new ClassSignatureParser({formals << it}, {supers << it}) .parse(null, null, "Ljava/lang/Object;Lfoo/bar/Hoge<[Ljava/lang/String;>;")

		then:
		formals.size() == 0
		supers.size() == 2

		then:
		supers[0].typeParam == null
		supers[0].descriptor == "Ljava/lang/Object;"
		supers[0].slotList == []

		then:
		supers[1].typeParam == null
		supers[1].descriptor == "Lfoo/bar/Hoge;"
		supers[1].slotList.size() == 1

		then:
		supers[1].slotList[0].typeParam == "="
		supers[1].slotList[0].descriptor == "["
		supers[1].slotList[0].slotList.size() == 1

		then:
		supers[1].slotList[0].slotList[0].typeParam == null
		supers[1].slotList[0].slotList[0].descriptor == "Ljava/lang/String;"
		supers[1].slotList[0].slotList[0].slotList == []
	}

	//def "implements generic intreface : class Foo implements Hoge<String>"() { }

	def "generic class : class Foo<K,V>"() {
		when:
		new ClassSignatureParser({formals << it}, {supers << it}) .parse(null, null, "<K:Ljava/lang/Object;V:Ljava/lang/Object;>Ljava/lang/Object;")

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
	
	def "partial bound generic class : class Foo<K> extends Hoge<K, String[]>"() {
		when:
		new ClassSignatureParser({formals << it}, {supers << it}) .parse(null, null, "<K:Ljava/lang/Object;>Lfoo/bar/Hoge<TK;[Ljava/lang/String;>;")

		then:
		formals.size() == 1
		supers.size() == 1

		then:
		formals[0].typeParam == "K"
		formals[0].descriptor == "Ljava/lang/Object;"
		formals[0].slotList == []

		then:
		supers[0].typeParam == null
		supers[0].descriptor == "Lfoo/bar/Hoge;"
		supers[0].slotList.size() == 2
		
		then:
		supers[0].slotList[0].typeParam == "K"
		supers[0].slotList[0].descriptor == "Ljava/lang/Object;"
		supers[0].slotList[0].slotList == []
		
		then:
		supers[0].slotList[1].typeParam == "="
		supers[0].slotList[1].descriptor == "["
		supers[0].slotList[1].slotList.size() == 1
		
		then:
		supers[0].slotList[1].slotList[0].typeParam == null
		supers[0].slotList[1].slotList[0].descriptor == "Ljava/lang/String;"
		supers[0].slotList[1].slotList[0].slotList == []
	}
	
	def "extends nested generic class : class Foo extends Hoge<List<String>>"() {
		when:
		new ClassSignatureParser({formals << it}, {supers << it}) .parse(null, null, "Lfoo/bar/Hoge<Ljava/util/List<Ljava/lang/String;>;>;")
		
		then:
		formals.size() == 0
		supers.size() == 1

		then:
		supers[0].typeParam == null
		supers[0].descriptor == "Lfoo/bar/Hoge;"
		supers[0].slotList.size() == 1
		
		then:
		supers[0].slotList[0].typeParam == "="
		supers[0].slotList[0].descriptor == "Ljava/util/List;"
		supers[0].slotList[0].slotList.size() == 1
		
		then:
		supers[0].slotList[0].slotList[0].typeParam == "="
		supers[0].slotList[0].slotList[0].descriptor == "Ljava/lang/String;"
		supers[0].slotList[0].slotList[0].slotList == []
	}

}
