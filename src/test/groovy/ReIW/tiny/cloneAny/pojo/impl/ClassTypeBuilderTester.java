package ReIW.tiny.cloneAny.pojo.impl;

import java.util.List;
import java.util.Map;

interface ClassTypeBuilderTester {

	abstract class FromList implements List<String> {
	}

	abstract class FromMap<X> implements Map<X, String> {
	}

}
