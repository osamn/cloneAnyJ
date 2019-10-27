package ReIW.tiny.cloneAny.pojo.impl

import ReIW.tiny.cloneAny.pojo.Accessor
import ReIW.tiny.cloneAny.pojo.Accessor.*
import ReIW.tiny.cloneAny.pojo.impl.ClassTypeBuilderTester.CtorAccTester
import ReIW.tiny.cloneAny.pojo.impl.ClassTypeBuilderTester.FieldAccTester
import ReIW.tiny.cloneAny.pojo.impl.ClassTypeBuilderTester.FromList
import ReIW.tiny.cloneAny.pojo.impl.ClassTypeBuilderTester.FromMap
import ReIW.tiny.cloneAny.pojo.impl.ClassTypeBuilderTester.ImplMany
import ReIW.tiny.cloneAny.pojo.impl.ClassTypeBuilderTester.PropAccTester
import ReIW.tiny.cloneAny.pojo.impl.ClassTypeBuilderTester.SimpleTester
import spock.lang.Specification

class ClassTypeBuilderSpec extends Specification {

	def "配列の descrirptor からつくる"() {
		// ここではクラスはなくてもいいので signature だけで
		when:
		def ct = new ClassTypeBuilder().buildClassType('[Ljava/lang/String;')

		then:
		ct.thisSlot.descriptor == '[Ljava/lang/String;'

		then:
		// 配列に普通のコンストラクタはないので indexed のみ
		ct.accessors.size() == 1
		ct.superSlot == null

		then:
		ct.accessors[0].getType() == Accessor.AccessType.ArrayType
		ct.accessors[0].getName() == '@indexed'
		ct.accessors[0].canRead() == true
		ct.accessors[0].canWrite() == true

		when:
		def indexed = ct.accessors[0] as IndexedAccess

		then:
		// 配列の要素
		indexed.elementSlot.descriptor == 'Ljava/lang/String;'

	}

	def "明示的なコンストラクタがない場合、デフォルトコンストラクタがアクセサに追加されること"() {
		when:
		def ct = new ClassTypeBuilder().buildClassType(SimpleTester)

		then:
		ct.superSlot.descriptor == 'Ljava/lang/Object;'

		then:
		LumpSetAccess.isCase(ct.accessors[0])

		then:
		(ct.accessors[0] as LumpSetAccess) .methodDescriptor == '()V'
	}

	def "List を実装するクラス"() {
		when:
		def ct = new ClassTypeBuilder().buildClassType(FromList)

		then:
		ct.superSlot.descriptor == 'Ljava/lang/Object;'

		then:
		ct.accessors[0].getType() == Accessor.AccessType.ListType
		ct.accessors[0].getName() == '@indexed'
		ct.accessors[0].canRead() == true
		ct.accessors[0].canWrite() == true

		when:
		def indexed = ct.accessors[0] as IndexedAccess

		then:
		indexed.elementSlot.descriptor == 'Ljava/lang/String;'
	}

	def "Map を実装するクラス"() {
		when:
		def ct = new ClassTypeBuilder().buildClassType(FromMap)

		then:
		ct.superSlot.descriptor == 'Ljava/lang/Object;'

		then:
		ct.accessors[0].getType() == Accessor.AccessType.MapType
		ct.accessors[0].getName() == '@keyed'
		ct.accessors[0].canRead() == true
		ct.accessors[0].canWrite() == true

		when:
		def keyed = ct.accessors[0] as KeyedAccess

		then:
		keyed.keySlot.descriptor == 'Ljava/lang/String;'
		keyed.valueSlot.descriptor == 'Ljava/lang/Integer;'
	}

	def "public なコンストラクタのみアクセサに追加される+パラメタのマップが正しいこと"() {
		when:
		def ct = new ClassTypeBuilder().buildClassType(CtorAccTester)

		then:
		ct.accessors.size() == 1

		then:
		ct.accessors[0].type == Accessor.AccessType.LumpSet
		ct.accessors[0].name == '<init>'
		ct.accessors[0].canRead() == false
		ct.accessors[0].canWrite() == true

		then:
		def ctor = ct.accessors[0] as LumpSetAccess
		ctor.methodDescriptor == '(Ljava/lang/String;[Ljava/lang/Integer;I)V'

		def expected = ['str':'Ljava/lang/String;', 'intArr':'[Ljava/lang/Integer;', 'i':'I' ]
		ctor.parameters.collectEntries { key, val -> [key, val.descriptor]} == expected
	}

	def "public + non static フィールドのみアクセサに追加されること"() {
		when:
		def ct = new ClassTypeBuilder().buildClassType(FieldAccTester)

		then:
		ct.accessors.size() == 3

		when:
		def fields = ct.accessors.findAll { FieldAccess.isCase(it) }.collect { it as FieldAccess }

		then:
		fields.size() == 2

		then:
		fields[0].type == AccessType.ReadonlyField
		fields[0].name == 'publicReadonlyStr'
		fields[0].canRead() == true
		fields[0].canWrite() == false
		fields[0].slot.descriptor == 'Ljava/lang/String;'

		then:
		fields[1].type == AccessType.Field
		fields[1].name == 'publicStr'
		fields[1].canRead() == true
		fields[1].canWrite() == true
		fields[1].slot.descriptor == 'Ljava/lang/String;'
	}

	def "public + non static なプロパティのみアクセサに追加されること"() {
		when:
		def ct = new ClassTypeBuilder().buildClassType(PropAccTester)

		then:
		ct.accessors.size == 3

		when:
		def props = ct.accessors.findAll { PropAccess.isCase(it) }.collect { it as PropAccess }

		then:
		props.size() == 2

		then:
		props[0].type == AccessType.Get
		props[0].name == 'publicStr'
		props[0].canRead() == true
		props[0].canWrite() == false
		props[0].slot.descriptor == 'Ljava/lang/String;'

		then:
		props[1].type == AccessType.Set
		props[1].name == 'publicStr'
		props[1].canRead() == false
		props[1].canWrite() == true
		props[1].slot.descriptor == 'Ljava/lang/String;'
	}

	def "継承クラス・実装インターフェースがリストアップされてること"() {
		when:
		def ct = new ClassTypeBuilder().buildClassType(ImplMany)
		def expected = ['Ljava/lang/Object;', 'Ljava/io/Serializable;', 'Ljava/lang/Cloneable;']

		then:
		ct.ancestors.size() == expected.size()
		ct.ancestors.containsAll(expected)
	}
}
