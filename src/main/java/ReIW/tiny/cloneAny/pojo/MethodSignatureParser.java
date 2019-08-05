package ReIW.tiny.cloneAny.pojo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import ReIW.tiny.cloneAny.asm7.DefaultMethodVisitor;
import ReIW.tiny.cloneAny.asm7.DefaultSignatureVisitor;

final class MethodSignatureParser extends DefaultSignatureVisitor {

	static <T> void nop(T val) {
	};

	static MethodVisitor parameterParserVisitor(final String descriptor, final String signature,
			final BiConsumer<String, Slot> parametersCons) {
		// 引数Slotのリストを作成する
		final ArrayList<Slot> slots = new ArrayList<>();
		parseArgumentsAndReturn(descriptor, signature, slots::add, MethodSignatureParser::nop);

		// visitLocalVariable で使うため引数の個数をとっておく
		final int argSize = Type.getArgumentsAndReturnSizes(descriptor) >> 2;

		final Iterator<Slot> it = slots.iterator();
		return new DefaultMethodVisitor() {

			/*
			 * You need to compile your class with the -parameters option to make javac
			 * include the parameter names. ということでコンパイルオプション依存らしいので使えない
			 */
			// public void visitParameter(String name, int access)

			@Override
			public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end,
					int index) {
				// this(0) を除いた引数だけ処理する
				// ガードしないと引数じゃないローカル変数まで処理しちゃうので
				if (0 < index && index < argSize) {
					parametersCons.accept(name, it.next());
				}
			}

			@Override
			public void visitEnd() {
				if (it.hasNext()) {
					// 対象が残っている
					// -> visitLocalVariable が呼ばれていない
					// -> debug 情報がついていない
					// なのでコンストラクタパラメタに名前でマッチングできない
					// なのでエラーにしておく
					throw new UnboundMethodParameterNameException("No debug symbols.");
				}
			}
		};
	}

	/** 引数と戻り値の slot を作る */
	static void parseArgumentsAndReturn(final String descriptor, final String signature,
			final Consumer<Slot> argumentsCons, final Consumer<Slot> returnCons) {
		if (signature == null) {
			Type m = Type.getMethodType(descriptor);
			for (Type t : m.getArgumentTypes()) {
				argumentsCons.accept(new Slot(null, t.getDescriptor()));
			}
			returnCons.accept(new Slot(null, m.getReturnType().getDescriptor()));
		} else {
			final MethodSignatureParser parser = new MethodSignatureParser(argumentsCons, returnCons);
			new SignatureReader(signature).accept(parser);
		}
	}

	private final Consumer<Slot> arguments;
	private final Consumer<Slot> returns;

	private final Stack<Slot> stack = new Stack<>();

	private Consumer<Slot> cons;
	private String typeParamName;

	private MethodSignatureParser(Consumer<Slot> argumentsCons, Consumer<Slot> returnCons) {
		this.arguments = argumentsCons;
		this.returns = returnCons;
	}

	@Override
	public void visitFormalTypeParameter(String name) {
		throw new UnboundFormalTypeParameterException("Method should not have formal type parameter.");
	}

	@Override
	public SignatureVisitor visitParameterType() {
		cons = arguments;
		return super.visitParameterType();
	}

	@Override
	public void visitBaseType(char descriptor) {
		cons.accept(new Slot(null, String.valueOf(descriptor)));
	}

	@Override
	public SignatureVisitor visitReturnType() {
		cons = returns;
		return super.visitReturnType();
	}

	@Override
	public void visitClassType(String name) {
		stack.push(new Slot(typeParamName, Type.getObjectType(name).getDescriptor()));
	}

	@Override
	public SignatureVisitor visitTypeArgument(char wildcard) {
		typeParamName = String.valueOf(wildcard);
		return super.visitTypeArgument(wildcard);
	}

	@Override
	public void visitTypeVariable(String name) {
		if (stack.isEmpty()) {
			cons.accept(new Slot(name));
		} else {
			stack.peek().slotList.add(new Slot(name));
		}
	}

	@Override
	public void visitEnd() {
		Slot slot = stack.pop();
		if (stack.isEmpty()) {
			cons.accept(slot);
		} else {
			stack.peek().slotList.add(slot);
		}
		typeParamName = null;
	}

}
