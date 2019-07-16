package ReIW.tiny.cloneAny.pojo;

import java.util.stream.Stream;

interface TypeAccessDef {

	boolean hasDefaultCtor();

	Stream<AccessEntry> accessors();

}