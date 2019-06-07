package ReIW.tiny.cloneAny.pojo;

import java.util.ArrayList;
import java.util.Set;

public abstract class Foo implements Set<ArrayList<? extends String>>{

	public static void main(String[] args) {
		System.out.println(TypeDefBuilder.createTypeDef(Foo0.class.getName()).typeSlot);
		System.out.println(TypeDefBuilder.createTypeDef(Foo1.class.getName()).typeSlot);
		System.out.println(TypeDefBuilder.createTypeDef(Foo2.class.getName()).typeSlot);
	}

}
