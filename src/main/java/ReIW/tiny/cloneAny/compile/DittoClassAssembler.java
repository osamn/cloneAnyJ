package ReIW.tiny.cloneAny.compile;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;

import ReIW.tiny.cloneAny.core.AssemblyDomain;
import ReIW.tiny.cloneAny.core.AssemblyException;
import ReIW.tiny.cloneAny.pojo.UnboundFormalTypeParameterException;
import ReIW.tiny.cloneAny.pojo_.TypeDef;

import static ReIW.tiny.cloneAny.utils.Descriptors.*;

final class DittoClassAssembler {

	private final String clazzName;
	private final TypeDef lhsDef;
	private final TypeDef rhsDef;

	private boolean trace;
	private boolean verify;

	DittoClassAssembler(final String className, final TypeDef lhs, final TypeDef rhs) {
		this.clazzName = className;
		this.lhsDef = lhs;
		this.rhsDef = rhs;
	}

	Class<?> ensureDittoClass() {
		final AssemblyDomain domain = AssemblyDomain.getDefaultAssemblyDomain();
		synchronized (domain) {
			try {
				final Class<?> clazz = domain.findLocalClass(clazzName);
				if (clazz == null) {
					if (!lhsDef.toSlot().isCertainBound() || !rhsDef.toSlot().isCertainBound()) {
						throw new UnboundFormalTypeParameterException();
					}
					loadConcreteDitto(domain);
					return domain.findLocalClass(clazzName);
				} else {
					return clazz;
				}
			} catch (IOException e) {
				throw new AssemblyException(e);
			}
		}
	}

	public void setVerify(final boolean verify) {
		this.verify = verify;
	}

	public void setTrace(final boolean trace) {
		this.trace = trace;
	}

	private void loadConcreteDitto(final AssemblyDomain domain) throws IOException {
		// prepare visitor chain.
		final ClassVisitor term = domain.getTerminalClassVisitor(this::inspectBytes);
		final ClassVisitor cv0 = new ConcreteDittoClassVisitor(clazzName, term);
		final ClassVisitor cv1 = new ImplementClassNameGetterVisitor(toInternalName(lhsDef.toSlot().descriptor),
				toInternalName(rhsDef.toSlot().descriptor), cv0);
		final ClassVisitor cv2 = new ImplementCopyOrCloneVisitor(new OperandGenerator(lhsDef, rhsDef), cv1);
		// クラスを構築してロードする
		final ClassReader cr = new ClassReader(AbstractDitto.class.getName());
		cr.accept(cv2, ClassReader.SKIP_DEBUG);
	}

	private PrintWriter debugOut = new PrintWriter(System.out);

	/** for debugging use only ;-) */
	private void inspectBytes(final byte[] b) {
		if (trace) {
			try {
				final Class<?> clazz = Class.forName("org.objectweb.asm.util.TraceClassVisitor");
				final ClassVisitor cv = (ClassVisitor) clazz
						.getDeclaredConstructor(ClassVisitor.class, PrintWriter.class).newInstance(null, debugOut);
				new ClassReader(b).accept(cv, 0);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException
					| ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		if (verify) {
			try {
				final Class<?> clazz = Class.forName("org.objectweb.asm.util.CheckClassAdapter");
				final Method m = clazz.getDeclaredMethod("verify", ClassReader.class, boolean.class, PrintWriter.class);
				m.invoke(clazz, new ClassReader(b), true, debugOut);
			} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}
}
