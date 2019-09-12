package ReIW.tiny.cloneAny.pojo.impl

import org.objectweb.asm.Type

import ReIW.tiny.cloneAny.pojo.Accessor
import ReIW.tiny.cloneAny.pojo.Slot
import spock.lang.Specification

class TypeSlotSpec extends Specification {

	def "継承元のアクセスが抽出されること"() {
		when:
		def ts = TypeSlotBuilder.build(TypeSlotTester.Simple)
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
			Accessor.Type.LumpSet,
			Type.getInternalName(TypeSlotTester.Simple),
			"<init>",
			"<init>",
			"(Ljava/lang/String;)V"
		]
		acc[1] == [
			Accessor.Type.Field,
			Type.getInternalName(TypeSlotTester.Simple),
			"thisField",
			"thisField",
			"Ljava/lang/String;"
		]
		acc[2] == [
			Accessor.Type.Get,
			Type.getInternalName(TypeSlotTester.Simple),
			"thisVal",
			"getThisVal",
			"()D"
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
		ts.access[0].names == ["thisCtorArg"]
	}

	def "暗黙の bind で継承元まで型引数が bind されてること"() {
		when:
		def ts = TypeSlotBuilder.build(TypeSlotTester.GenericExtends)
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

		println ts.access.findAll {it in SingleSlotAccessor}.forEach {println it.slot}
		println ts.access.forEach {println it}

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
		ts.access[1].slot.descriptor == "Ljava/lang/Object;"

		then:
		acc[2] == [
			Accessor.Type.Field,
			Type.getInternalName(TypeSlotTester.GenericBase),
			"baseVal",
			"baseVal",
			"Ljava/lang/Object;"
		]
		ts.access[2].slot.typeParam == "="
		ts.access[2].slot.descriptor == "Ljava/lang/String;"

		acc[3] == [
			Accessor.Type.Set,
			Type.getInternalName(TypeSlotTester.GenericBase),
			"baseVal",
			"setBaseVal",
			"(Ljava/lang/Object;)V"
		]
		ts.access[3].slot.typeParam == "A"
		ts.access[3].slot.descriptor == "Ljava/lang/Object;"
	}

	def "明示的な bind で継承元まで型引数が bind されてること"() {
		when:
		def ts = TypeSlotBuilder.build(TypeSlotTester.GenericExtends)
				.bind([
					Slot.getSlot(null, "Ljava/lang/Double;"),
					Slot.getSlot(null, "[[I")
				])
		def access = ts.accessors().toArray()

		then:
		access.size() == 4

		then:
		access[1].slot.typeParam == "="
		access[1].slot.getTypeDescriptor() == "[[I"

		then:
		access[2].slot.typeParam == "="
		access[2].slot.descriptor == "Ljava/lang/String;"
		
		then:
		access[3].slot.typeParam == "="
		access[3].slot.descriptor == "Ljava/lang/Double;"
	}
	
	
	def "ctor の明示的な bind"() {
		setup:
		def clazz = TypeSlotTester.GenericCtor
		// コンストラクタは必ず自身が持つので継承元とかの考慮はいらない
		// なので明示的な bind だけ考えればいいよ
		when:
		def ts = TypeSlotBuilder.build(clazz)
				.bind([
					Slot.getSlot(null, "Ljava/lang/String;"),
					Slot.getSlot(null, "[I")
				])
		def access = ts.accessors().toArray()

		then:
		access.size() == 1
		access[0] in MultiSlotAccessor
		
		when:
		def acc = access[0] as MultiSlotAccessor
		
		then:
		acc.descriptor == '(Ljava/lang/Object;Ljava/lang/Object;)V'
		acc.name == '<init>'
		acc.rel == '<init>'
		acc.owner == Type.getInternalName(clazz)
		acc.names == ['first', 'second']
		acc.slots.collect {it.getTypeDescriptor()} == ['[I', 'Ljava/lang/String;']
	}
}
