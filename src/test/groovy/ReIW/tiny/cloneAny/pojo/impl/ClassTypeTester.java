package ReIW.tiny.cloneAny.pojo.impl;

import java.util.HashMap;

@SuppressWarnings("serial")
interface ClassTypeTester {

	class Base<X, Y, Z> extends HashMap<X, Y> {
		public X field_X;

		public void setPropY(Y val) {
		};
	}

	class Partial<A, B> extends Base<String, A, B> {
		public final A field_Y_A = null;

		public Partial() {
		}

		public Partial(A a, B b) {
		}

		public void setPropB(B val) {
		};
	}

	class Terminal extends Partial<Integer, Long> {

		public void setPropB(Boolean b) {
		}

		public void setPropY(Integer ii) {
		}

	}

}
