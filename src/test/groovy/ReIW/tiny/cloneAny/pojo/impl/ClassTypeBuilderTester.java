package ReIW.tiny.cloneAny.pojo.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
interface ClassTypeBuilderTester {

	enum MyEnum {
		A, B, C,
	}

	class SimpleTester {
	}

	abstract class FromList implements List<String> {
	}

	abstract class FromSet implements Set<Integer> {
	}

	abstract class FromMap implements Map<String, Integer> {
	}

	class CtorAccTester {
		public CtorAccTester(String str, Integer[] intArr, int i) {
		}

		protected CtorAccTester(boolean[] bb) {
		}

		private CtorAccTester(char[] ii) {
		}

		CtorAccTester(int[] ii) {
		}
	}

	class FieldAccTester {
		private final String privateReadonlyStr = null;
		final String packageReadonlyStr = null;
		protected final String protectedReadonlyStr = null;
		public final String publicReadonlyStr = null;
		public static final String staticReadonlyStr = null;

		private String privateStr;
		String packageStr;
		protected String protectedStr;
		public String publicStr;
		public static String staticStr;
	}

	abstract class PropAccTester {
		private String getPrivateStr() {
			return null;
		}

		String getPackageStr() {
			return null;
		}

		protected String getProtectedStr() {
			return null;
		}

		public String getPublicStr() {
			return null;
		}

		public static String getStaticStr() {
			return null;
		}

		public abstract String getAbstractStr();

		private void setPrivateStr(String str) {
		}

		void setPackageStr(String str) {
		}

		protected void setProtectedStr(String str) {
		}

		public void setPublicStr(String str) {
		}

		public static void setStaticStr(String str) {
		}

		public abstract void setAbstractStr(String str);
	}

	@SuppressWarnings("serial")
	class ImplMany implements Serializable, Cloneable {

	}
}
