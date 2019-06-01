package ReIW.tiny.cloneAny.pojo;

public class Props<T, K> {

	public void setFoo(T t) {
	}

	public K getBar() {
		return null;
	}

	public void setInt(int i) {
	}

	public long getLong() {
		return 0L;
	}

	// 型引数がついてるので対象外
	public <X> void setPiyo(X x) {
	}

	// static は対象外
	public static String getHoge() {
		return null;
	}

	// public 以外は対象外
	void setHoge(String s) {
	}

}
