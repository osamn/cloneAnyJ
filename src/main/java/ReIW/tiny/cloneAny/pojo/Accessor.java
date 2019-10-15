package ReIW.tiny.cloneAny.pojo;

import java.util.LinkedHashMap;
import java.util.Map;

public interface Accessor {

	enum AccessType {
		// フィールド
		Field, ReadonlyField,
		// プロパティ
		Get, Set,
		// ctor の引数セット
		LumpSet,
		// Array
		ArrayGet, ArraySet,
		// List
		ListGet, ListAdd,
		// Map
		MapGet, MapPut,
	}

	AccessType getType();

	boolean canRead();

	boolean canWrite();

	// Field/Method の持ち主の internalName
	String getOwner();

	// プロパティとしての名称
	String getName();

	class FieldAccess implements Accessor {

		private final AccessType type;
		private final String owner;
		private final String name;

		public final Slot slot;

		public FieldAccess(final AccessType type, final String owner, final String name, final Slot slot) {
			this.type = type;
			this.owner = owner;
			this.name = name;
			this.slot = slot;
		}

		@Override
		public AccessType getType() {
			return type;
		}

		@Override
		public boolean canRead() {
			return true;
		}

		@Override
		public boolean canWrite() {
			return type == AccessType.Field;
		}

		@Override
		public String getOwner() {
			return owner;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return "FieldAccess [type=" + type + ", owner=" + owner + ", name=" + name + ", slot=" + slot + "]";
		}
	}

	class PropAccess implements Accessor {

		private final AccessType type;
		private final String owner;
		private final String name;

		public final String rel;
		public final String methodDescriptor;
		public final Slot slot;

		public PropAccess(final AccessType type, final String owner, final String name, final String rel,
				final String methodDescriptor, final Slot slot) {
			this.type = type;
			this.owner = owner;
			this.name = name;
			this.rel = rel;
			this.methodDescriptor = methodDescriptor;
			this.slot = slot;
		}

		@Override
		public AccessType getType() {
			return type;
		}

		@Override
		public boolean canRead() {
			return type == AccessType.Get;
		}

		@Override
		public boolean canWrite() {
			return type == AccessType.Set;
		}

		@Override
		public String getOwner() {
			return owner;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return "PropAccess [type=" + type + ", owner=" + owner + ", name=" + name + ", rel=" + rel
					+ ", methodDescriptor=" + methodDescriptor + ", slot=" + slot + "]";
		}
	}

	class LumpSetAccess implements Accessor {

		private final String owner;

		public final String rel;
		public final String methodDescriptor;

		public final Map<String, Slot> slotInfo = new LinkedHashMap<>();

		public LumpSetAccess(final String owner, final String rel, final String methodDescriptor) {
			this.owner = owner;
			this.rel = rel;
			this.methodDescriptor = methodDescriptor;
		}

		@Override
		public AccessType getType() {
			return AccessType.LumpSet;
		}

		@Override
		public boolean canRead() {
			return false;
		}

		@Override
		public boolean canWrite() {
			return true;
		}

		@Override
		public String getOwner() {
			return owner;
		}

		@Override
		public String getName() {
			return rel;
		}

		@Override
		public String toString() {
			return "LumpSetAccess [owner=" + owner + ", rel=" + rel + ", methodDescriptor=" + methodDescriptor
					+ ", slotInfo=" + slotInfo + "]";
		}
	}

	/*
	 * Indexed と Keyed は owner が null だよ
	 * それ自身をアクセサとみなすので、Field/Method のようにオーナはないの
	 * あえて言えば owner は this になるけど
	 * 
	 * class Bar extends List<Foo> で
	 * ditto<Foo[], Bar> とかそんなとき
	 * 
	 */

	// Array/java.util.List
	class IndexedAccess implements Accessor {

		private final AccessType type;

		public final Slot elementSlot;

		public IndexedAccess(final AccessType type, final Slot elementSlot) {
			this.type = type;
			this.elementSlot = elementSlot;
		}

		@Override
		public AccessType getType() {
			return type;
		}

		@Override
		public boolean canRead() {
			return type == AccessType.ArrayGet || type == AccessType.ListGet;
		}

		@Override
		public boolean canWrite() {
			return type == AccessType.ArraySet || type == AccessType.ListAdd;
		}

		@Override
		public String getOwner() {
			return null;
		}

		@Override
		public String getName() {
			return "@indexed";
		}

		@Override
		public String toString() {
			return "IndexedAccess [type=" + type + ", elementSlot=" + elementSlot + "]";
		}
	}

	// java.util.Map
	class KeyedAccess implements Accessor {

		private final AccessType type;

		public final Slot keySlot;
		public final Slot valueSlot;

		public KeyedAccess(final AccessType type, final Slot keySlot, final Slot valueSlot) {
			this.type = type;
			this.keySlot = keySlot;
			this.valueSlot = valueSlot;
		}

		@Override
		public AccessType getType() {
			return type;
		}

		@Override
		public boolean canRead() {
			return type == AccessType.ArrayGet || type == AccessType.ListGet;
		}

		@Override
		public boolean canWrite() {
			return type == AccessType.ArraySet || type == AccessType.ListAdd;
		}

		@Override
		public String getOwner() {
			return null;
		}

		@Override
		public String getName() {
			return "@keyed";
		}

		@Override
		public String toString() {
			return "KeyedAccess [type=" + type + ", keySlot=" + keySlot + ", valueSlot=" + valueSlot + "]";
		}
	}

}
