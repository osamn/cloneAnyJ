package ReIW.tiny.cloneAny.pojo;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public final class TypeSlot {

	final List<Slot> formalSlots;
	final Slot superSlot;

	TypeSlot(List<Slot> formal, Slot superSlot) {
		this.formalSlots = formal;
		this.superSlot = superSlot;
	}

	@Override
	public String toString() {
		try (StringWriter writer = new StringWriter(); PrintWriter out = new PrintWriter(writer)) {
			out.println("== formal ==");
			for (Slot s : formalSlots) {
				out.println(s);
			}
			out.println("== super ==");
			out.println(superSlot);
			return writer.toString();
		} catch (IOException e) {
			throw new RuntimeException("Unhandled", e);
		}
	}

}
