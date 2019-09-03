package ReIW.tiny.cloneAny.pojo.impl

import ReIW.tiny.cloneAny.pojo.Slot
import spock.lang.Specification

class FieldSignatureParserSpec extends Specification{

	def "non generic object"() {
		setup:
		def Slot slot

		when:
		new FieldSignatureParser({slot = it}).parse("Lfoo/bar/Hoge;", null)

		then:
		slot.typeParam == null
		slot.descriptor == "Lfoo/bar/Hoge;"
		slot.slotList == []
	}

	def "non generic object array : String[][]"() {
		setup:
		def Slot slot
		def Slot slot_1
		def Slot slot_2

		when:
		new FieldSignatureParser({slot = it}).parse("[[Ljava/lang/String;", null)
		slot_1 = slot.slotList[0]
		slot_2 = slot_1.slotList[0]

		then:
		slot.typeParam == null
		slot.descriptor == "["
		slot.slotList.size() == 1

		slot_1.typeParam == null
		slot_1.descriptor == "["
		slot_1.slotList.size() == 1

		slot_2.typeParam == null
		slot_2.descriptor == "Ljava/lang/String;"
		slot_2.slotList == []
	}

	def "generic object : Map<String, Integer>"(){
		setup:
		def Slot slot
		def Slot slot_1
		def Slot slot_2

		when:
		new FieldSignatureParser({slot = it}).parse(null, "Ljava/util/Map<Ljava/lang/String;Ljava/lang/Integer;>;")
		slot_1 = slot.slotList[0]
		slot_2 = slot.slotList[1]

		then:
		slot.typeParam == null
		slot.descriptor == "Ljava/util/Map;"
		slot.slotList.size() == 2

		slot_1.typeParam == "="
		slot_1.descriptor == "Ljava/lang/String;"
		slot_1.slotList == []

		slot_2.typeParam == "="
		slot_2.descriptor == "Ljava/lang/Integer;"
		slot_2.slotList == []
	}

	def "unbound generic object : Map<K, Integer>"() {
		setup:
		def Slot slot
		def Slot slot_1
		def Slot slot_2

		when:
		new FieldSignatureParser({slot = it}).parse(null, "Ljava/util/Map<TK;Ljava/lang/Integer;>;")
		slot_1 = slot.slotList[0]
		slot_2 = slot.slotList[1]

		then:
		slot.typeParam == null
		slot.descriptor == "Ljava/util/Map;"
		slot.slotList.size() == 2

		slot_1.typeParam == "K"
		slot_1.descriptor == "Ljava/lang/Object;"
		slot_1.slotList == []

		slot_2.typeParam == "="
		slot_2.descriptor == "Ljava/lang/Integer;"
		slot_2.slotList == []
	}

	def "generic object array : List<String[][]>[][]"(){
		setup:
		def Slot slot
		def Slot slot_1
		def Slot slot_2
		def Slot slot_3
		def Slot slot_4
		def Slot slot_5

		when:
		new FieldSignatureParser({slot = it}).parse(null, "[[Ljava/util/List<[[Ljava/lang/String;>;")
		slot_1 = slot.slotList[0]
		slot_2 = slot_1.slotList[0]
		slot_3 = slot_2.slotList[0]
		slot_4 = slot_3.slotList[0]
		slot_5 = slot_4.slotList[0]

		then:
		slot.typeParam == null
		slot.descriptor == "["
		slot.slotList.size() == 1

		slot_1.typeParam == null
		slot_1.descriptor == "["
		slot_1.slotList.size() == 1

		slot_2.typeParam == null
		slot_2.descriptor == "Ljava/util/List;"
		slot_2.slotList.size() == 1

		slot_3.typeParam == "="
		slot_3.descriptor == "["
		slot_3.slotList.size() == 1

		slot_4.typeParam == null
		slot_4.descriptor == "["
		slot_4.slotList.size() == 1

		slot_5.typeParam == null
		slot_5.descriptor == "Ljava/lang/String;"
		slot_5.slotList == []
	}
}
