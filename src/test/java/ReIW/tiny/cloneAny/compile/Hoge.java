package ReIW.tiny.cloneAny.compile;

import java.util.ArrayList;
import java.util.List;

class Hoge {

	static class Foo {
		String getA() {
			return "";
		}

		Integer getB() {
			return 0;
		}

		List<Long> values;
	}

	static class Bar {
		Bar(String a, int b) {

		}

		void setValues(long[] val) {
		}

		List<Long> values;
	}

	public Bar copy(Foo foo, Bar bar) {
		// ctor
		if (bar == null) {
			bar = new Bar(foo.getA(), foo.getB());
		}

		// list -> array
		final List<Long> list = foo.values;
		final long[] val = new long[list.size()];
		for (int i = 0; i < foo.values.size(); i++) {
			val[i] = list.get(i);
		}
		bar.setValues(val);

		// list -> list
		final List<Long> list1 = foo.values;
		final List<Long> val1 = new ArrayList<Long>();
		for (int i = 0; i < list1.size(); i++) {
			val1.add(list1.get(i));
		}
		bar.values = val1;

		return bar;
	}

}
