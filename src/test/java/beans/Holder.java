package beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Holder {

	private Typed1<Bean1, Bean2> foo;
	private Typed2<Bean1, String, Bean2, HashMap<String, Bean2>, ArrayList<Bean2>, HashSet<Bean2>> hoge;

	public Typed1<Bean1, Bean2> getFoo() {
		return foo;
	}

	public void setFoo(Typed1<Bean1, Bean2> foo) {
		this.foo = foo;
	}

	public Typed2<Bean1, String, Bean2, HashMap<String, Bean2>, ArrayList<Bean2>, HashSet<Bean2>> getHoge() {
		return hoge;
	}

	public void setHoge(Typed2<Bean1, String, Bean2, HashMap<String, Bean2>, ArrayList<Bean2>, HashSet<Bean2>> hoge) {
		this.hoge = hoge;
	}

	// -----------------------------------------

	public Bean1 getVal() {
		return hoge.getVal();
	}

	public void setVal(Bean1 val) {
		hoge.setVal(val);
	}

	public HashMap<String, Bean2> getMap() {
		return hoge.getMap();
	}

	public void setMap(HashMap<String, Bean2> map) {
		hoge.setMap(map);
	}

	public ArrayList<Bean2> getList() {
		return hoge.getList();
	}

	public void setList(ArrayList<Bean2> list) {
		hoge.setList(list);
	}

	public HashSet<Bean2> getSet() {
		return hoge.getSet();
	}

	public void setSet(HashSet<Bean2> set) {
		hoge.setSet(set);
	}

	public Map<String, Bean2> getMap2() {
		return hoge.getMap2();
	}

	public void setMap2(Map<String, Bean2> map2) {
		hoge.setMap2(map2);
	}

	public List<? extends String> getList2() {
		return hoge.getList2();
	}

	public void setList2(List<? extends String> list2) {
		hoge.setList2(list2);
	}

	public Set<? super String> getSet2() {
		return hoge.getSet2();
	}

	public void setSet2(Set<? super String> set2) {
		hoge.setSet2(set2);
	}

}
