package ReIW.tiny.cloneAny.pojo

import static org.junit.Assert.*

import org.junit.Test

import spock.lang.Specification

class TypeDefSpec extends Specification {

	def "bind で直接型パラメタ指定"() {
		setup:
		def concrete = TypeDefBuilder.createTypeDef(TypeDef_BoundProp.class.getName())
		def unbound = TypeDefBuilder.createTypeDef(TypeDef_Unbound.class.getName())
		def acclist = unbound.bind(concrete.access[0].slot.slotList).accessors().toList()
		def acc1 = acclist[0]
		def acc2 = acclist[1]
		
		expect:
		acc1.slot.typeClass == "java/lang/Integer"
		acc2.slot.typeClass == "java/lang/String"
		
	}
}
