package ReIW.tiny.cloneAny.pojo.impl;

public class TypeSlotBuilderTester {

	public static class Field {
		private Field() {
		}

		// static なので NG
		public static String staticField;

		// private scope は NG
		@SuppressWarnings("unused")
		private String privateField;

		// package scope は NG
		String packageField;

		// protected scope は NG
		protected String protectedField;

		// OK
		public String publicField;
	}

	public static class ReadonlyField {
		private ReadonlyField() {
		}

		// OK
		// primitive も確認しとく
		public final int publicField = 0;
	}

	public static abstract class Getter {
		private Getter() {
		}

		// abstract は NG
		public abstract String getAbstractPublic();

		// OK
		public Integer getPublic() {
			return null;
		}
	}

	public static class Setter {
		private Setter() {
		}

		// 同名のパラメタ型違い setter

		public void setFoo(String val) {
		}

		public void setFoo(Integer val) {
		}
	}

	public static class Ctor {

		public Ctor(String hoge) {
		}

		public Ctor(boolean foo, Integer bar) {
		}
	}

	public static class DefaultCtor {
	}

}
