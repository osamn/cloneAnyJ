package ReIW.tiny.cloneAny.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;

import ReIW.tiny.cloneAny.core.AssemblyDomain;
import ReIW.tiny.cloneAny.core.AssemblyException;
import ReIW.tiny.cloneAny.pojo.Operand;

public final class DittoClassBuilder {

	private final AssemblyDomain domain;

	public DittoClassBuilder() {
		domain = AssemblyDomain.getDefaultAssemblyDomain();
	}

	public Class<?> createClass(final CKey key) {
		final AssemblyDomain domain = AssemblyDomain.getDefaultAssemblyDomain();
		try {
			loadClass(key);
			return domain.forName(getDittoName(key));
		} catch (IOException | ClassNotFoundException e) {
			throw new AssemblyException(e);
		}
	}

	private void loadClass(final CKey key) throws IOException {
		final Stream<Operand> ops = Operand.builder(key.lhs, key.rhs).operands(true);
		final Map<String, List<Operand>> opGroup = ops.collect(Collectors.groupingBy(new Function<Operand, String>() {
			private boolean ctor = true;

			@Override
			public String apply(Operand t) {
				if (ctor) {
					if (t instanceof Operand.Ctor) {
						ctor = false;
					}
					return "ctor";
				}
				return "prop";
			}

		}));

		final ClassVisitor cv = domain.getTerminalClassVisitor();
		final ClassReader cr = new ClassReader(AbstractDitto.class.getName());

		// TODO びじたのチェーン作って accept する
	}

	private static String getDittoName(final CKey key) {
		return "$ditto." + key.toString();
	}
}
