package ReIW.tiny.cloneAny.pojo.impl;

interface ClassSignatureParserTester {

	/*
	 * 基本ここで使うのは extends される側のクラスだけなので、実際に extends したクラスは必要ないよ
	 * 
	 * でも実際の signature を確認するのに一応定義してるよ
	 */

	class Simple {
	}

	class Generic<X> {
	}

	// signature 確認用
	class Bound extends Generic<String> {
	}

	interface Foo {
	}

	interface Bar {
	}

	interface GenericIntf<X> {
	}

	// signature 確認用
	class BoundIntf implements GenericIntf<String[]> {
	}
	
	class Unbound<K, V> {
	}
	
	class Partial<A> extends Unbound<A, int[]> {
	}

}
