package ReIW.tiny.cloneAny.pojo;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

final class TypeSlot {

	final List<Slot> formalSlots;
	final Slot superSlot;

	TypeSlot(List<Slot> formal, Slot superSlot) {
		this.formalSlots = formal;
		this.superSlot = superSlot;
	}

	@Override
	public String toString() {
		try (StringWriter writer = new StringWriter(); PrintWriter out = new PrintWriter(writer)) {
			for (Slot s : formalSlots) {
				out.print(s);
			}
			out.println("== super ==");
			out.print(superSlot);
			return writer.toString();
		} catch (IOException e) {
			throw new RuntimeException("Unhandled", e);
		}
	}

}
