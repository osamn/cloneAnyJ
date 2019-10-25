package ReIW.tiny.cloneAny.pojo.impl

import spock.lang.Specification

class SlotValueSpec extends Specification{

	//@spock.lang.Unroll
	def "isCertainBound 型パラメタがすべて解決されてるか"() {
		expect:
		def actual = new SlotValueBuilder().build(signature)
		actual.isCertainBound() == bound

		where:
		signature                                | bound
		'Ljava/lang/String;'                     | true
		'[Ljava/lang/String;'                    | true
		'THoge;'                                 | false
		'Ljava/util/List<[J>;'                   | true
		'Ljava/util/Map<Ljava/lang/String;[C>;'  | true
		'Ljava/util/Map<Ljava/lang/String;TX;>;' | false
	}

	//@spock.lang.Unroll
	def "型パラメタのバインドで正しくスロットが更新されること"() {
		setup:
		// Map<X, Map<Y, List<Z>[]>>[]
		def slot = new SlotValueBuilder().build('[Ljava/util/Map<TX;Ljava/util/Map<TY;[Ljava/util/List<TZ;>;>;>;')
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
		def actual_slot_x = actual.slotList[0].slotList.collect {[it.wildcard, it.typeParam, it.descriptor]}[0]
		def actual_slot_y = actual.slotList[0].slotList[1]
				.slotList.collect {[it.wildcard, it.typeParam, it.descriptor]}[0]
		def actual_slot_z = actual.slotList[0].slotList[1]
				.slotList[1].slotList[0]
				.slotList.collect{[it.wildcard, it.typeParam, it.descriptor]}[0]

		actual_slot_x == slot_x
		actual_slot_y == slot_y
		actual_slot_z == slot_z

		where:
		bindMap         || slot_x                          | slot_y                          | slot_z
		[:]/*empty map*/|| ['=', 'X', 'Ljava/lang/Object;']| ['=', 'Y', 'Ljava/lang/Object;']| ['=', 'Z', 'Ljava/lang/Object;']
		['Y':'TA']      || ['=', 'X', 'Ljava/lang/Object;']| ['=', 'A', 'Ljava/lang/Object;']| ['=', 'Z', 'Ljava/lang/Object;']
		['Z':'[Ljava/lang/String;']\
		                || ['=', 'X', 'Ljava/lang/Object;']| ['=', 'Y', 'Ljava/lang/Object;']| ['=', null, '[Ljava/lang/String;']
		[
			'X':'Ljava/lang/Integer;',
			'Y':'Ljava/lang/Long;',
			'Z':'THoge']\
		                || ['=', null, 'Ljava/lang/Integer;']| ['=', null, 'Ljava/lang/Long;']| ['=', 'Hoge', 'Ljava/lang/Object;']

	}

	//@spock.lang.Unroll
	def "シグネチャ文字列の再構築で元に戻ること"() {
		expect:
		def slot = new SlotValueBuilder().build(signature)
		slot.signature == expected

		where:
		signature                                     | expected
		'Ljava/util/List<*>;'                         | 'Ljava/util/List<*>;' // List<?>
		'Ljava/util/List<+Ljava/lang/CharSequence;>;' | 'Ljava/util/List<+Ljava/lang/CharSequence;>;' // List<? extends CharSequence>
		'Ljava/util/List<-Ljava/lang/String;>;'       | 'Ljava/util/List<-Ljava/lang/String;>;' // List<? super String>
		'Ljava/util/List<Ljava/lang/String;>;'        | 'Ljava/util/List<Ljava/lang/String;>;'
	}
}
