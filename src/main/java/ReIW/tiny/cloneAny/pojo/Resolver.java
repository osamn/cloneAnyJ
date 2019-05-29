package ReIW.tiny.cloneAny.pojo;

import java.util.ArrayList;
import java.util.List;

public class Resolver {

	private final TypeDef[] typeDefs; // 親から子にならんだ TypeDef 配列
	private final ArrayList<String> boundList = new ArrayList<>(5);

	public final ArrayList<AccessEntry> accessors = new ArrayList<>();

	Resolver(TypeDef[] typeDefs) {
		this.typeDefs = typeDefs;
	}
	
	public void bind(final String typeClass) {
		boundList.add(typeClass);
	}
	
	public void resolve() {
		// setup accessors
	}
}
