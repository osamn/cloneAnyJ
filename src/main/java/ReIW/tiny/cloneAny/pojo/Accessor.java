package ReIW.tiny.cloneAny.pojo;

public interface Accessor {

	enum Type {
		Field, ReadonlyField, Get, Set, LumpSet,
	}

	Type getType();

	boolean canRead();

	boolean canWrite();

	String getOwner();

	String getName(); // field 名または method 名なので注意！プロパティ名じゃないよ

	String getDescriptor();

}
