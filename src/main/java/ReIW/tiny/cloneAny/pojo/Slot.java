package ReIW.tiny.cloneAny.pojo;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

final class Slot {

	Slot(String typeParam, String typeClass) {
		this.typeParam = typeParam;
		this.typeClass = typeClass;
	}

	final String typeParam;
	final String typeClass;
	final ArrayList<Slot> slotList = new ArrayList<>(5);

	@Override
	public String toString() {
		try (StringWriter writer = new StringWriter(); PrintWriter out = new PrintWriter(writer)) {
			printTo(out, "");
			// 最後の改行を取り除いてあげる
			final String s = writer.toString();
			return s.substring(0, s.length() - 1);
		} catch (IOException e) {
			throw new RuntimeException("Unhandled", e);
		}
	}

	private void printTo(PrintWriter out, String indent) {
		out.format("%sSlot [typeParam=%s, typeClass=%s]\n", indent, typeParam, typeClass);
		indent += "  ";
		for (Slot slot : slotList) {
			slot.printTo(out, indent);
		}
	}

}
