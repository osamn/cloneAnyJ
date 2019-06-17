package ReIW.tiny.cloneAny.pojo;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

final class TypeSlot {

	final List<Slot> formalSlots;
	final Slot superSlot;
	final List<Slot> interfaceSlot;

	TypeSlot(List<Slot> formal, List<Slot> superSlot) {
		this.formalSlots = formal;
		this.superSlot = superSlot.get(0);
		this.interfaceSlot = superSlot.subList(1, superSlot.size());
	}

	@Override
	public String toString() {
		try (StringWriter writer = new StringWriter(); PrintWriter out = new PrintWriter(writer)) {
			out.print("TypeSlot [");
			for (Slot s : formalSlots) {
				out.write('\n');
				out.print(s);
			}
			out.println(":");
			out.print(superSlot);
			for (Slot s : interfaceSlot) {
				out.write('\n');
				out.print(s);
			}
			out.write(']');
			return writer.toString();
		} catch (IOException e) {
			throw new RuntimeException("Unhandled", e);
		}
	}

}
