package ReIW.tiny.cloneAny.impl;

import org.objectweb.asm.Type;

import ReIW.tiny.cloneAny.pojo.Slot;

interface Operand {

	/** フィールド読取 */
	final class Load implements Operand {
		public final String owner;
		public final String name;
		public final int size;

		Load(final String owner, final String name, final String clazz) {
			this.owner = owner;
			this.name = name;
			this.size = Type.getType(clazz).getSize();
		}

		@Override
		public String toString() {
			return "Load \"" + name + "\"";
		}
	}

	/** フィールド格納 */
	final class Store implements Operand {
		public final String owner;
		public final String name;
		public final int size;

		Store(final String owner, final String name, final String clazz) {
			this.owner = owner;
			this.name = name;
			this.size = Type.getType(clazz).getSize();
		}

		@Override
		public String toString() {
			return "Store \"" + name + "\"";
		}
	}

	/** プロパティ取得 */
	final class Get implements Operand {
		public final String owner;
		public final String rel;
		public final int size;

		Get(final String owner, final String rel, final String clazz) {
			this.owner = owner;
			this.rel = rel;
			this.size = Type.getType(clazz).getSize();
		}

		@Override
		public String toString() {
			return "Prop#" + rel;
		}
	}

	/** プロパティ設定 */
	final class Set implements Operand {
		public final String owner;
		public final String rel;
		public final int size;

		Set(final String owner, final String rel, final String clazz) {
			this.owner = owner;
			this.rel = rel;
			this.size = Type.getType(clazz).getSize();
		}

		@Override
		public String toString() {
			return "Prop#" + rel;
		}
	}

	// map の場合 get も put も Object が対象になるんで size ==1 固定なんで持たなくてもいいよね

	/** Map#get */
	final class GetKey implements Operand {
		public final String name;

		GetKey(final String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return "Map#get \"" + name + "\"";
		}
	}

	/** Map#put */
	final class SetKey implements Operand {
		public final String name;

		SetKey(final String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return "Map#put \"" + name + "\"";
		}
	}

	/** Stack top の値変換 */
	final class ConvTop implements Operand {
		public final Slot src;
		public final Slot dst;

		ConvTop(final Slot src, final Slot dst) {
			this.src = src;
			this.dst = dst;
		}

		@Override
		public String toString() {
			return "Conv " + src.descriptor + " -> " + dst.descriptor;
		}
	}

	/** コンストラクタ呼び出し */
	final class Ctor implements Operand {
		public final String descriptor;

		Ctor(final String descriptor) {
			this.descriptor = descriptor;
		}

		@Override
		public String toString() {
			return "<init>" + descriptor;
		}
	}

}
