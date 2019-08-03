package ReIW.tiny.cloneAny.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Type;

public final class Slot {

	public final String typeParam;
	public final String typeClass;
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
	public String toString() {
		final StringBuilder buf = new StringBuilder();
		printTo(buf, "");
		return buf.toString();
	}

	private void printTo(final StringBuilder buf, String indent) {
		buf.append(String.format("%sSlot [typeParam=%s, typeClass=%s]", indent, typeParam, typeClass));
		indent += "  ";
		for (Slot slot : slotList) {
			buf.append('\n');
			slot.printTo(buf, indent);
		}
	}

}
