package ReIW.tiny.cloneAny.pojo;

import java.util.stream.Stream;

// TypeDef と BoundTypeDef をまとめるため分離しておいた
public interface TypeAccessDef {

	boolean hasDefaultCtor();

	String getInternalName();

	Stream<AccessEntry> accessors();

}