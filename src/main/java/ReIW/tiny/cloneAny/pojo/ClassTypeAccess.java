package ReIW.tiny.cloneAny.pojo;

import java.util.stream.Stream;

import org.objectweb.asm.Type;

import ReIW.tiny.cloneAny.pojo.impl.ClassTypeBuilder;

public interface ClassTypeAccess {

	Slot getSlot();

	Stream<Accessor> accessors();

	boolean isAssignableTo(final String descriptor);

	static ClassTypeAccess create(final Class<?> clazz) {
		return new ClassTypeBuilder().buildClassType(Type.getDescriptor(clazz));
	}

	static ClassTypeAccess create(final Slot slot) {
		var ct = new ClassTypeBuilder().buildClassType(slot.getDescriptor());
		if (slot.elementSlot().size() == 0) {
			return ct.bind(slot.elementSlot());
		}
		return ct;
	}
}
