package ReIW.tiny.cloneAny;

public interface Ditto<L, R> {

	R clone(L lhs);

	R copyProperties(L lhs, R rhs);

}
