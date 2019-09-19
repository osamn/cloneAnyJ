package ReIW.tiny.cloneAny.compile;

import java.util.Objects;

import ReIW.tiny.cloneAny.pojo.TypeDef;

final class CKey {

	final TypeDef lhs;
	final TypeDef rhs;

	private final int hash;

	CKey(final TypeDef lhs, final TypeDef rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
		// 各スロットはここに来た時点でもう決定済み
		// なので hash 値は構築時ですべて決定できるはず
		hash = Objects.hash(lhs.getSignaturedName(), rhs.getSignaturedName());
	}
	
	String getClassName() {
		return "$ditto." + lhs.getSignaturedName() + "." + rhs.getSignaturedName();
	}

	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CKey other = (CKey) obj;
		if (lhs == null) {
			if (other.lhs != null)
				return false;
		} else if (!lhs.getSignaturedName().contentEquals(other.lhs.getSignaturedName()))
			return false;
		if (rhs == null) {
			if (other.rhs != null)
				return false;
		} else if (!rhs.getSignaturedName().contentEquals(other.rhs.getSignaturedName()))
			return false;
		return true;
	}
}