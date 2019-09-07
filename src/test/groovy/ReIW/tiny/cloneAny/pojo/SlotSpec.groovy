package ReIW.tiny.cloneAny.pojo

import spock.lang.Ignore
import spock.lang.Specification

class SlotSpec extends Specification {

	def 'getSlot simple object class'() {
		setup:
		def Slot slot

		when:
		slot = Slot.getSlot(null, Object.class)

		then:
		slot.getClassDescriptor() == "Ljava/lang/Object;"

		then:
		slot.isArrayType == false
		slot.isPrimitiveType == false
		slot.isMap == false
		slot.isList == false
		slot.isCharSequence == false
		slot.descriptor == "Ljava/lang/Object;"
		slot.slotList.size() == 0

		when:
		slot = Slot.getSlot(null, "Ljava/lang/Object;")

		then:
		slot.getClassDescriptor() == "Ljava/lang/Object;"

		then:
		slot.isArrayType == false
		slot.isPrimitiveType == false
		slot.isMap == false
		slot.isList == false
		slot.isCharSequence == false
		slot.descriptor == "Ljava/lang/Object;"
		slot.slotList.size() == 0
	}

	def 'instance of Map'() {
		// descriptor / class から作るので generic の型パラメタは作成されない
		setup:
		def Slot slot
		
		when:
		slot = Slot.getSlot(null, HashMap.class)
		
		then:
		slot.isArrayType == false
		slot.isPrimitiveType == false
		slot.isMap == true
		slot.isList == false
		slot.isCharSequence == false
		slot.descriptor == "Ljava/util/HashMap;"
		slot.slotList.size() == 0

		when:
		slot = Slot.getSlot(null, "Ljava/util/HashMap;")
		
		then:
		slot.isArrayType == false
		slot.isPrimitiveType == false
		slot.isMap == true
		slot.isList == false
		slot.isCharSequence == false
		slot.descriptor == "Ljava/util/HashMap;"
		slot.slotList.size() == 0
	}

	def 'instance of List'() {
		// descriptor / class から作るので generic の型パラメタは作成されない
		setup:
		def Slot slot
		
		when:
		slot = Slot.getSlot(null, ArrayList.class)
		
		then:
		slot.isArrayType == false
		slot.isPrimitiveType == false
		slot.isMap == false
		slot.isList == true
		slot.isCharSequence == false
		slot.descriptor == "Ljava/util/ArrayList;"
		slot.slotList.size() == 0

		when:
		slot = Slot.getSlot(null, "Ljava/util/ArrayList;")
		
		then:
		slot.isArrayType == false
		slot.isPrimitiveType == false
		slot.isMap == false
		slot.isList == true
		slot.isCharSequence == false
		slot.descriptor == "Ljava/util/ArrayList;"
		slot.slotList.size() == 0
	}

	def 'instance of CharSequence'() {
		setup:
		def Slot slot
		
		when:
		slot = Slot.getSlot(null, String.class)
		
		then:
		slot.isArrayType == false
		slot.isPrimitiveType == false
		slot.isMap == false
		slot.isList == false
		slot.isCharSequence == true
		slot.descriptor == "Ljava/lang/String;"
		slot.slotList.size() == 0

		when:
		slot = Slot.getSlot(null, "Ljava/lang/String;")
		
		then:
		slot.isArrayType == false
		slot.isPrimitiveType == false
		slot.isMap == false
		slot.isList == false
		slot.isCharSequence == true
		slot.descriptor == "Ljava/lang/String;"
		slot.slotList.size() == 0
	}

	def 'getSlot object array class'() {
		setup:
		def Slot slot

		when:
		slot = Slot.getSlot(null, Integer[].class)

		then:
		slot.getClassDescriptor() == "[Ljava/lang/Integer;"

		then:
		slot.isArrayType == true
		slot.descriptor == "["
		slot.slotList.size() == 1

		then:
		slot.slotList[0].isArrayType == false
		slot.slotList[0].isPrimitiveType == false
		slot.slotList[0].descriptor == "Ljava/lang/Integer;"
		slot.slotList[0].slotList.size() == 0

		when:
		slot = Slot.getSlot(null, "[Ljava/lang/Long;")

		then:
		slot.getClassDescriptor() == "[Ljava/lang/Long;"

		then:
		slot.isArrayType == true
		slot.descriptor == "["
		slot.slotList.size() == 1

		then:
		slot.slotList[0].isArrayType == false
		slot.slotList[0].isPrimitiveType == false
		slot.slotList[0].descriptor == "Ljava/lang/Long;"
		slot.slotList[0].slotList.size() == 0
	}

	def 'getSlot primitive array class'() {
		setup:
		def Slot slot

		when:
		slot = Slot.getSlot(null, int[][].class)

		then:
		slot.getClassDescriptor() == "[[I"

		then:
		slot.isArrayType == true
		slot.descriptor == "["
		slot.slotList.size() == 1

		then:
		slot.slotList[0].isArrayType == true
		slot.slotList[0].descriptor == "["
		slot.slotList[0].slotList.size() == 1

		then:
		slot.slotList[0].slotList[0].isArrayType == false
		slot.slotList[0].slotList[0].isPrimitiveType == true
		slot.slotList[0].slotList[0].descriptor == "I"
		slot.slotList[0].slotList[0].slotList.size() == 0

		when:
		slot = Slot.getSlot(null, "[[J")

		then:
		slot.getClassDescriptor() == "[[J"

		then:
		slot.isArrayType == true
		slot.descriptor == "["
		slot.slotList.size() == 1

		then:
		slot.slotList[0].isArrayType == true
		slot.slotList[0].descriptor == "["
		slot.slotList[0].slotList.size() == 1

		then:
		slot.slotList[0].slotList[0].isArrayType == false
		slot.slotList[0].slotList[0].isPrimitiveType == true
		slot.slotList[0].slotList[0].descriptor == "J"
		slot.slotList[0].slotList[0].slotList.size() == 0
	}


	@Ignore
	def "rebind"() {
		// TODO 未テスト
	}
}
