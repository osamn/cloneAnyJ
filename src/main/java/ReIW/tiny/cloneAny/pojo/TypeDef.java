package ReIW.tiny.cloneAny.pojo;

import java.util.stream.Stream;

import ReIW.tiny.cloneAny.pojo.impl.TypeSlotBuilder;
import ReIW.tiny.cloneAny.utils.Descriptors;

public interface TypeDef {

	/** internalName */
	String getName();

	boolean hasDefaultCtor();
	
	boolean isCertainBound();

	boolean isArrayType();

	boolean isPrimitiveType();

	boolean isBoxingType();

	boolean isList();

	boolean isMap();

	boolean isCharSequence();

	Slot elementSlot(); // List

	Slot valueSlot(); // Map

	Stream<Accessor> accessors();

	static TypeDef createInstance(final Class<?> clazz) {
		return new TypeSlotBuilder().buildTypeSlot(clazz);
	}

	static TypeDef createInstance(final Slot slot) {
		return new TypeSlotBuilder().buildTypeSlot(Descriptors.toClass(slot.descriptor)).bind(slot.slotList);
	}
}