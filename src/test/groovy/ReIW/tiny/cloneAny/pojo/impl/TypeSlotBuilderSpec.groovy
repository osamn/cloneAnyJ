package ReIW.tiny.cloneAny.pojo.impl

import ReIW.tiny.cloneAny.pojo.Accessor
import spock.lang.Specification

class TypeSlotBuilderSpec extends Specification {

	def "field : public で non static だけとれること"() {
		when:
		def typeSlot = new TypeSlotBuilder().buildTypeSlot(TypeSlotBuilderTester.Field.class)

		then:
		typeSlot.access.size() == 1
		typeSlot.access[0].getType() == Accessor.Type.Field
		typeSlot.access[0].getName() == "publicField"
		typeSlot.access[0].getDescriptor() == "Ljava/lang/String;"
		typeSlot.access[0].slot.descriptor == "Ljava/lang/String;"

		// 以下はおまけでここだけ一応チェックしとく

		then:
		typeSlot.typeParam == null;
		typeSlot.getTypeDescriptor() == 'LReIW/tiny/cloneAny/pojo/impl/TypeSlotBuilderTester$Field;'
		typeSlot.slotList.size()== 0

		then:
		typeSlot.superSlots[0].typeParam == null
		typeSlot.superSlots[0].descriptor == "Ljava/lang/Object;"
		typeSlot.superSlots.size() == 1
	}

	def "final field は readonly になること"() {
		when:
		def typeSlot = new TypeSlotBuilder().buildTypeSlot(TypeSlotBuilderTester.ReadonlyField.class)

		then:
		typeSlot.access.size() == 1
		typeSlot.access[0].getType() == Accessor.Type.ReadonlyField
		typeSlot.access[0].getName() == "publicField"
		// primitive も問題ないよね
		typeSlot.access[0].getDescriptor() == "I"
		typeSlot.access[0].slot.descriptor == "I"
	}

	def "getter + abstract は対象にならないこと"() {
		when:
		def typeSlot = new TypeSlotBuilder().buildTypeSlot(TypeSlotBuilderTester.Getter.class)

		then:
		typeSlot.access.size() == 1
		typeSlot.access[0].getType() == Accessor.Type.Get
		typeSlot.access[0].getName() == "public"
		typeSlot.access[0].getRel() == "getPublic"
		typeSlot.access[0].getDescriptor() == "()Ljava/lang/Integer;"
		typeSlot.access[0].slot.descriptor == "Ljava/lang/Integer;"
	}

	def "setter + 引数型の違うものは異なった accessor として取れること"() {
		when:
		def typeSlot = new TypeSlotBuilder().buildTypeSlot(TypeSlotBuilderTester.Setter.class)

		then:
		typeSlot.access.size() == 2

		then:
		typeSlot.access[0].getType() == Accessor.Type.Set
		typeSlot.access[0].getName() == "foo"
		typeSlot.access[0].getRel() == "setFoo"
		typeSlot.access[0].getDescriptor() == "(Ljava/lang/String;)V"
		typeSlot.access[0].slot.descriptor == "Ljava/lang/String;"

		then:
		typeSlot.access[1].getType() == Accessor.Type.Set
		typeSlot.access[1].getName() == "foo"
		typeSlot.access[1].getRel() == "setFoo"
		typeSlot.access[1].getDescriptor() == "(Ljava/lang/Integer;)V"
		typeSlot.access[1].slot.descriptor == "Ljava/lang/Integer;"
	}

	def "引数ありコンストラクタ"() {
		when:
		def typeSlot = new TypeSlotBuilder().buildTypeSlot(TypeSlotBuilderTester.Ctor.class)

		then:
		typeSlot.access.size() == 2

		then:
		typeSlot.access[0].getType() == Accessor.Type.LumpSet
		typeSlot.access[0].getName() == "<init>"
		typeSlot.access[0].getRel() == "<init>"
		typeSlot.access[0].getDescriptor() == "(Ljava/lang/String;)V"
		typeSlot.access[0].slotInfo().collect {
			[it.param, it.slot.typeParam, it.slot.descriptor, it.slot.slotList]
		} ==
		[["hoge", null, "Ljava/lang/String;", []]]

		then:
		typeSlot.access[1].getType() == Accessor.Type.LumpSet
		typeSlot.access[1].getName() == "<init>"
		typeSlot.access[1].getRel() == "<init>"
		typeSlot.access[1].getDescriptor() == "(ZLjava/lang/Integer;)V"
		typeSlot.access[1].names == ["foo", "bar"]
		typeSlot.access[1].slotInfo().collect {
			[it.param, it.slot.typeParam, it.slot.descriptor, it.slot.slotList]
		} == [["foo", null, "Z", []], ["bar", null, "Ljava/lang/Integer;", []]]
	}

	def "デフォルトコンストラクタ"() {
		when:
		def typeSlot = new TypeSlotBuilder().buildTypeSlot(TypeSlotBuilderTester.DefaultCtor.class)

		then:
		typeSlot.access.size() == 1

		then:
		typeSlot.access[0].getType() == Accessor.Type.LumpSet
		typeSlot.access[0].getName() == "<init>"
		typeSlot.access[0].getRel() == "<init>"
		typeSlot.access[0].getDescriptor() == "()V"
		typeSlot.access[0].slotInfo().collect {[it]} == []
	}
}
