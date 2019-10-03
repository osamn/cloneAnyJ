package ReIW.tiny.cloneAny.compile;

import java.util.List;

class Hoge {
	
	static class Foo {
		String getA() { return "";}
		Integer getB() { return 0;}
		
		List<Long> values;
	}
	
	static class Bar {
		Bar(String a, int b) {
			
		}
		
		void setValues(long[] val) {}
	}
	
	public Bar copy(Foo foo, Bar bar) {
		if (bar == null) {
			bar = new Bar(foo.getA(), foo.getB());
		}
		
		final long[] val = new long[foo.values.size()];
		for (int i=0; i < foo.values.size(); i++) {
			val[i] = foo.values.get(i);
		}
		bar.setValues(val);
		return bar;
	}
	


}
