package ReIW.tiny.cloneAny.impl;

import org.objectweb.asm.Type;

import ReIW.tiny.cloneAny.pojo.Slot;

interface Operand {

	/** インスタンスフィールドのロード */
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

	/** インスタンスフィールドに設定 */
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
	final class PropGet implements Operand {
		public final String owner;
		public final String rel;
		public final int size;

		PropGet(final String owner, final String rel, final String clazz) {
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
	final class PropSet implements Operand {
		public final String owner;
		public final String rel;
		public final int size;

		PropSet(final String owner, final String rel, final String clazz) {
			this.owner = owner;
			this.rel = rel;
			this.size = Type.getType(clazz).getSize();
		}

		@Override
		public String toString() {
			return "Prop#" + rel;
		}
	}

	/** Map#get */
	final class MapGet implements Operand {
		public final String name;

		MapGet(final String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return "Map#get \"" + name + "\"";
		}
	}

	// map の場合 get も put も Object が対象になるんで size ==1 固定なんで持たなくてもいいよね

	/** Map#put */
	final class MapPut implements Operand {
		public final String name;

		MapPut(final String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return "Map#put \"" + name + "\"";
		}
	}

	/** 値変換 */
	final class Move implements Operand {
		public final Slot src;
		public final Slot dst;

		Move(final Slot src, final Slot dst) {
			this.src = src;
			this.dst = dst;
		}

		@Override
		public String toString() {
			return "Move " + src.typeClass + " -> " + dst.typeClass;
		}
	}

	/** コンストラクタ引数用の値変換 */
	final class Push implements Operand {
		public final Slot src;
		public final Slot dst;

		Push(final Slot src, final Slot dst) {
			this.src = src;
			this.dst = dst;
		}

		@Override
		public String toString() {
			return "Push " + src.typeClass + " -> " + dst.typeClass;
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
