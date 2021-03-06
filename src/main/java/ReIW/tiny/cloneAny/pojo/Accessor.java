package ReIW.tiny.cloneAny.pojo;

import java.util.LinkedHashMap;
import java.util.Map;

public interface Accessor {

	enum AccessType {
		// フィールド
		Field, ReadonlyField,
		// プロパティ
		Getter, Setter,
		// 基本コンストラクタしかないけど
		// 複数の引数で設定するタイプ
		LumpSet,
		// Array
		ArrayType,
		// List
		ListType,
		// Set
		SetType,
		// Map
		MapType,
	}

	AccessType getType();

	boolean canRead();

	boolean canWrite();

	// プロパティが実際に宣言されてるクラスの descriptor
	// デバッグ時に見れるとうれしいだけの情報なんで、これを使ってなんかすることはないよ
	String getOwner();

	// プロパティとしての名称
	String getName();

	class FieldAccess implements Accessor {

		private final AccessType type;
		private final String owner;
		private final String name;

		public final Slot slot;

		public FieldAccess(final AccessType type, final String owner, final String name, final Slot slot) {
			assert type == AccessType.Field || type == AccessType.ReadonlyField;
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
			assert type == AccessType.Getter || type == AccessType.Setter;
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
			return type == AccessType.Getter;
		}

		@Override
		public boolean canWrite() {
			return type == AccessType.Setter;
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

		public final Map<String, Slot> parameters;

		// 並び順が重要なんで LinkedHashMap
		public static Map<String, Slot> emptyParamMap() {
			return new LinkedHashMap<String, Slot>();
		}

		public LumpSetAccess(final String owner, final String rel, final String methodDescriptor,
				final Map<String, Slot> parameters) {
			this.owner = owner;
			this.rel = rel;
			this.methodDescriptor = methodDescriptor;
			this.parameters = parameters;
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
			return "LumpSetAccess [owner=" + owner + ", rel=" + rel + ", methodDescriptor=" + methodDescriptor + ", "
					+ parameters + "]";
		}
	}

	// Array/java.util.List/java.util.Set
	class SequentialAccess implements Accessor {

		private final AccessType type;
		private final String owner;

		public final Slot elementSlot;

		public SequentialAccess(final AccessType type, final String owner, final Slot elementSlot) {
			assert type == AccessType.ArrayType || type == AccessType.ListType || type == AccessType.SetType;
			this.owner = owner;
			this.type = type;
			this.elementSlot = elementSlot;
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
			return true;
		}

		@Override
		public String getOwner() {
			return owner;
		}

		@Override
		public String getName() {
			return "@sequential";
		}

		@Override
		public String toString() {
			return "SequentialAccess [type=" + type + ", owner=" + owner + ", elementSlot=" + elementSlot + "]";
		}
	}

	// java.util.Map
	class KeyedAccess implements Accessor {

		private final AccessType type;
		private final String owner;

		public final Slot keySlot;
		public final Slot valueSlot;

		public KeyedAccess(final AccessType type, final String owner, final Slot keySlot, final Slot valueSlot) {
			assert type == AccessType.MapType;
			this.owner = owner;
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
			return true;
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
			return "@keyed";
		}

		@Override
		public String toString() {
			return "KeyedAccess [type=" + type + ", owner=" + owner + ", keySlot=" + keySlot + ", valueSlot="
					+ valueSlot + "]";
		}
	}

}
