package ReIW.tiny.cloneAny.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;

import ReIW.tiny.cloneAny.core.AssemblyDomain;
import ReIW.tiny.cloneAny.core.AssemblyException;
import ReIW.tiny.cloneAny.pojo.Operand;

// generic な要素を持つ配列の場合、型パラメタが消えちゃうの
// なので配列のスロットの型名から再度スロットをとって formal がのこってたらエラーにするとか
// そんな感じにしないといかんかも




public final class DittoClassBuilder {

	private final List<Operand> ctorOps;
	private final List<Operand> propOps;
	private final String clazzName;
	private final String signature;

	public DittoClassBuilder(final CKey key) {
		final Stream<Operand> ops = Operand.builder(key.lhs, key.rhs).operands(true);
		ctorOps = new ArrayList<>();
		propOps = ops.filter(new Predicate<Operand>() {
			boolean hadCtor = false;

			@Override
			public boolean test(Operand t) {
				if (!hadCtor) {
					ctorOps.add(t);
					if (t instanceof Operand.Ctor) {
						hadCtor = true;
					}
					return false;
				}
				return true;
			}
		}).collect(Collectors.toList());
		// 実体化するクラスの名前
		clazzName = "$ditto." + key.toString();
		// generic なシグネチャ文字列
		signature = key.toSignature();
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
		// TODO びじたのチェーン作って accept する
	}

}
