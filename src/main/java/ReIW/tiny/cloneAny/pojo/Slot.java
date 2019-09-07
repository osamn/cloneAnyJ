package ReIW.tiny.cloneAny.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Type;

import ReIW.tiny.cloneAny.utils.Descriptors;

public class Slot {

	public static Slot getSlot(final String typeParam, final Class<?> clazz) {
		if (clazz.isArray()) {
			final Slot root = new Slot(typeParam, "[", true, false, false, false, false);
			final Class<?> componentType = clazz.getComponentType();
			if (componentType != null) {
				root.slotList.add(getSlot(null, componentType));
			}
			return root;
		} else {
			return new Slot(typeParam, Type.getDescriptor(clazz), false, clazz.isPrimitive(),
					Map.class.isAssignableFrom(clazz), List.class.isAssignableFrom(clazz),
					CharSequence.class.isAssignableFrom(clazz));
		}
	}

	public static Slot getSlot(final String typeParam, final String descriptor) {
		if (descriptor.startsWith("[")) {
			final Slot root = new Slot(typeParam, "[", true, false, false, false, false);
			final String componentDesc = descriptor.substring(1);
			// SignatureReader#visitArrayType から呼ばれるときは descriptor == "[" なので
			// 子スロットを追加しないよ
			if (!componentDesc.isEmpty()) {
				root.slotList.add(getSlot(typeParam, componentDesc));
			}
			return root;
		} else {
			final Class<?> clazz = Descriptors.toClass(descriptor);
			return new Slot(typeParam, Type.getDescriptor(clazz), false, clazz.isPrimitive(),
					Map.class.isAssignableFrom(clazz), List.class.isAssignableFrom(clazz),
					CharSequence.class.isAssignableFrom(clazz));
		}
	}

	@Deprecated
	public static Slot getSlot(final String descriptor) {
		return getSlotFromDesc(null, descriptor);
	}

	/*
	 * signature じゃなく descriptor からスロットを作る人
	 * 
	 * なので型パラメタとかないので配列じゃない場合は子スロットは入らないよ
	 */
	@Deprecated
	private static Slot getSlotFromDesc(final String typeParam, final String descriptor) {
		// 配列か？
		if (descriptor.startsWith("[")) {
			// 配列の間、配列を示す slot 階層を追加していく
			String desc = descriptor.substring(1);
			final Slot root = new Slot(typeParam, "[");
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
			return new Slot(typeParam, descriptor);
		}
	}

	public final String typeParam;
	private final String descriptor;

	public final List<Slot> slotList = new ArrayList<>();

	public final boolean isArrayType;
	public final boolean isPrimitiveType;
	public final boolean isMap;
	public final boolean isList;
	public final boolean isCharSequence;

	protected Slot(String typeParam, String descriptor, boolean isArrayType, boolean isPrimitiveType, boolean isMap,
			boolean isList, boolean isCharSequence) {
		this.typeParam = typeParam;
		this.descriptor = descriptor;
		this.isArrayType = isArrayType;
		this.isPrimitiveType = isPrimitiveType;
		this.isMap = isMap;
		this.isList = isList;
		this.isCharSequence = isCharSequence;
	}

	@Deprecated
	public Slot(final Class<?> clazz) {
		this.typeParam = null;
		this.descriptor = Type.getDescriptor(clazz);
		this.isArrayType = clazz.isArray();
		if (isArrayType) {
			this.isPrimitiveType = false;
			this.isMap = false;
			this.isList = false;
			this.isCharSequence = false;
		} else {
			this.isPrimitiveType = clazz.isPrimitive();
			this.isMap = Map.class.isAssignableFrom(clazz);
			this.isList = List.class.isAssignableFrom(clazz);
			this.isCharSequence = CharSequence.class.isAssignableFrom(clazz);
		}
	}

	@Deprecated
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

	public boolean isCertainBound() {
		final boolean bound = typeParam == null || typeParam.contentEquals("=") || typeParam.contentEquals("+")
				|| typeParam.contentEquals("-");
		return bound && slotList.stream().allMatch(Slot::isCertainBound);
	}

	public String getClassDescriptor() {
		if (isArrayType) {
			return descriptor + slotList.get(0).getClassDescriptor();
		}
		return descriptor;
	}

	public Slot rebind(final Map<String, String> binds) {
		if (isCertainBound()) {
			// 型パラメタが解決済みなので自身をそのまま返す
			return this;
		}
		final String bound = binds.get(typeParam);
		// とりあえず bind した自身のコピーを作る
		// 細かくチェックすればコピーしなくていいかもしれんけど面倒なんで
		final Slot slot;
		if (bound == null) {
			// 自分の typeParam が再定義されていない
			slot = new Slot(typeParam, descriptor);
		} else if (bound.startsWith("T")) {
			// 型パラメタ名の再定義
			// see TypeSlot#createBindMap
			slot = new Slot(bound.substring(1), descriptor);
		} else {
			// 型パラメタに型引数をくっつける
			// certain bind
			slot = getSlot("=", bound);
		}
		// 子スロットを bind しながらコピーする
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
