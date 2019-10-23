package ReIW.tiny.cloneAny.pojo.impl

import ReIW.tiny.cloneAny.pojo.Accessor
import ReIW.tiny.cloneAny.pojo.Accessor.IndexedAccess
import ReIW.tiny.cloneAny.pojo.impl.ClassTypeBuilderTester.FromList
import spock.lang.Specification

class ClassTypeBuilderSpec extends Specification {
	
	def "createBindMap"() {
		// TODO test
	}

	def "配列からつくる"() {
		// ここではクラスはなくてもいいので signature だけで
		when:
		def ct = new ClassTypeBuilder().buildClassType('[Ljava/lang/String;')

		then:
		ct.thisSlot.descriptor == '[Ljava/lang/String;'

		then:
		ct.accessors.size() == 2

		then:
		ct.accessors[0].getType() == Accessor.AccessType.ArrayGet
		ct.accessors[0].getOwner() == null
		ct.accessors[0].getName() == '@indexed'
		ct.accessors[0].canRead() == true
		ct.accessors[0].canWrite() == false
		// 配列の要素
		(ct.accessors[0] as IndexedAccess).elementSlot.descriptor == 'Ljava/lang/String;'
		
		then:
		ct.accessors[1].getType() == Accessor.AccessType.ArraySet
		ct.accessors[1].getOwner() == null
		ct.accessors[1].getName() == '@indexed'
		ct.accessors[1].canRead() == false
		ct.accessors[1].canWrite() == true
		// 配列の要素
		(ct.accessors[1] as IndexedAccess).elementSlot.descriptor == 'Ljava/lang/String;'
	}
	
	def "List を実装するクラス"() {
		when:
		def ct = new ClassTypeBuilder().buildClassType(FromList)
		
		then:
		println ct.superSlots
		ct.accessors.forEach{println it}
	}
	
}
