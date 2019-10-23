package ReIW.tiny.cloneAny.pojo.impl

import static org.junit.Assert.*

import org.junit.Test

import spock.lang.Specification

class MethodSignatureParserSpec extends Specification {

	def "non generic な引数"() {
		setup:
		// void foo(int, String)
		def descriptor = '(ILjava/lang/String;)V'
		def args = []

		when:
		new MethodSignatureParser({args << it},null).parseArgumentsAndReturn(descriptor, null);

		then:
		args.size() == 2
		
		then:
		args[0].typeParam == null
		args[0].descriptor == 'I'
		args[0].slotList.size() == 0
		
		then:
		args[1].typeParam == null
		args[1].descriptor == 'Ljava/lang/String;'
		args[1].slotList.size() == 0
	}

	def "配列の引数あり"() {
		setup:
		// void foo(Integer, String[])
		def descriptor = '(Ljava/lang/Integer;[Ljava/lang/String;)V'
		def args = []

		when:
		new MethodSignatureParser({args << it},null).parseArgumentsAndReturn(descriptor, null);

		then:
		args.size() == 2
		
		then:
		args[0].typeParam == null
		args[0].descriptor == 'Ljava/lang/Integer;'
		args[0].slotList.size() == 0
		
		then:
		args[1].typeParam == null
		args[1].@descriptor == '['
		args[1].slotList.size() == 1

		then:
		args[1].slotList[0].typeParam == null
		args[1].slotList[0].descriptor == 'Ljava/lang/String;'
		args[1].slotList[0].slotList.size() == 0
	}

	def "non generic な戻り値"() {
		setup:
		// String foo()
		def descriptor = '()Ljava/lang/String;'
		def ret = []

		when:
		new MethodSignatureParser(null, {ret << it}).parseArgumentsAndReturn(descriptor, null);

		then:
		ret.size() == 1
		
		then:
		ret[0].typeParam == null
		ret[0].descriptor == 'Ljava/lang/String;'
		ret[0].slotList.size() == 0

	}

	def "配列の戻り値"() {
		setup:
		// long[] foo()
		def descriptor = '()[J'
		def ret = []

		when:
		new MethodSignatureParser(null, {ret << it}).parseArgumentsAndReturn(descriptor, null);

		then:
		ret.size() == 1
		
		then:
		ret[0].typeParam == null
		ret[0].@descriptor == '['
		ret[0].slotList.size() == 1

		then:
		ret[0].slotList[0].typeParam == null
		ret[0].slotList[0].descriptor == 'J'
		ret[0].slotList[0].slotList.size() == 0
	}

	def "gneric なやつ"() {
		setup:
		// A[] foo(List<String>)
		def signature = '(Ljava/util/List<Ljava/lang/String;>;)[TA;'
		def args = []
		def ret = []

		when:
		new MethodSignatureParser({args << it}, {ret << it}).parseArgumentsAndReturn(null, signature);

		then:
		args.size() == 1
		ret.size() == 1
		
		then:
		args[0].typeParam == null
		args[0].descriptor == 'Ljava/util/List;'
		args[0].slotList.size() == 1

		then:
		args[0].slotList[0].typeParam == '='
		args[0].slotList[0].descriptor == 'Ljava/lang/String;'
		args[0].slotList[0].slotList.size() == 0

		then:
		ret[0].typeParam == null
		ret[0].@descriptor == '['
		ret[0].slotList.size() == 1

		then:
		ret[0].slotList[0].typeParam == 'A'
		ret[0].slotList[0].descriptor == 'Ljava/lang/Object;'
		ret[0].slotList[0].slotList.size() == 0

	}
}
