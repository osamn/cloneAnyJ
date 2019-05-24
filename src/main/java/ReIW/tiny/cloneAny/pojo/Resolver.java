package ReIW.tiny.cloneAny.pojo;

import java.util.ArrayList;
import java.util.List;

public class Resolver {

	private final TypeDef[] defs; // 親から子にならんだ TypeDef 配列
	private final ArrayList<String> boundList = new ArrayList<>(5);

	public final ArrayList<PartialEntry> accessors = new ArrayList<>();

	Resolver(List<TypeDef> list) {
		defs = list.toArray(size -> new TypeDef[size]);
	}
	
	public void bind(final String className) {
		boundList.add(className);
	}
	
	public void resolve() {
		// setup accessors
	}
}
