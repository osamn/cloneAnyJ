package ReIW.tiny.cloneAny.pojo

import spock.lang.Specification

class TypeDefBuilderSpec extends Specification {

	def "PlainCtor public なコンストラクタの引数のみ access にはいる"() {
		when:
		def actual = new TypeDefBuilder().getTypeDef(TypeDefBuilder_PlainCtor.class.getName())
		
		then:
		actual.name == "ReIW/tiny/cloneAny/pojo/PlainCtor"
		actual.superType == null
		actual.typeSlot == null;

		actual.access.size()== 3

		actual.access[0].elementType == AccessEntry.ACE_CTOR_ARG
		actual.access[0].name == "ii"
		actual.access[0].slot.typeParam == null
		actual.access[0].slot.typeClass == "java/lang/Integer"
		actual.access[0].canSet == true
		actual.access[0].canGet == false
		actual.access[0].rel == "(Ljava/lang/Integer;Ljava/lang/String;)V"

		actual.access[1].elementType == AccessEntry.ACE_CTOR_ARG
		actual.access[1].name == "ss"
		actual.access[1].slot.typeParam == null
		actual.access[1].slot.typeClass == "java/lang/String"
		actual.access[1].canSet == true
		actual.access[1].canGet == false
		actual.access[1].rel == "(Ljava/lang/Integer;Ljava/lang/String;)V"

		actual.access[2].elementType == AccessEntry.ACE_CTOR_ARG
		actual.access[2].name == "l"
		actual.access[2].slot.typeParam == null
		actual.access[2].slot.typeClass == "J"
		actual.access[2].canSet == true
		actual.access[2].canGet == false
		actual.access[2].rel == "(J)V"

	}

	def "TypedCtor<T> 型引数がちゃんとできてるかみてみる"() {
		when:
		def actual = new TypeDefBuilder().getTypeDef(TypeDefBuilder_TypedCtor.class.getName())
		
		then:
		actual.name == "ReIW/tiny/cloneAny/pojo/TypedCtor"
		actual.superType == null
		actual.typeSlot.formalSlots[0].typeParam == "T";
		actual.typeSlot.formalSlots[0].typeClass == "java/lang/Object";

		actual.access.size()== 2

		actual.access[0].elementType == AccessEntry.ACE_CTOR_ARG
		actual.access[0].name == "t1"
		actual.access[0].slot.typeParam == "T"
		actual.access[0].slot.typeClass == null
		actual.access[0].canSet == true
		actual.access[0].canGet == false
		actual.access[0].rel == "(Ljava/lang/Object;Ljava/lang/String;)V"

		actual.access[1].elementType == AccessEntry.ACE_CTOR_ARG
		actual.access[1].name == "str1"
		actual.access[1].slot.typeParam == null
		actual.access[1].slot.typeClass == "java/lang/String"
		actual.access[1].canSet == true
		actual.access[1].canGet == false
		actual.access[1].rel == "(Ljava/lang/Object;Ljava/lang/String;)V"

	}

	def "Fields<T,K> public なフィールドだけ access 追加される"() {
		when:
		def actual = new TypeDefBuilder().getTypeDef(TypeDefBuilder_Fields.class.getName())
		
		then:
		actual.name == "ReIW/tiny/cloneAny/pojo/Fields"
		actual.typeSlot.formalSlots[0].typeParam == "T";
		actual.typeSlot.formalSlots[0].typeClass == "java/lang/Object";
		actual.typeSlot.formalSlots[1].typeParam == "K";
		actual.typeSlot.formalSlots[1].typeClass == "java/lang/Object";

		actual.access.size()== 4

		actual.access[0].elementType == AccessEntry.ACE_FIELD;
		actual.access[0].name == "t1"
		actual.access[0].slot.typeParam == "T"
		actual.access[0].slot.typeClass == null
		actual.access[0].canSet == true
		actual.access[0].canGet == true
		actual.access[0].rel == null

		actual.access[1].elementType == AccessEntry.ACE_FINAL_FIELD;
		actual.access[1].name == "k1"
		actual.access[1].slot.typeParam == "K"
		actual.access[1].slot.typeClass == null
		actual.access[1].canSet == false
		actual.access[1].canGet == true
		actual.access[1].rel == null

		actual.access[2].elementType == AccessEntry.ACE_FIELD;
		actual.access[2].name == "ii"
		actual.access[2].slot.typeParam == null
		actual.access[2].slot.typeClass == "I"
		actual.access[2].canSet == true
		actual.access[2].canGet == true
		actual.access[2].rel == null

		actual.access[3].elementType == AccessEntry.ACE_FINAL_FIELD;
		actual.access[3].name == "ss"
		actual.access[3].slot.typeParam == null
		actual.access[3].slot.typeClass == "java/lang/String"
		actual.access[3].canSet == false
		actual.access[3].canGet == true
		actual.access[3].rel == null

	}

	def "Props<T> public な getter setter が access に追加される"() {
		when:
		def actual = new TypeDefBuilder().getTypeDef(TypeDefBuilder_Props.class.getName())
		
		then:
		actual.name == "ReIW/tiny/cloneAny/pojo/Props"
		actual.superType == null
		actual.typeSlot.formalSlots[0].typeParam == "T";
		actual.typeSlot.formalSlots[0].typeClass == "java/lang/Object";
		actual.typeSlot.formalSlots[1].typeParam == "K";
		actual.typeSlot.formalSlots[1].typeClass == "java/lang/Object";

		actual.access.size()== 4

		actual.access[0].elementType == AccessEntry.ACE_PROP_SET
		actual.access[0].name == "foo"
		actual.access[0].slot.typeParam == "T"
		actual.access[0].slot.typeClass == null
		actual.access[0].canSet == true
		actual.access[0].canGet == false
		actual.access[0].rel == "setFoo"

		actual.access[1].elementType == AccessEntry.ACE_PROP_GET
		actual.access[1].name == "bar"
		actual.access[1].slot.typeParam == "K"
		actual.access[1].slot.typeClass == null
		actual.access[1].canSet == false
		actual.access[1].canGet == true
		actual.access[1].rel == "getBar"

		actual.access[2].elementType == AccessEntry.ACE_PROP_SET
		actual.access[2].name == "int"
		actual.access[2].slot.typeParam == null
		actual.access[2].slot.typeClass == "I"
		actual.access[2].canSet == true
		actual.access[2].canGet == false
		actual.access[2].rel == "setInt"

		actual.access[3].elementType == AccessEntry.ACE_PROP_GET
		actual.access[3].name == "long"
		actual.access[3].slot.typeParam == null
		actual.access[3].slot.typeClass == "J"
		actual.access[3].canSet == false
		actual.access[3].canGet == true
		actual.access[3].rel == "getLong"

	}
	
	def "public じゃない class は例外"() {
		when:
		new TypeDefBuilder().getTypeDef(TypeDefBuilder_InternalScope.class.getName())
		
		then:
		thrown(UnsupportedOperationException)
	}
}
