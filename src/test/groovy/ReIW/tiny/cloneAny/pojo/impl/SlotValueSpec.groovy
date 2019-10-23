package ReIW.tiny.cloneAny.pojo.impl

import spock.lang.Specification

class SlotValueSpec extends Specification{

	//@spock.lang.Unroll
	def "isCertainBound 型パラメタがすべて解決されてるか"() {
		expect:
		def actual = new SlotValueBuilder(null).build(signature)
		actual.isCertainBound() == bound

		where:
		signature                                | bound
		'Ljava/lang/String;'                     | true
		'[Ljava/lang/String;'                    | true
		'Ljava/util/List<[J>;'                   | true
		'Ljava/util/Map<Ljava/lang/String;[C>;'  | true
		'Ljava/util/Map<Ljava/lang/String;TX;>;' | false
	}

	//@spock.lang.Unroll
	def "rebind 型パラメタのバインド"() {
		setup:
		// Map<X, Map<Y, List<Z>[]>>[]
		def slot = new SlotValueBuilder(null).build('[Ljava/util/Map<TX;Ljava/util/Map<TY;[Ljava/util/List<TZ;>;>;>;')
		// スロットの階層はこんな感じ
		// [
		//   + Ljava/util/Map;
		//     + X
		//     + Ljava/util/Map;
		//       + Y
		//       + [
		//         + Ljava/util/List;
		//           + Z

		expect:
		def actual = slot.rebind(bindMap)
		def slot_x = actual.slotList[0].slotList[0]
		def slot_y = actual.slotList[0].slotList[1].slotList[0]
		def slot_z = actual.slotList[0].slotList[1].slotList[1].slotList[0].slotList[0]
		slot_x.typeParam == param_x
		slot_y.typeParam == param_y
		slot_z.typeParam == param_z
		slot_x.descriptor == desc_x
		slot_y.descriptor == desc_y
		slot_z.descriptor == desc_z

		where:
		bindMap         || param_x | desc_x                | param_y  | desc_y               | param_z  | desc_z
		['@':'@']/*nop*/|| 'X'     | 'Ljava/lang/Object;'  | 'Y'      | 'Ljava/lang/Object;' | 'Z'      | 'Ljava/lang/Object;'
		['Y':'TA']      || 'X'     | 'Ljava/lang/Object;'  | 'A'      | 'Ljava/lang/Object;' | 'Z'      | 'Ljava/lang/Object;'
		[
			'Z':'[Ljava/lang/String;'
		]               || 'X'     | 'Ljava/lang/Object;'  | 'Y'      | 'Ljava/lang/Object;' | '='      | '[Ljava/lang/String;'
		[
			'X':'Ljava/lang/Integer;',
			'Y':'Ljava/lang/Long;',
			'Z':'THoge'
		]               || '='     | 'Ljava/lang/Integer;' | '='      | 'Ljava/lang/Long;'   | 'Hoge'   | 'Ljava/lang/Object;'
		[
			'Z':'Ljava/util/List<Ljava/lang/String;>;'
		]               || 'X'     | 'Ljava/lang/Object;'  | 'Y'      | 'Ljava/lang/Object;' | '='      | 'Ljava/util/List;'

	}
	
	def "シグネチャ"() {
		// TODO スロットのシグネチャのテスト
		// + とか - とかどうやって作るんやろ	
		when:
		def slot = new SlotValueBuilder(null).build("Ljava/util/List<*>;")
		
		then:
		println slot
	}
}
