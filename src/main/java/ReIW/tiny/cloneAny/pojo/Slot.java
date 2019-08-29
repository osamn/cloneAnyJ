package ReIW.tiny.cloneAny.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.objectweb.asm.Type;

public class Slot {

	public final String typeParam;
	public final String descriptor;
	public final List<Slot> slotList = new ArrayList<>(5);

	public boolean isArray;
	public boolean instanceOfMap;
	public boolean instanceOfList;

	public Slot(final Class<?> clazz) {
		if (clazz.isArray()) {
			// 配列クラスを直接指定して作成すると、slotList.get(0) が elementType になって
			// いろいろ面倒になるのでエラーにしておく
			throw new IllegalArgumentException();
		}
		this.typeParam = null;
		this.descriptor = Type.getDescriptor(clazz);
		this.instanceOfMap = Map.class.isAssignableFrom(clazz);
		this.instanceOfList = List.class.isAssignableFrom(clazz);
	}

	public Slot(final String typeParam, final String descriptor) {
		this.typeParam = typeParam;
		this.descriptor = descriptor;
		if (descriptor.contentEquals("[")) {
			this.isArray = true;
			this.instanceOfMap = false;
			this.instanceOfList = false;
		} else {
			final Class<?> clazz = Type.getType(descriptor).getClass();
			this.isArray = false;
			this.instanceOfMap = Map.class.isAssignableFrom(clazz);
			this.instanceOfList = List.class.isAssignableFrom(clazz);
		}
	}

	public Slot rebind(final Map<String, String> binds) {
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
			slot = new Slot(typeParam, descriptor);
		} else if (bound.startsWith("T")) {
			// 型パラメタ名の再定義
			// see TypeDef#createBindMap
			slot = new Slot(bound.substring(1), descriptor);
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
		return Objects.hash(typeParam, descriptor, slotList);
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
		if (descriptor == null) {
			if (other.descriptor != null)
				return false;
		} else if (!descriptor.equals(other.descriptor))
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
		return "Slot [typeParam=" + typeParam + ", typeClass=" + descriptor + ", slotList=" + slotList + "]";
	}

}
