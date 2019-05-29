package ReIW.tiny.cloneAny.pojo

import spock.lang.Specification

class TypesSpec extends Specification {

	def "createTypeSlot 型パラメータなし"() {
		given:
		def typed1="<V1:Ljava/lang/Object;ML1::Ljava/util/Map<TT1;Ljava/util/List<TV1;>;>;S1:Ljava/util/HashSet<-Ljava/util/ArrayList<TT1;>;>;T1:Ljava/lang/Object;>Ljava/lang/Object;"

		when:
		def ts1 = Types.createTypeSlot(typed1, "")()

		then:
		println ts1
	}
	def "createTypeSlot 型パラメータあり"() {

	}

	def "createTypeSlot ネストした型パラメータあり"() {

	}

	def "createTypeSlot 型パラメータなしの継承"() {
	}

	def "createTypeSlot 継承元に型パラメータありですべてバインド済み"() {
	}

	def "createTypeSlot 継承元に型パラメータあり＋未バインドあり"() {

	}
}
