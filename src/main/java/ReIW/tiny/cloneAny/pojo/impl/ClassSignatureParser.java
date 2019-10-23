package ReIW.tiny.cloneAny.pojo.impl;

import java.util.Arrays;
import java.util.function.Consumer;

import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

class ClassSignatureParser extends SlotLikeSignatureParser {

	final Consumer<SlotValue> superCons;

	ClassSignatureParser(Consumer<SlotValue> formalParamCons, Consumer<SlotValue> superCons) {
		this.superCons = superCons;
		// 先に自クラスの型パラメタが読み取られるので
		// その cons をいれておく
		super.slotCons = formalParamCons;
	}

	void parse(final String superName, final String[] interfaces, final String signature) {
		// superName とか interfaces の中身とかに配列 '[' がくることはないよ
		// なんで signature のありなしだけで判断する
		if (signature == null) {
			// 自クラスも継承元も non generic なんで子要素なし
			superCons.accept(newSlotLike(null, null, Type.getObjectType(superName).getDescriptor()));
			if (interfaces != null) {
				Arrays.stream(interfaces)
						.map(intfName -> newSlotLike(null, null, Type.getObjectType(intfName).getDescriptor()))
						.forEach(superCons);
			}
		} else {
			new SignatureReader(signature).accept(this);
		}
	}

	@Override
	public SignatureVisitor visitSuperclass() {
		// こっから継承元に入るので cons を super 用に切り替える
		super.slotCons = superCons;
		return super.visitSuperclass();
	}

	@Override
	protected SlotValue newSlotLike(final String wildcard, final String typeParam, final String descriptor) {
		return new SlotValue(wildcard, typeParam, descriptor);
	}

}
