package ReIW.tiny.cloneAny.pojo.impl

import ReIW.tiny.cloneAny.pojo.Slot
import ReIW.tiny.cloneAny.pojo.UnboundFormalTypeParameterException
import spock.lang.Specification

class MethodSignatureParserSpec extends Specification {

	def "non generic object : String get..."() {
		setup:
		def Slot slot

		when:
		new MethodSignatureParser(null, {slot = it}).parseArgumentsAndReturn("()Ljava/lang/String;", null)

		then:
		slot.typeParam == null
		slot.@descriptor == "Ljava/lang/String;"
		slot.slotList == []
	}

	def "non generic object array getter : String[][] get..."() {
		setup:
		def Slot slot
		def Slot slot_1
		def Slot slot_2

		when:
		new MethodSignatureParser(null, {slot = it}).parseArgumentsAndReturn("()[[Ljava/lang/String;", null)
		println slot
		slot_1 = slot.slotList[0]
		slot_2 = slot_1.slotList[0]

		then:
		slot.typeParam == null
		slot.@descriptor == "["
		slot.slotList.size() == 1

		then:
		slot_1.typeParam == null
		slot_1.@descriptor == "["
		slot_1.slotList.size() == 1

		then:
		slot_2.typeParam == null
		slot_2.@descriptor == "Ljava/lang/String;"
		slot_2.slotList == []
	}

	def "generic object getter : Map<String, Integer> get..."(){
		setup:
		def Slot slot
		def Slot slot_1
		def Slot slot_2

		when:
		new MethodSignatureParser(null, {slot = it}).parseArgumentsAndReturn(null, "()Ljava/util/Map<Ljava/lang/String;Ljava/lang/Integer;>;")
		slot_1 = slot.slotList[0]
		slot_2 = slot.slotList[1]

		then:
		slot.typeParam == null
		slot.@descriptor == "Ljava/util/Map;"
		slot.slotList.size() == 2

		slot_1.typeParam == "="
		slot_1.@descriptor == "Ljava/lang/String;"
		slot_1.slotList == []

		slot_2.typeParam == "="
		slot_2.@descriptor == "Ljava/lang/Integer;"
		slot_2.slotList == []
	}

	def "unbound generic object getter : Map<K, Integer> get..."() {
		setup:
		def Slot slot
		def Slot slot_1
		def Slot slot_2

		when:
		new MethodSignatureParser(null, {slot = it}).parseArgumentsAndReturn(null, "()Ljava/util/Map<TK;Ljava/lang/Integer;>;")
		slot_1 = slot.slotList[0]
		slot_2 = slot.slotList[1]

		then:
		slot.typeParam == null
		slot.@descriptor == "Ljava/util/Map;"
		slot.slotList.size() == 2

		slot_1.typeParam == "K"
		slot_1.@descriptor == "Ljava/lang/Object;"
		slot_1.slotList == []

		slot_2.typeParam == "="
		slot_2.@descriptor == "Ljava/lang/Integer;"
		slot_2.slotList == []
	}

	def "generic object array getter : List<String[][]>[][] get..."(){
		setup:
		def Slot slot
		def Slot slot_1
		def Slot slot_2
		def Slot slot_3
		def Slot slot_4
		def Slot slot_5

		when:
		new MethodSignatureParser(null, {slot = it}).parseArgumentsAndReturn(null, "()[[Ljava/util/List<[[Ljava/lang/String;>;")
		slot_1 = slot.slotList[0]
		slot_2 = slot_1.slotList[0]
		slot_3 = slot_2.slotList[0]
		slot_4 = slot_3.slotList[0]
		slot_5 = slot_4.slotList[0]

		then:
		slot.typeParam == null
		slot.@descriptor == "["
		slot.slotList.size() == 1

		slot_1.typeParam == null
		slot_1.@descriptor == "["
		slot_1.slotList.size() == 1

		slot_2.typeParam == null
		slot_2.@descriptor == "Ljava/util/List;"
		slot_2.slotList.size() == 1

		slot_3.typeParam == "="
		slot_3.@descriptor == "["
		slot_3.slotList.size() == 1

		slot_4.typeParam == null
		slot_4.@descriptor == "["
		slot_4.slotList.size() == 1

		slot_5.typeParam == null
		slot_5.@descriptor == "Ljava/lang/String;"
		slot_5.slotList == []
	}

