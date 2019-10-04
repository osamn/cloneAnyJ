package ReIW.tiny.cloneAny.pojo;

import java.util.stream.Stream;

import ReIW.tiny.cloneAny.pojo.impl.TypeSlotBuilder;

public interface TypeDef {

	boolean hasDefaultCtor();
	
	boolean isList();

	boolean isMap();

	boolean isCharSequence();
	
	boolean isNumber();

	/** Array/List の要素スロット */
	Slot elementSlot();

	/** Map の value スロット */
	Slot valueSlot();

	Stream<Accessor> accessors();
	
	/** このインスタンスを表すスロット */
	Slot toSlot();

	static TypeDef createInstance(final Class<?> clazz) {
		return new TypeSlotBuilder().buildTypeSlot(clazz);
	}

	static TypeDef createInstance(final Slot slot) {
		return new TypeSlotBuilder().buildTypeSlot(slot.descriptor).bind(slot.slotList);
	}
}