package ReIW.tiny.cloneAny.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.objectweb.asm.Type;

public final class Slot {

	public static Slot fromClass(final Class<?> clazz) {
		return new Slot("=", Type.getDescriptor(clazz));
	}

	public final String typeParam;
	public final String typeClass; // descriptor 文字列が入るはず
	public final List<Slot> slotList = new ArrayList<>(5);

	Slot(final String typeParam) {
		this(typeParam, Type.getDescriptor(Object.class));
	}

	Slot(final String typeParam, final String typeClass) {
		this.typeParam = typeParam;
		this.typeClass = typeClass;
	}

	Slot rebind(final Map<String, String> binds) {
		if (slotList.size() == 0) {
			// 子要素がない ＆＆
			if (typeParam == null || typeParam.contentEquals("=") || typeParam.contentEquals("+")
					|| typeParam.contentEquals("-")) {
				// 自身の型パラメタが解決済み
				return this;
			}
		}
		final String bound = binds.get(typeParam);
		final Slot slot;
		if (bound == null) {
			// 自分の typeParam が再定義されていない
			slot = new Slot(typeParam, typeClass);
		} else if (bound.startsWith("T")) {
			// 型パラメタ名の再定義
			// see TypeDef#createBindMap
			slot = new Slot(bound.substring(1), typeClass);
		} else {
			// 型パラメタに型引数をくっつける
			// certain bind
			slot = new Slot("=", bound);
		}
		for (Slot child : slotList) {
			slot.slotList.add(child.rebind(binds));
		}
		return slot;
	}

	@Override
	public int hashCode() {
		// slotList は構築後に追加されるため hashCode は使用する時点で再計算が必要
		return Objects.hash(typeParam, typeClass, slotList);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Slot other = (Slot) obj;
		if (slotList == null) {
			if (other.slotList != null)
				return false;
		} else if (!slotList.equals(other.slotList))
			return false;
		if (typeClass == null) {
			if (other.typeClass != null)
				return false;
		} else if (!typeClass.equals(other.typeClass))
			return false;
		if (typeParam == null) {
			if (other.typeParam != null)
				return false;
		} else if (!typeParam.equals(other.typeParam))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Slot [typeParam=" + typeParam + ", typeClass=" + typeClass + ", slotList=" + slotList + "]";
	}

}
