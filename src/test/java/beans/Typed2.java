package beans;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Typed2<T, K, V, M extends Map<K, V>, L extends List<? extends V>, S extends Set<? super V>> {

	private T val;

	private M map;
	private L list;
	private S set;

	private Map<K, V> map2;
	private List<? extends K> list2;
	private Set<? super K> set2;

	public T getVal() {
		return val;
	}

	public void setVal(T val) {
		this.val = val;
	}

	public M getMap() {
		return map;
	}

	public void setMap(M map) {
		this.map = map;
	}

	public L getList() {
		return list;
	}

	public void setList(L list) {
		this.list = list;
	}

	public S getSet() {
		return set;
	}

	public void setSet(S set) {
		this.set = set;
	}

	public Map<K, V> getMap2() {
		return map2;
	}

	public void setMap2(Map<K, V> map2) {
		this.map2 = map2;
	}

	public List<? extends K> getList2() {
		return list2;
	}

	public void setList2(List<? extends K> list2) {
		this.list2 = list2;
	}

	public Set<? super K> getSet2() {
		return set2;
	}

	public void setSet2(Set<? super K> set2) {
		this.set2 = set2;
	}

}
