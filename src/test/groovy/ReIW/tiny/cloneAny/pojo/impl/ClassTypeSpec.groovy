package ReIW.tiny.cloneAny.pojo.impl

import org.objectweb.asm.Type

import ReIW.tiny.cloneAny.pojo.Accessor.AccessType
import ReIW.tiny.cloneAny.pojo.Accessor.FieldAccess
import ReIW.tiny.cloneAny.pojo.Accessor.KeyedAccess
import ReIW.tiny.cloneAny.pojo.Accessor.LumpSetAccess
import ReIW.tiny.cloneAny.pojo.Accessor.PropAccess
import ReIW.tiny.cloneAny.pojo.impl.ClassTypeTester.Base
import ReIW.tiny.cloneAny.pojo.impl.ClassTypeTester.Partial
import ReIW.tiny.cloneAny.pojo.impl.ClassTypeTester.Terminal
import spock.lang.Specification

class ClassTypeSpec extends Specification {

	def "継承階層のクラス、インターフェースがすべて抽出されること"() {
		when:
		def ct = new ClassTypeBuilder().buildClassType(Terminal)
		def expected = [
			Type.getDescriptor(Base),
			Type.getDescriptor(Partial),
			'Ljava/util/HashMap;',
			'Ljava/util/AbstractMap;',
			'Ljava/util/Map;',
			'Ljava/lang/Cloneable;',
			'Ljava/io/Serializable;',
			'Ljava/lang/Object;'
		]

		then:
		ct.ancestors.size() == expected.size()
		ct.ancestors.containsAll(expected)
	}

	def "継承階層のコンストラクタが抽出されないこと"() {
		when:
		def ct = new ClassTypeBuilder().buildClassType(Terminal)
		def lump  = ct.accessors.findAll { it instanceof LumpSetAccess }.collect {it.owner}

		then:
		lump == [Type.getDescriptor(Terminal)]
	}

	def "継承階層上のリストのアクセサが抽出されること"() {
		// TODO まあマップと同じだからあとで
	}

	def "継承階層上のマップのアクセサが抽出されること"() {
		when:
		def ct = new ClassTypeBuilder().buildClassType(Terminal)
		def keyed = ct.accessors.findAll { it instanceof KeyedAccess }.collect {(KeyedAccess)it}

		then:
		keyed.size() == 1

		then:
		keyed[0].owner == 'Ljava/util/HashMap;'
		keyed[0].keySlot.descriptor == 'Ljava/lang/String;'
		keyed[0].valueSlot.descriptor == 'Ljava/lang/Integer;'
	}

	def "フィールドアクセスが正しく継承されること"() {
		when:
		def ct = new ClassTypeBuilder().buildClassType(Terminal)
		def field = ct.accessors.findAll { it instanceof FieldAccess }.collect {(FieldAccess)it}

		then:
		field.size()== 2

		then:
		field[0].type == AccessType.ReadonlyField
		field[0].owner == Type.getDescriptor(Partial)
		field[0].name == 'field_Y_A'
		field[0].slot.descriptor == 'Ljava/lang/Integer;'

		then:
		field[1].type == AccessType.Field
		field[1].owner == Type.getDescriptor(Base)
		field[1].name == 'field_X'
		field[1].slot.descriptor == 'Ljava/lang/String;'
	}

	def "プロパティアクセスが正しく継承されること"() {
		when:
		def ct = new ClassTypeBuilder().buildClassType(Terminal)
		def prop  = ct.accessors.findAll { it instanceof PropAccess }.collect {(PropAccess)it}

		then:
		prop.size()== 4

		then:
		prop[0].type == AccessType.Set
		prop[0].owner == Type.getDescriptor(Terminal)
		prop[0].name == 'propB'
		prop[0].slot.descriptor == 'Ljava/lang/Boolean;'

		then:
		prop[1].type == AccessType.Set
		prop[1].owner == Type.getDescriptor(Terminal)
		prop[1].name == 'propY'
		prop[1].slot.descriptor == 'Ljava/lang/Integer;'

		then:
		prop[2].type == AccessType.Set
		prop[2].owner == Type.getDescriptor(Partial)
		prop[2].name == 'propB'
		prop[2].slot.descriptor == 'Ljava/lang/Long;'

		// Base#setPropY はバインドした結果で Terminal#setPropY と同じものになるので抽出されない

		then:
		prop[3].type == AccessType.Get
		prop[3].owner == 'Ljava/util/HashMap;'
		prop[3].name == 'empty'
		prop[3].slot.descriptor == 'Z'

	}
}
