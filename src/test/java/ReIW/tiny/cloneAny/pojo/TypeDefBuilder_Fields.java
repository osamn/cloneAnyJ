package ReIW.tiny.cloneAny.pojo;

public class TypeDefBuilder_Fields<T, K> {

	public T t1;
	public final K k1 = null;
	public int ii;
	public final String ss = null;

	// static は除外
	public static Object obj;
	
	// public 以外は除外
	@SuppressWarnings("unused")
	private T t2;
	protected final K k2 = null;
	String str;

}
