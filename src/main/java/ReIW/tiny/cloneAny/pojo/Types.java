package ReIW.tiny.cloneAny.pojo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class Types {

	public static final int ACC_FIELD = 1;
	public static final int ACC_FINAL_FIELD = 2;
	public static final int ACC_CTOR_ARG = 3;
	public static final int ACC_PROP_GET = 4;
	public static final int ACC_PROP_SET = 5;

	// TODO ConcurrentHashMap と Reference つかってなんとかできないもんか
	private static final Map<String, TypeDef> hive = Collections.synchronizedMap(new WeakHashMap<>());

	public static List<TypeDef> getTypeDefChain(final String className) {
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
