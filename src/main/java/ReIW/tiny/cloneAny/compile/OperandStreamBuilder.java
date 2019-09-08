package ReIW.tiny.cloneAny.compile;

import ReIW.tiny.cloneAny.pojo.Slot;
import ReIW.tiny.cloneAny.pojo.impl.TypeSlot;
import ReIW.tiny.cloneAny.pojo.impl.TypeSlotBuilder;

public class OperandStreamBuilder {

	public OperandStreamBuilder builder(final Class<?> lhs, final Class<?> rhs) {
		final TypeSlot lhsSlot = TypeSlotBuilder.build(lhs);
		final TypeSlot rhsSlot = TypeSlotBuilder.build(rhs);
		return new OperandStreamBuilder(lhsSlot, rhsSlot);
	}

	private final TypeSlot lhs;
	private final TypeSlot rhs;

	private OperandStreamBuilder(final TypeSlot lhs, final TypeSlot rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
	}

	private static boolean canMove(final Slot lhs, final Slot rhs) {
		// List / Array をまずチェック
		if (lhs.indexed() && rhs.indexed()) {
			return canMove(lhs.slotList.get(0), rhs.slotList.get(0));
		} else if (lhs.indexed() || rhs.indexed()) {
			// 片方だけ indexed だったら変換不可
			return false;
		}

		// Map をチェック
		if (lhs.keyed() && rhs.keyed()) {
			return canMove(lhs.slotList.get(1), rhs.slotList.get(1));
		} else if (lhs.keyed()) {
			return canMove(lhs.slotList.get(1), rhs);
		} else if (rhs.keyed()) {
			return canMove(lhs, rhs.slotList.get(1));
		}

		// 同じものだったら OK
		// generic が違っても一応おっけにしとく
		if (lhs.descriptor.contentEquals(rhs.descriptor)) {
			return true;
		}

		// キャスト操作のチェック
		if (lhs.isPrimitiveType && rhs.isPrimitiveType) {
			// boolean はキャストでほかの primitive に持っていけないので特別扱い
			// 片方だけ真偽値は NG
			if (lhs.descriptor.contentEquals("Z") && rhs.descriptor.contentEquals("Z")) {
				return true;
			} else if (lhs.descriptor.contentEquals("Z") || rhs.descriptor.contentEquals("Z")) {
				return false;
			}
			// それ以外はキャスト可能
			return true;
		}

		// Box 操作のチェック
		if (lhs.isPrimitiveType && rhs.isBoxingType) {
			if (lhs.descriptor.contentEquals("Z") && rhs.descriptor.contentEquals("Ljava/lang/Boolean;")) {
				return true;
			} else if (lhs.descriptor.contentEquals("Z") || rhs.descriptor.contentEquals("Ljava/lang/Boolean;")) {
				return false;
			}
			return true;
		}

		// Unbox 操作のチェック
		if (lhs.isBoxingType && rhs.isPrimitiveType) {
			if (lhs.descriptor.contentEquals("Ljava/lang/Boolean;") && rhs.descriptor.contentEquals("Z")) {
				return true;
			} else if (lhs.descriptor.contentEquals("Ljava/lang/Boolean;") || rhs.descriptor.contentEquals("Z")) {
				return false;
			}
			return true;
		}

		// 左が文字列化可能な場合
		if (lhs.isCharSequence) {
			// 文字列に設定する場合は OK
			if (rhs.descriptor.contentEquals("Ljava/lang/String;")) {
				return true;
			}
			// valueOf で変換できそうなんで OK
			if (rhs.isPrimitiveType || rhs.isBoxingType) {
				return true;
			}
			return false;
		}

		// Object -> Object はがんばる;-)
		return true;
	}

}
