package ReIW.tiny.cloneAny.pojo;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Slot {

	public final String typeParam;
	public final String typeClass;
	public final List<Slot> slotList = new ArrayList<>(5);

	Slot(String typeParam, String typeClass) {
		this.typeParam = typeParam;
		this.typeClass = typeClass;
	}

	Slot rebind(Map<String, String> binds) {
		if (slotList.size() == 0) {
			if (typeParam == null) {
				return this;
			}
			if (typeParam.contentEquals("=") || typeParam.contentEquals("+") || typeParam.contentEquals("-")) {
				return this;
			}
		}
		final Slot slot;
		final String bound = binds.get(typeParam);
		if (bound == null) {
			slot = new Slot(typeParam, typeClass);
		} else if (bound.startsWith("T")) {
			// re-bind parameter name
			slot = new Slot(bound.substring(1), typeClass);
		} else {
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
		try (StringWriter writer = new StringWriter(); PrintWriter out = new PrintWriter(writer)) {
			printTo(out, "");
			return writer.toString();
		} catch (IOException e) {
			throw new RuntimeException("Unhandled", e);
		}
	}

	private void printTo(PrintWriter out, String indent) {
		out.format("%sSlot [typeParam=%s, typeClass=%s]", indent, typeParam, typeClass);
		indent += "  ";
		for (Slot slot : slotList) {
			out.write('\n');
			slot.printTo(out, indent);
		}
	}

}
