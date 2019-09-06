package ReIW.tiny.cloneAny.pojo.impl

import org.objectweb.asm.Type

import ReIW.tiny.cloneAny.pojo.Accessor
import ReIW.tiny.cloneAny.pojo.Slot
import spock.lang.Specification

class TypeSlotSpec extends Specification {

	def "継承元のアクセスが抽出されること"() {
		when:
		def ts = TypeSlotBuilder.createTypeSlot(TypeSlotTester.Simple)
		def acc = ts.access.collect {
			[
				it.type,
				it.owner,
				it.name,
				it.rel,
				it.descriptor
			]
		}

		then:
		ts.access.size() == 6

		then:
		acc[0] == [
			Accessor.Type.Field,
			Type.getInternalName(TypeSlotTester.Simple),
			"thisField",
			"thisField",
			"Ljava/lang/String;"
		]
		acc[1] == [
			Accessor.Type.Get,
			Type.getInternalName(TypeSlotTester.Simple),
			"thisVal",
			"getThisVal",
			"()D"
		]
		acc[2] == [
			Accessor.Type.LumpSet,
			Type.getInternalName(TypeSlotTester.Simple),
			"<init>",
			"<init>",
			"(Ljava/lang/String;)V"
		]
		acc[3] == [
			Accessor.Type.Set,
			Type.getInternalName(TypeSlotTester.Simple),
			"superVal",
			"setSuperVal",
			"(Ljava/lang/String;)V"
		]
		acc[4] == [
			Accessor.Type.ReadonlyField,
			Type.getInternalName(TypeSlotTester.SimpleBase),
			"superField",
			"superField",
			"Ljava/lang/String;"
		]
		acc[5] == [
			Accessor.Type.Set,
			Type.getInternalName(TypeSlotTester.SimpleBase),
			"superVal",
			"setSuperVal",
			"(Ljava/lang/Long;)V"
		]

		then:
		ts.access[2].names == ["thisCtorArg"]
	}

	def "暗黙の bind で継承元まで型引数が bind されてること"() {
		when:
		def ts = TypeSlotBuilder.createTypeSlot(TypeSlotTester.GenericExtends)
		def acc = ts.access.collect {
			[
				it.type,
				it.owner,
				it.name,
				it.rel,
				it.descriptor, // generic の場合 parse 時点の情報になるので Object になってる
			]
		}

		then:
		ts.access.size() == 4

		then:
		acc[0] == [
			Accessor.Type.LumpSet,
			Type.getInternalName(TypeSlotTester.GenericExtends),
			"<init>",
			"<init>",
			"()V"
		]

		then:
		acc[1] == [
			Accessor.Type.Get,
			Type.getInternalName(TypeSlotTester.GenericExtends),
			"extendsVal",
			"getExtendsVal",
			"()Ljava/lang/Object;"
		]
		ts.access[1].slot.typeParam == "B"
		ts.access[1].slot.getClassDescriptor() == "Ljava/lang/Object;"

		then:
		acc[2] == [
			Accessor.Type.Field,
			Type.getInternalName(TypeSlotTester.GenericBase),
			"baseVal",
			"baseVal",
			"Ljava/lang/Object;"
		]
		ts.access[2].slot.typeParam == "="
		ts.access[2].slot.getClassDescriptor() == "Ljava/lang/String;"

		acc[3] == [
			Accessor.Type.Set,
			Type.getInternalName(TypeSlotTester.GenericBase),
			"baseVal",
			"setBaseVal",
			"(Ljava/lang/Object;)V"
		]
		ts.access[3].slot.typeParam == "A"
		ts.access[3].slot.getClassDescriptor() == "Ljava/lang/Object;"
	}

	def "明示的な bind で継承元まで型引数が bind されてること"() {
		when:
		def ts = TypeSlotBuilder.createTypeSlot(TypeSlotTester.GenericExtends)
				.bind([
					Slot.getSlot("Ljava/lang/Double;"),
					Slot.getSlot("[[I")
				])
		def access = ts.accessors().toArray()

		then:
		access.size() == 4

		then:
		access[1].slot.typeParam == "="
		access[1].slot.getClassDescriptor() == "[[I"

		then:
		access[2].slot.typeParam == "="
		access[2].slot.getClassDescriptor() == "Ljava/lang/String;"
		
		then:
		access[3].slot.typeParam == "="
		access[3].slot.getClassDescriptor() == "Ljava/lang/Double;"
	}
	
	def "ctor の暗黙的な bind"() {
		// TODO test	
	}
	
	def "ctor の明示的な bind"() {
		// TODO test	
	}
}
