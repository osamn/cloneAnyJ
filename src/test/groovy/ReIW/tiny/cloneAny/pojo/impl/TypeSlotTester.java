package ReIW.tiny.cloneAny.pojo.impl;

interface TypeSlotTester {

	class SimpleBase {

		public final String superField;

		public SimpleBase(String superCtorArg) {
			this.superField = superCtorArg;
		}

		// このこは descriptor が違うから抽出される
		public void setSuperVal(Long val) {
		}

		// 継承先のものが優先されてこのこは抽出されない
		public double getThisVal() {
			return 0;
		}

	}

	class Simple extends SimpleBase {

		public String thisField;

		public double getThisVal() {
			return 0;
		}

		public Simple(String thisCtorArg) {
			super(thisCtorArg);
		}

		public void setSuperVal(String val) {
		}

	}

	class GenericBase<X, Y, Z> {
		public Z baseVal;
		public void setBaseVal(X val) {}
	}

	class GenericExtends<A, B> extends GenericBase<A, B, String> {
		public B getExtendsVal() {
			return null;
		}
	}
	
	class GenericCtor<X, Y> {
		public GenericCtor(Y first, X second) {
			
		}
	}
}
