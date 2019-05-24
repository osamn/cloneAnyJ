package ReIW.tiny.cloneAny.pojo;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Stack;

import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import ReIW.tiny.cloneAny.asm7.DefaultSignatureVisitor;

class TypeSlot {

	public static void main(String[] args) {
		TypeSlot ts = accept(
				"<V2:Ljava/lang/Object;ML2::Ljava/util/Map<Ljava/lang/String;Ljava/util/List<TV2;>;>;>LReIW/tiny/cloneAny/pojo/Typed1<TV2;TML2;Ljava/util/HashSet<Ljava/util/ArrayList<Ljava/lang/String;>;>;Ljava/lang/String;>;");
		System.out.println(ts);
	}

	static TypeSlot accept(final String signature) {
		if (signature == null) {
			return null;
		}
		final SlotParser parser = new SlotParser();
		new SignatureReader(signature).accept(parser);
		return new TypeSlot(parser.formalSlots, parser.supersSlots.get(0).slotList);
	}

	final ArrayList<Slot> formalSlots;
	final ArrayList<Slot> supersSlots;

	private TypeSlot(ArrayList<Slot> formal, ArrayList<Slot> supers) {
		this.formalSlots = formal;
		this.supersSlots = supers;
	}

	@Override
	public String toString() {
		try (StringWriter writer = new StringWriter(); PrintWriter out = new PrintWriter(writer)) {
			out.println("== formal ==");
			for (Slot s : formalSlots) {
				out.println(s);
			}
			out.println("== supers ==");
			for (Slot s : supersSlots) {
				out.println(s);
			}
			return writer.toString();
		} catch (IOException e) {
			throw new RuntimeException("Unhandled", e);
		}
	}

	private static class SlotParser extends DefaultSignatureVisitor {
		private final ArrayList<Slot> formalSlots = new ArrayList<>();
		private final ArrayList<Slot> supersSlots = new ArrayList<>();
		private final Stack<Slot> stack = new Stack<>();
		private String formalTypeParameter;
		private ArrayList<Slot> activeSlots = formalSlots;

		@Override
		public SignatureVisitor visitSuperclass() {
			activeSlots = supersSlots;
			formalTypeParameter = "@@";
			return super.visitSuperclass();
		}

		@Override
		public void visitFormalTypeParameter(String name) {
			formalTypeParameter = name;
		}

		@Override
		public SignatureVisitor visitClassBound() {
			return super.visitClassBound();
		}

		@Override
		public SignatureVisitor visitInterfaceBound() {
			return super.visitInterfaceBound();
		}

		@Override
		public void visitClassType(String name) {
			stack.push(new Slot(formalTypeParameter, name));
		}

		@Override
		public SignatureVisitor visitTypeArgument(char wildcard) {
			formalTypeParameter = String.valueOf(wildcard);
			return super.visitTypeArgument(wildcard);
		}

		@Override
		public void visitTypeVariable(String name) {
			stack.peek().slotList.add(new Slot(name, null));
		}

		@Override
		public void visitEnd() {
			Slot slot = stack.pop();
			if (stack.isEmpty()) {
				activeSlots.add(slot);
			} else {
				stack.peek().slotList.add(slot);
			}
		}

	}
}
