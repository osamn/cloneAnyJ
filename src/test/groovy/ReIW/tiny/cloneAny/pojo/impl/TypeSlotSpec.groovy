package ReIW.tiny.cloneAny.pojo.impl

import org.objectweb.asm.Type

import ReIW.tiny.cloneAny.pojo.Accessor
import spock.lang.Specification

class TypeSlotSpec extends Specification {

	def "継承元のアクセスが抽出されること"() {
		when:
		def ts = TypeSlotBuilder.createTypeSlot(TypeSlotTester.Simple)
		def acc = ts.access.collect {[it.type, it.owner, it.name, it.rel, it.descriptor]}
		
		then:
		ts.access.size() == 6
		
		then:
		acc[0] == [Accessor.Type.Field, Type.getInternalName(TypeSlotTester.Simple), "thisField", "thisField", "Ljava/lang/String;"]
		acc[1] == [Accessor.Type.Get, Type.getInternalName(TypeSlotTester.Simple), "thisVal", "getThisVal", "D"]
		acc[2] == [Accessor.Type.LumpSet, Type.getInternalName(TypeSlotTester.Simple), "<init>", "<init>", "(Ljava/lang/String;)V"]
		acc[3] == [Accessor.Type.Set, Type.getInternalName(TypeSlotTester.Simple), "superVal", "setSuperVal", "Ljava/lang/String;"]
		acc[4] == [Accessor.Type.ReadonlyField, Type.getInternalName(TypeSlotTester.SimpleBase), "superField","superField", "Ljava/lang/String;"]
		acc[5] == [Accessor.Type.Set, Type.getInternalName(TypeSlotTester.SimpleBase), "superVal", "setSuperVal", "Ljava/lang/Long;"]
		
		then:
		ts.access[2].names == ["thisCtorArg"]
	}

	def "暗黙の bind で継承元まで型引数が bind されてること"() {
		// TODO test
		
	}
	
	def "明示的な bind で継承元まで型引数が bind されてること"() {
		// TODO test
		
	}
}
