package ReIW.tiny.cloneAny

import static org.junit.jupiter.api.Assertions.*

import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import spock.lang.Specification
import spock.lang.Unroll

class AccessorUtilSpec extends Specification {

	def "get property name from method name"() {
		expect:
		expected == AccessorUtil.getPropertyName(methodName)

		where:
		methodName	|| expected
		"setFoo"	|| "foo"
		"setFooBar"	|| "fooBar"
		"setFOoBar"	|| "FOoBar"
		"set"		|| null
		"getFoo"	|| "foo"
		"getFooBar"	|| "fooBar"
		"getFOoBar"	|| "FOoBar"
		"get"		|| null
		"isFoo"		|| "foo"
		"isFooBar"	|| "fooBar"
		"isFOoBar"	|| "FOoBar"
		"is"		|| null
		"fooBar"	|| null
	}

	def "check method is getter or not"() {
		expect:
		expected == AccessorUtil.isGetter(name, descriptor)

		where:
		expected	|| name			| descriptor
		true		|| "getFoo"		| "()Ljava/lang/String;"
		true		|| "isFoo"		| "()Z"
		false		|| "isFoo"		| "()Ljava/lang/Boolean;"
		false		|| "getBarBuz"	| "(D)J"
		false		|| "isBarBuz"	| "(I)Z"
		false		|| "get"		| "()D"
		false		|| "is"			| "()Z"
		false		|| "fooBar"		| "()Ljava/lang/Object;"
	}

	def "check method is setter or not"() {
		expect:
		expected == AccessorUtil.isSetter(name, descriptor)

		where:
		expected	|| name			| descriptor
		true		|| "setFoo"		| "(Ljava/lang/String;)V"
		true		|| "setBarBuz"	| "(D)V"
		false		|| "set"		| "(Ljava/lang/String;)V" // プロパティ名が取れない
		false		|| "fooBar"		| "(Ljava/lang/String;)V" // setter じゃない
		false		|| "setHoge"	| "(Ljava/lang/Object;Z)V" // 引数が２つ
		false		|| "setFoo"		| "()V" // 引数が無し
		false		|| "setFoo"		| "()Ljava/lang/Integer;" // 戻り値が void じゃない
	}
}
