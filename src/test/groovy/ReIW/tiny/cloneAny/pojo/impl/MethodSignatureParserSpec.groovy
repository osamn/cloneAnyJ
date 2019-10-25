package ReIW.tiny.cloneAny.pojo.impl

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
		
		when:
		SlotValue arg_slot_1 = args[0]
		SlotValue arg_slot_2 = args[1]
		
		then:
		arg_slot_1.wildcard == null
		arg_slot_1.typeParam == null
		arg_slot_1.descriptor == 'I'
		arg_slot_1.slotList == []
		
		then:
		arg_slot_2.wildcard == null
		arg_slot_2.typeParam == null
		arg_slot_2.descriptor == 'Ljava/lang/String;'
		arg_slot_2.slotList == []
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
		
		when:
		SlotValue arg_slot_1 = args[0]
		SlotValue arg_slot_2 = args[1]
		
		then:
		arg_slot_1.wildcard == null
		arg_slot_1.typeParam == null
		arg_slot_1.descriptor == 'Ljava/lang/Integer;'
		arg_slot_1.slotList == []
		
		then:
		arg_slot_2.wildcard == null
		arg_slot_2.typeParam == null
		arg_slot_2.descriptor == '[Ljava/lang/String;'
		arg_slot_2.@descriptor == '['
		arg_slot_2.slotList.size() == 1

		then:
		arg_slot_2.slotList[0].wildcard == null
		arg_slot_2.slotList[0].typeParam == null
		arg_slot_2.slotList[0].descriptor == 'Ljava/lang/String;'
		arg_slot_2.slotList[0].slotList == []
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
		
		when:
		SlotValue ret_slot = ret[0]

		then:
		ret_slot.wildcard == null
		ret_slot.typeParam == null
		ret_slot.descriptor == 'Ljava/lang/String;'
		ret_slot.slotList == []

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
		
		when:
		SlotValue ret_slot = ret[0]

		then:
		ret_slot.wildcard == null
		ret_slot.typeParam == null
		ret_slot.descriptor == '[J'
		ret_slot.@descriptor == '['
		ret_slot.arrayType == true
		ret_slot.slotList.size() == 1

		then:
		ret_slot.slotList[0].wildcard == null
		ret_slot.slotList[0].typeParam == null
		ret_slot.slotList[0].descriptor == 'J'
		ret_slot.slotList[0].slotList == []

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
		
		when:
		SlotValue arg_slot = args[0]
		SlotValue ret_slot = ret[0]
		
		then:
		arg_slot.wildcard == null
		arg_slot.typeParam == null
		arg_slot.descriptor == 'Ljava/util/List;'
		arg_slot.slotList.size() == 1

		then:
		arg_slot.slotList[0].wildcard == '='
		arg_slot.slotList[0].typeParam == null
		arg_slot.slotList[0].descriptor == 'Ljava/lang/String;'
		arg_slot.slotList[0].slotList == []

		then:
		ret_slot.wildcard == null
		ret_slot.typeParam == null
		ret_slot.@descriptor == '['
		ret_slot.descriptor == '[Ljava/lang/Object;'
		ret_slot.slotList.size() == 1

		then:
		ret_slot.slotList[0].wildcard == null
		ret_slot.slotList[0].typeParam == 'A'
		ret_slot.slotList[0].descriptor == 'Ljava/lang/Object;'
		ret_slot.slotList[0].slotList == []

	}
}
