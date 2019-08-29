package ReIW.tiny.cloneAny.pojo

import static org.junit.Assert.*

import org.junit.Test

import spock.lang.Specification
import spock.lang.Unroll

class SlotSpec extends Specification {

	//@Unroll
	def "子スロットがないときの rebind"() {
		setup:
		def bindMap = ['AA':'Ljava/lang/Long;']
		def slot = new Slot(typeParam, 'any')
		def actual = slot.rebind(bindMap);
		
		expect:
		// rebind の必要ない場合は自身をかえすはず
		slot.is(actual) == is_me
		// rebind されたか確認
		actual.typeParam == actualTypeParam
		actual.descriptor == actualTypeClass
		
		where:
		typeParam || is_me | actualTypeParam | actualTypeClass
		null      || true  | null            | 'any'
		'='       || true  | '='             | 'any'
		'-'       || true  | '-'             | 'any'
		'+'       || true  | '+'             | 'any'
		'AA'      || false | '='             | 'Ljava/lang/Long;'
		'BB'      || false | 'BB'            | 'any'
	}


	def "子スロットの rebind"() {
		setup:
		def bindMap = ['AA':'foo/bar/Hoge', 'BB':'TX']
		def slot = new Slot('=', 'any')
		slot.slotList.add(new Slot(typeParam, typeClass))

		def actual = slot.rebind(bindMap);
		
		expect:
		actual.slotList[0].typeParam == actualTypeParam
		actual.slotList[0].descriptor == actualTypeClass
		
		where:
		typeParam | typeClass | actualTypeParam | actualTypeClass
		'N/A'     | 'any'     | 'N/A'           | 'any'
		'BB'      | 'any'     | 'X'             | 'any'
		'AA'      | null      | '='             | 'foo/bar/Hoge'
		
	}
}
