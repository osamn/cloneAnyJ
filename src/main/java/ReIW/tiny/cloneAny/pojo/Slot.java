package ReIW.tiny.cloneAny.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Type;

import ReIW.tiny.cloneAny.utils.Descriptors;

public class Slot {

	/**
	 * descriptor から Slot をつくる.
	 * 
	 * signature がある場合は SignatureReader から作るんだけど non generic な人の場合は自分で作らないといかんの
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
	public final List<Slot> slotList = new ArrayList<>();

	public final boolean isArrayType;
	public final boolean isPrimitiveType;
	public final boolean isMap;
	public final boolean isList;
	public final boolean isCharSequence;

	public Slot(final Class<?> clazz) {
		this.typeParam = null;
		this.descriptor = Type.getDescriptor(clazz);
		this.isArrayType = clazz.isArray();
		this.isPrimitiveType = clazz.isPrimitive();
		this.isMap = Map.class.isAssignableFrom(clazz);
		this.isList = List.class.isAssignableFrom(clazz);
		this.isCharSequence = CharSequence.class.isAssignableFrom(clazz);
	}

	public Slot(final String typeParam, final String descriptor) {
		this.typeParam = typeParam;
		this.descriptor = descriptor;
		if (descriptor.contentEquals("[")) {
			this.isArrayType = true;
			this.isPrimitiveType = false;
			this.isMap = false;
			this.isList = false;
			this.isCharSequence = false;
		} else {
			this.isArrayType = false;
			final Class<?> clazz = Descriptors.toClass(descriptor);
			this.isPrimitiveType = clazz.isPrimitive();
			this.isMap = Map.class.isAssignableFrom(clazz);
			this.isList = List.class.isAssignableFrom(clazz);
			this.isCharSequence = CharSequence.class.isAssignableFrom(clazz);
		}
	}

	public boolean keyed() {
		// Map のキーが String 以外はキーアクセス不可にする
		// キー自体を型変換するのがいやなので
		return isMap && slotList.get(0).descriptor.contentEquals("Ljava/lang/String;");
	}

	public boolean indexed() {
		return isArrayType || isList;
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