	def "non generic object array setter : set...(String[])"() {
		setup:
		def Slot slot
		def Slot slot_1
		def Slot slot_2

		when:
		new MethodSignatureParser({slot = it}, null).parseArgumentsAndReturn("([Ljava/lang/String;)V", null)
		slot_1 = slot.slotList.get(0)

		then:
		slot.typeParam == null
		slot.@descriptor == "["
		slot.slotList.size() == 1

		then:
		slot_1.typeParam == null
		slot_1.@descriptor == "Ljava/lang/String;"
		slot_1.slotList.size() == 0
	}

	def "generic object array setter : set...(List<String[]>[])"(){
		setup:
		def Slot slot
		def Slot slot_1
		def Slot slot_2
		def Slot slot_3

		when:
		new MethodSignatureParser({slot = it}, null).parseArgumentsAndReturn(null, "([Ljava/util/List<[Ljava/lang/String;>;)V")
		slot_1 = slot.slotList.get(0)
		slot_2 = slot_1.slotList.get(0)
		slot_3 = slot_2.slotList.get(0)

		then:
		slot.typeParam == null
		slot.@descriptor == "["
		slot.slotList.size() == 1

		slot_1.typeParam == null
		slot_1.@descriptor == "Ljava/util/List;"
		slot_1.slotList.size() == 1

		slot_2.typeParam == "="
		slot_2.@descriptor == "["
		slot_2.slotList.size() == 1

		slot_3.typeParam == null
		slot_3.@descriptor == "Ljava/lang/String;"
		slot_3.slotList.size() == 0
	}

	def "non generic lump setter : set...(String, Integer)"() {
		setup:
		def slots = []

		when:
		new MethodSignatureParser({slots<< it}, null).parseArgumentsAndReturn("(Ljava/lang/String;Ljava/lang/Integer;I)V", null)

		then:
		slots.size() == 3

		then:
		slots[0].typeParam == null
		slots[0].@descriptor == "Ljava/lang/String;"
		slots[0].slotList.size() == 0

		then:
		slots[1].typeParam == null
		slots[1].@descriptor == "Ljava/lang/Integer;"
		slots[1].slotList.size() == 0

		then:
		slots[2].typeParam == null
		slots[2].@descriptor == "I"
		slots[2].slotList.size() == 0
	}
	
	def "lump setter mixed : set...(String[], List<Long>, int[])"() {
		setup:
		def slots = []

		when:
		new MethodSignatureParser({slots<< it}, null).parseArgumentsAndReturn(null, "([Ljava/lang/String;Ljava/util/List<Ljava/lang/Long;>;[I)V")

		then:
		slots.size() == 3

		then:
		slots[0].typeParam == null
		slots[0].@descriptor == "["
		slots[0].slotList.size() == 1
		slots[0].slotList[0].typeParam == null
		slots[0].slotList[0].@descriptor == "Ljava/lang/String;"
		slots[0].slotList[0].slotList.size() == 0

		then:
		slots[1].typeParam == null
		slots[1].@descriptor == "Ljava/util/List;"
		slots[1].slotList.size() == 1
		slots[1].slotList[0].typeParam == "="
		slots[1].slotList[0].@descriptor == "Ljava/lang/Long;"
		slots[1].slotList[0].slotList.size() == 0

		then:
		slots[2].typeParam == null
		slots[2].@descriptor == "["
		slots[2].slotList.size() == 1
		slots[2].slotList[0].typeParam == null
		slots[2].slotList[0].@descriptor == "I"
		slots[2].slotList[0].slotList.size() == 0
	}
	
	def "has formal parameter method throws error : <X> void set...(X)"() {
		when:
		new MethodSignatureParser(null, null).parseArgumentsAndReturn(null, "<X:Ljava/lang/Object;>(TX;)V")

		then:
		thrown(UnboundFormalTypeParameterException)
	}
	
	def "primitive array getter"() {
		setup:
		def slots = []

		when:
		new MethodSignatureParser(null , {slots << it}).parseArgumentsAndReturn("()[J", null)

		
		then:
		slots.size() == 1

		then:
		slots[0].typeParam == null
		slots[0].@descriptor == "["
		slots[0].slotList.size() == 1
		
		then:
		slots[0].slotList[0].typeParam == null
		slots[0].slotList[0].@descriptor == 'J'
		slots[0].slotList[0].slotList.size() == 0
	}
	
	def "primitive array setter"() {
		setup:
		def slots = []

		when:
		new MethodSignatureParser({slots << it}, null).parseArgumentsAndReturn("([I)V", null)

		
		then:
		slots.size() == 1

		then:
		slots[0].typeParam == null
		slots[0].@descriptor == "["
		slots[0].slotList.size() == 1
		
		then:
		slots[0].slotList[0].typeParam == null
		slots[0].slotList[0].@descriptor == 'I'
		slots[0].slotList[0].slotList.size() == 0
	}
}
