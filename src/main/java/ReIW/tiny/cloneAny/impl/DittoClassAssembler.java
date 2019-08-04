package ReIW.tiny.cloneAny.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Stream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.util.TraceClassVisitor;

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

	public DittoClassAssembler(final CKey key) {
		// 実体化するクラスの名前
		clazzName = "$ditto$/" + key.getInternalName();
		// AbstractDitto に埋め込む情報
		lhsName = key.lhs.getName();
		rhsName = key.rhs.getName();
		// オペランドの元
		builder = Operand.builder(key.lhs, key.rhs);
	}

	public Class<?> createClass() {
		final AssemblyDomain domain = AssemblyDomain.getDefaultAssemblyDomain();
		try {
			loadConcreteDitto(domain);
			return domain.forName(clazzName);
		} catch (IOException | ClassNotFoundException e) {
			throw new AssemblyException(e);
		}
	}

	private void loadConcreteDitto(final AssemblyDomain domain) throws IOException {
		final Stream<Operand> ops = builder.operands(true);
		final ClassReader cr = new ClassReader(AbstractDitto.class.getName());
		final ClassVisitor cv0 = new ConcreteDittoClassVisitor(clazzName,
				// remove trace
				domain.getTerminalClassVisitor(new TraceClassVisitor(new PrintWriter(System.out))));
		final ClassVisitor cv1 = new ImplementClassNameGetterVisitor(lhsName, rhsName, cv0);
		final ClassVisitor cv = new ImplementCopyOrCloneVisitor(ops, cv1);
		cr.accept(cv, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES | ClassReader.SKIP_CODE);
	}

}
