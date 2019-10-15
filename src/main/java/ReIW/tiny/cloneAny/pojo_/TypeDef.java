package ReIW.tiny.cloneAny.pojo_;

import java.util.stream.Stream;

import ReIW.tiny.cloneAny.pojo_.impl.TypeSlotBuilder;

public interface TypeDef {

	boolean hasDefaultCtor();
	
	boolean isList();

	boolean isMap();

	boolean isCharSequence();
	
	boolean isNumber();

	boolean hasAncestor(String descriptor);
	
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
		// FIXME Slot から直接作らないとまずくね？
		return new TypeSlotBuilder().buildTypeSlot(slot.getTypeDescriptor()).bind(slot.slotList);
	}
}