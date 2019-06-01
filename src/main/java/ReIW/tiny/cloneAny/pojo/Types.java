package ReIW.tiny.cloneAny.pojo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class Types {

	// TODO ConcurrentHashMap と Reference つかってなんとかできないもんか
	private static final Map<String, TypeDef> hive = Collections.synchronizedMap(new WeakHashMap<>());

	private Types() {
	}

	static List<TypeDef> getTypeDefChain(final String className) {
		ArrayList<TypeDef> list = new ArrayList<>();
		TypeDef t = hive.computeIfAbsent(className, TypeDefBuilder::createTypeDef);
		list.add(t);
		while (t.superName != null) {
			t = hive.computeIfAbsent(t.superName, TypeDefBuilder::createTypeDef);
			list.add(t);
		}
		return list;
	}

}
