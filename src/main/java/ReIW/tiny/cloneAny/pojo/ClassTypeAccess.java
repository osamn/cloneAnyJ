package ReIW.tiny.cloneAny.pojo;

import java.util.stream.Stream;

public interface ClassTypeAccess {

	String getInternalName();

	Stream<Accessor> accessors();

}
