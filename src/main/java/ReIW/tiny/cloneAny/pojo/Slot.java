package ReIW.tiny.cloneAny.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Type;

public class Slot {

	/**
	 * descriptor から Slot をつくる.
	 */
	public static Slot getSlot(final String descriptor) {
		// 配列か？
		if (descriptor.startsWith("[")) {
			// 配列の間、配列を示す slot 階層を追加していく
			String desc = descriptor.substring(1);
			final Slot root = new Slot(null, "[");
			Slot curr = root;
			while (desc.startsWith("[")) {
				Slot s = new Slot(null, "[");
				curr.slotList.add(s);
				curr = s;
				desc = desc.substring(1);
			}
			// 最後に elementType の slot を追加する
			curr.slotList.add(new Slot(null, desc));
			return root;
		} else {
			return new Slot(null, descriptor);
		}
	}

	public final String typeParam;
	public final String descriptor;
	public final List<Slot> slotList = new ArrayList<>(5);

	private final boolean isArray;
	private final boolean instanceOfMap;
	private final boolean instanceOfList;

	public Slot(final Class<?> clazz) {
		if (clazz.isArray()) {
			// 配列クラスを直接指定して作成すると、slotList.get(0) が elementType になって
			// いろいろ面倒になるのでエラーにしておく
			throw new IllegalArgumentException();
		}
		this.isArray = false;
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

	public boolean isArrayType() {
		return isArray;
	}

	public boolean keyed() {
		return instanceOfMap && slotList.get(0).descriptor.contentEquals("Ljava/lang/String;");
	}

	public boolean indexed() {
		return isArray || instanceOfList;
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
	public String toString() {
		return "Slot [typeParam=" + typeParam + ", descriptor=" + descriptor + ", slotList=" + slotList + "]";
	}

}
