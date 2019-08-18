package ReIW.tiny.cloneAny.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;

import ReIW.tiny.cloneAny.core.AssemblyDomain;
import ReIW.tiny.cloneAny.core.AssemblyException;
import ReIW.tiny.cloneAny.pojo.Operand;

// generic な要素を持つ配列の場合、型パラメタが消えちゃうの
// なので配列のスロットの型名から再度スロットをとって formal がのこってたらエラーにするとか
// そんな感じにしないといかんかも

public final class DittoClassAssembler {

	private final Operand.Builder builder;
	private final String clazzName;
	private final String lhsName;
	private final String rhsName;

	private boolean trace;
	private boolean verify;

	DittoClassAssembler(final CKey key) {
		// 実体化するクラスの名前
		clazzName = key.getInternalName();
		// AbstractDitto に埋め込む情報
		lhsName = Type.getType(key.lhs.typeClass).getInternalName();
		rhsName = Type.getType(key.rhs.typeClass).getInternalName();
		// オペランドの元
		builder = Operand.builder(key.lhs, key.rhs);
	}

	Class<?> createClass() {
		final AssemblyDomain domain = AssemblyDomain.getDefaultAssemblyDomain();
		try {
			loadConcreteDitto(domain);
			return domain.forName(clazzName);
		} catch (IOException | ClassNotFoundException e) {
			throw new AssemblyException(e);
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
		final ClassVisitor cv1 = new ImplementClassNameGetterVisitor(lhsName, rhsName, cv0);
		final ClassVisitor cv2 = new ImplementCopyOrCloneVisitor(builder.operands(true), cv1);
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
