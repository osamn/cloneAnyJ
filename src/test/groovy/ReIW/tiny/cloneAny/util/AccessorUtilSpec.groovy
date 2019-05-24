package ReIW.tiny.cloneAny.util

import spock.lang.Specification

class AccessorUtilSpec extends Specification {

	def "Property name from #methodName => #propName"() {
		expect:
		propName == AccessorUtil.getPropertyName(methodName)

		where:
		methodName	|| propName
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

	def "Method #name#descriptor is getter or not => #expected"() {
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

	def "Method #name#descriptor is setter or not => #expected"() {
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
