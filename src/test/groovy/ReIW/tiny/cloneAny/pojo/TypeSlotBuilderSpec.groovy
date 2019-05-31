package ReIW.tiny.cloneAny.pojo

import spock.lang.Specification

class TypeSlotBuilderSpec extends Specification {

	def "createTypeSlot シグネチャが null"() {
		when:
		def actual = TypeSlotBuilder.createTypeSlot(null)

		then:
		actual == null
	}

	def "createTypeSlot 型引数あり class Foo<A,B>"() {
		when:
		def actual = TypeSlotBuilder.createTypeSlot("<A:Ljava/lang/Object;B:Ljava/lang/Object;>Ljava/lang/Object;")

		then:
		actual.formalSlots.size() == 2
		actual.formalSlots[0].typeParam == "A"
		actual.formalSlots[0].typeClass == "java/lang/Object"
		actual.formalSlots[1].typeParam == "B"
		actual.formalSlots[1].typeClass == "java/lang/Object"
		actual.superSlot.typeParam == null
		actual.superSlot.typeClass == "java/lang/Object"
	}

	def "createTypeSlot 型引数あり  => class Foo<T,L extends List<T>>"() {
		when:
		def actual = TypeSlotBuilder.createTypeSlot("<T:Ljava/lang/Object;L::Ljava/util/List<TT;>;>Ljava/lang/Object;")

		then:
		actual.formalSlots.size() == 2
		actual.formalSlots[0].typeParam == "T"
		actual.formalSlots[0].typeClass == "java/lang/Object"
		actual.formalSlots[1].typeParam == "L"
		actual.formalSlots[1].typeClass == "java/util/List"
		actual.formalSlots[1].slotList.size()== 1
		actual.formalSlots[1].slotList[0].typeParam == "T"
		actual.formalSlots[1].slotList[0].typeClass == null
		actual.superSlot.typeParam == null
		actual.superSlot.typeClass == "java/lang/Object"
	}

	def "createTypeSlot 型引数なし generic 継承 => class Foo extends HashMap<Integer, ArrayList<String>"() {
		when:
		def actual = TypeSlotBuilder.createTypeSlot("Ljava/util/HashMap<Ljava/lang/Integer;Ljava/util/ArrayList<Ljava/lang/String;>;>;")

		then:
		actual.formalSlots.size() == 0
		actual.superSlot.typeParam == null
		actual.superSlot.typeClass == "java/util/HashMap"
		actual.superSlot.slotList[0].typeParam == "="
		actual.superSlot.slotList[0].typeClass == "java/lang/Integer"
		actual.superSlot.slotList[1].typeParam == "="
		actual.superSlot.slotList[1].typeClass == "java/util/ArrayList"
		actual.superSlot.slotList[1].slotList[0].typeParam == "="
		actual.superSlot.slotList[1].slotList[0].typeClass == "java/lang/String"
	}

}
