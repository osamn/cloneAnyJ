package ReIW.tiny.cloneAny.pojo;

import java.util.stream.Stream;

import ReIW.tiny.cloneAny.pojo.impl.TypeSlotBuilder;
import ReIW.tiny.cloneAny.utils.Descriptors;

public interface TypeDef {

	boolean hasDefaultCtor();
	
	boolean isList();

	boolean isMap();

	boolean isCharSequence();

	Slot elementSlot(); // List/Array の要素のスロット

	Slot valueSlot(); // Map の value のスロット

	Stream<Accessor> accessors();
	
	Slot toSlot();

	static TypeDef createInstance(final Class<?> clazz) {
		return new TypeSlotBuilder().buildTypeSlot(clazz);
	}

	static TypeDef createInstance(final Slot slot) {
		return new TypeSlotBuilder().buildTypeSlot(Descriptors.toClass(slot.descriptor)).bind(slot.slotList);
	}
}