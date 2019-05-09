package helpers;

import java.io.InputStream;
import java.io.PrintWriter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;

import beans.Bean2;

public final class ShowB {

	public static void main(String[] args) throws Exception {
		showBytecode(Bean2.class);
	}

	public static void showBytecode(Class<?> clazz) throws Exception {
		final String path = clazz.getName().replace('.', '/') + ".class";

		try (InputStream is = clazz.getClassLoader().getResourceAsStream(path)) {
			final ClassReader cr = new ClassReader(is);
			final TraceClassVisitor tcv = new TraceClassVisitor(new PrintWriter(System.out));
			cr.accept(tcv, 0);
		}
	}

}
