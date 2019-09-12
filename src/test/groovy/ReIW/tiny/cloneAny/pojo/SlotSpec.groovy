package ReIW.tiny.cloneAny.pojo

import org.objectweb.asm.Type

import spock.lang.Specification

class SlotSpec extends Specification {

	def 'getSlot simple object class'() {
		when:
		def Slot slot = new Slot(null, Type.getDescriptor(Object.class))

		then:
		slot.isArrayType == false
		slot.isPrimitiveType == false
		slot.isBoxingType == false
		slot.descriptor == "Ljava/lang/Object;"
		slot.slotList.size() == 0
	}

	def 'generic なクラスだけど型パラメタの子スロットは追加されない'() {
		// descriptor から作るので generic の型パラメタは作成されない
		when:
		def Slot slot = Slot.getSlot(null, Type.getDescriptor(HashMap))

		then:
		slot.isArrayType == false
		slot.isPrimitiveType == false
		slot.descriptor == "Ljava/util/HashMap;"
		slot.slotList.size() == 0
	}

	def 'object array class'() {
		when:

		def Slot slot = Slot.getSlot(null, "[Ljava/lang/Integer;")

		then:
		slot.isArrayType == true
		slot.descriptor == "["
		slot.slotList.size() == 1

		then:
		slot.slotList[0].isArrayType == false
		slot.slotList[0].isPrimitiveType == false
		slot.slotList[0].isBoxingType == true
		slot.slotList[0].descriptor == "Ljava/lang/Integer;"
		slot.slotList[0].slotList.size() == 0
	}

	def 'primitive array class'() {
		when:
		def Slot slot = Slot.getSlot(null, Type.getDescriptor(int[][]))

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
		slot.slotList[0].slotList[0].isBoxingType == false
		slot.slotList[0].slotList[0].descriptor == "I"
		slot.slotList[0].slotList[0].slotList.size() == 0
	}


	def "rebind slot"() {
		def actual

		setup:
		def slot = new Slot(null, Type.getDescriptor(Object))
		slot.slotList.add(new Slot('X', Type.getDescriptor(Object)))
		slot.slotList.add(new Slot('Y', Type.getDescriptor(Object)))
		slot.slotList.add(new Slot('Z', Type.getDescriptor(Object)))

		when:
		actual = slot.rebind(['X':'TA', 'Y':'Ljava/lang/String;', 'Z':'[I'])

		then:
		actual.slotList.size() == 3

		then:
		actual.slotList[0].typeParam == 'A'
		actual.slotList[0].descriptor == 'Ljava/lang/Object;'

		then:
		actual.slotList[1].typeParam == '='
		actual.slotList[1].descriptor == 'Ljava/lang/String;'

		then:
		actual.slotList[2].typeParam == '='
		actual.slotList[2].descriptor == '['
		actual.slotList[2].slotList[0].descriptor == 'I'

		when:
		actual = actual.rebind(['A':'Ljava/lang/Integer;'])

		then:
		actual.slotList.size() == 3

		then:
		actual.slotList[0].typeParam == '='
		actual.slotList[0].descriptor == 'Ljava/lang/Integer;'

		then:
		actual.slotList[1].typeParam == '='
		actual.slotList[1].descriptor == 'Ljava/lang/String;'

		then:
		actual.slotList[2].typeParam == '='
		actual.slotList[2].descriptor == '['
		actual.slotList[2].slotList[0].descriptor == 'I'
	}
	
	def "rebind with generic type"() {
		def Slot actual

		setup:
		def slot = new Slot(null, 'Lfoo/bar/Hoge;')
		slot.slotList.add(new Slot('X', Type.getDescriptor(Object)))

		when:
		actual = slot.rebind(['X':'[Ljava/util/List<Ljava/lang/String;>;'])

		then:
		actual.getTypeSignature() == 'Lfoo/bar/Hoge<[Ljava/util/List<Ljava/lang/String;>;>;'
		
		then:
		actual.descriptor == 'Lfoo/bar/Hoge;'
		actual.slotList.size() == 1
		
		then:
		actual.slotList[0].descriptor == '['
		actual.slotList[0].slotList.size() == 1
		
		then:
		actual.slotList[0].slotList[0].descriptor == 'Ljava/util/List;'
		actual.slotList[0].slotList[0].slotList.size() == 1
		
		then:
		actual.slotList[0].slotList[0].slotList[0].descriptor == 'Ljava/lang/String;'
		actual.slotList[0].slotList[0].slotList[0].slotList.size() == 0
	}

	def "rebind with 未解決あり generic type"() {
		def Slot actual

		setup:
		def slot = new Slot(null, 'Lfoo/bar/Hoge;')
		slot.slotList.add(new Slot('X', Type.getDescriptor(Object)))

		when:
		actual = slot.rebind(['X':'Ljava/util/List<[TA;>;'])

		then:
		actual.getTypeSignature() == 'Lfoo/bar/Hoge<Ljava/util/List<[TA;>;>;'
		
		then:
		actual.descriptor == 'Lfoo/bar/Hoge;'
		actual.slotList.size() == 1
		
		then:
		actual.slotList[0].typeParam == '='
		actual.slotList[0].descriptor == 'Ljava/util/List;'
		actual.slotList[0].slotList.size() == 1
		
		then:
		actual.slotList[0].slotList[0].typeParam == '='
		actual.slotList[0].slotList[0].descriptor == '['
		actual.slotList[0].slotList[0].slotList.size() == 1
		
		then:
		actual.slotList[0].slotList[0].slotList[0].typeParam == 'A'
		actual.slotList[0].slotList[0].slotList[0].descriptor == 'Ljava/lang/Object;'
		actual.slotList[0].slotList[0].slotList[0].slotList.size() == 0
	}
}
