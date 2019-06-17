package ReIW.tiny.cloneAny

import ReIW.tiny.cloneAny.pojo.TypeDefBuilder

public class FooBar {
	public static class Base<A, B, C, D, E> {
	}

	public static class Sample<b extends List<? super Integer>, d> extends Base<CharSequence, b, Integer, d, Long>
	implements Serializable {
	}
	public static class Piyo {}
	public static class Hoge extends Piyo{
	}
}

def typeDef = TypeDefBuilder.createTypeDef(FooBar.Sample.class.getName());
println typeDef.typeSlot