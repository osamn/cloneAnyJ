package ReIW.tiny.cloneAny.pojo_.impl

import org.objectweb.asm.Type

import ReIW.tiny.cloneAny.pojo_.Accessor
import ReIW.tiny.cloneAny.pojo_.Slot
import ReIW.tiny.cloneAny.pojo_.SlotTestHelper
import ReIW.tiny.cloneAny.pojo_.impl.MultiSlotAccessor
import ReIW.tiny.cloneAny.pojo_.impl.TypeSlotBuilder
import spock.lang.Specification

class TypeSlotSpec extends Specification implements SlotTestHelper {

	def "継承元のアクセスが抽出されること"() {
		when:
		def ts = new TypeSlotBuilder().buildTypeSlot(TypeSlotTester.Simple)
		def acc = ts.access.collect {
			[it.type, it.owner, it.name, it.rel, it.descriptor]
		}

		then:
		ts.access.size() == 6

		then:
		acc[0] == [Accessor.Kind.Field, Type.getInternalName(TypeSlotTester.Simple), "thisField", "thisField", "Ljava/lang/String;"]
		acc[1] == [Accessor.Kind.Get, Type.getInternalName(TypeSlotTester.Simple), "thisVal", "getThisVal", "()D"]
		acc[2] == [Accessor.Kind.LumpSet, Type.getInternalName(TypeSlotTester.Simple), "<init>", "<init>", "(Ljava/lang/String;)V"]
		acc[3] == [Accessor.Kind.Set, Type.getInternalName(TypeSlotTester.Simple), "superVal", "setSuperVal", "(Ljava/lang/String;)V"]
		acc[4] == [Accessor.Kind.ReadonlyField, Type.getInternalName(TypeSlotTester.SimpleBase), "superField", "superField", "Ljava/lang/String;"]
		acc[5] == [Accessor.Kind.Set, Type.getInternalName(TypeSlotTester.SimpleBase), "superVal", "setSuperVal", "(Ljava/lang/Long;)V"]

		then:
		ts.access[2].slotInfo().collect {[it.param]} == [["thisCtorArg"]]
	}

	def "暗黙の bind で継承元まで型引数が bind されてること"() {
		when:
		def ts = new TypeSlotBuilder().buildTypeSlot(TypeSlotTester.GenericExtends)
		def acc = ts.access.collect {
			[it.type, it.owner, it.name, it.rel, it.descriptor, // generic の場合 parse 時点の情報になるので Object になってる
			]
		}

		then:
		ts.access.size() == 4

		then:
		acc[0] == [Accessor.Kind.LumpSet, Type.getInternalName(TypeSlotTester.GenericExtends), "<init>", "<init>", "()V"]

		then:
		acc[1] == [Accessor.Kind.Get, Type.getInternalName(TypeSlotTester.GenericExtends), "extendsVal", "getExtendsVal", "()Ljava/lang/Object;"]
		ts.access[1].slot.typeParam == "B"
		ts.access[1].slot.descriptor == "Ljava/lang/Object;"

		then:
		acc[2] == [Accessor.Kind.Field, Type.getInternalName(TypeSlotTester.GenericBase), "baseVal", "baseVal", "Ljava/lang/Object;"]
		ts.access[2].slot.typeParam == "="
		ts.access[2].slot.descriptor == "Ljava/lang/String;"

		acc[3] == [Accessor.Kind.Set, Type.getInternalName(TypeSlotTester.GenericBase), "baseVal", "setBaseVal", "(Ljava/lang/Object;)V"]
		ts.access[3].slot.typeParam == "A"
		ts.access[3].slot.descriptor == "Ljava/lang/Object;"
	}

	def "明示的な bind で継承元まで型引数が bind されてること"() {
		when:
		def ts = new TypeSlotBuilder().buildTypeSlot(TypeSlotTester.GenericExtends)
				.bind([getSlot("Ljava/lang/Double;", null), getSlot("[[I", null)])
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
		def ts = new TypeSlotBuilder().buildTypeSlot(clazz)
				.bind([getSlot("Ljava/lang/String;", null), getSlot("[I", null)])
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

	def "implements List<String>"() {
		setup:
		def clazz = TypeSlotTester.ListOfString

		when:
		def ts = new TypeSlotBuilder().buildTypeSlot(clazz)

		then:
		ts.listSlot.descriptor == 'Ljava/util/List;'
		ts.listSlot.slotList[0].typeParam == '='
		ts.listSlot.slotList[0].descriptor == 'Ljava/lang/String;'
	}

	def "extends ArrayList<String>"() {
		setup:
		def clazz = TypeSlotTester.ArrayListOfString

		when:
		def ts = new TypeSlotBuilder().buildTypeSlot(clazz)

		then:
		ts.isList() == true
		ts.elementSlot().typeParam == '='
		ts.elementSlot().descriptor == 'Ljava/lang/String;'
	}

	def "extends ArrayList<T> bind with Integer"() {
		setup:
		def clazz = TypeSlotTester.ExArrayListOf

		when:
		def ts = new TypeSlotBuilder().buildTypeSlot(clazz).bind([new Slot("T", "Ljava/lang/Integer;")])
		def elm = ts.elementSlot()

		then:
		elm.descriptor == 'Ljava/lang/Integer;'
	}

	def "extends HashMap<String, Long>" () {
		setup:
		def clazz = TypeSlotTester.ExMapOfStringKeyd

		when:
		def ts = new TypeSlotBuilder().buildTypeSlot(clazz)

		then:
		ts.isMap() == true
		ts.valueSlot().typeParam == '='
		ts.valueSlot().descriptor == 'Ljava/lang/Long;'
	}

	def "extends HashMap<Integer, Long>" () {
		setup:
		def clazz = TypeSlotTester.ExMapOfIntegerKeyed

		when:
		def ts = new TypeSlotBuilder().buildTypeSlot(clazz)

		then:
		ts.isMap() == false
	}

	def "extends HashMap<K, Long> with bind String" () {
		setup:
		def clazz = TypeSlotTester.ExMapOf

		when:
		def ts = new TypeSlotBuilder().buildTypeSlot(clazz).bind([new Slot(null, "Ljava/lang/String;"), new Slot("Z", "Ljava/lang/Boolean;")])
		// いちおう型パラメタがなんでもいいことの確認もしとく

		then:
		ts.isMap() == true
		ts.valueSlot().typeParam == '='
		ts.valueSlot().descriptor == 'Ljava/lang/Long;'
	}

	def "extends HashMap<K, Long> with bind Character" () {
		setup:
		def clazz = TypeSlotTester.ExMapOf

		when:
		def ts = new TypeSlotBuilder().buildTypeSlot(clazz).bind([new Slot("K", "Ljava/lang/Character;"), new Slot("Z", "Ljava/lang/Boolean;")])

		then:
		ts.isMap() == false
	}

}
