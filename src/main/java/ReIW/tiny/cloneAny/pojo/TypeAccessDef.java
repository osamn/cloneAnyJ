package ReIW.tiny.cloneAny.pojo;

import java.util.stream.Stream;

// TypeDef と BoundTypeDef をまとめるため分離しておいた
public interface TypeAccessDef {

	/** デフォルトコンストラクタがある場合 true */
	boolean hasDefaultCtor();

	/** internalName */
	String getName();
	
	Stream<AccessEntry> accessors();
	
	static TypeAccessDef createTypeDef(final Class<?> clazz) {
		return null;
	}


}