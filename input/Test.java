import java.io.File;
import java.io.FileOutputStream;

class Test {
	void m1(Object o) {
		int i = o.hashCode();
		if (i > 0)
			o.hashCode();
	}

	void m2(Object o) {
		o.hashCode();
		o.hashCode();
	}

	void m3(int i) {
		if (i < 0) {
			i = -i;
			m(i);
			return;
		}
		else m(i++);
		if (true) {
			i = -i;
			m(i);
			return;
		}
		else i++;
		m(i);
	}
	
	void m(int index) throws Exception {
		for (int i : a)
			a(i);
	}
	
	void testTry(Test t) {
		int i = 0;
		try {
			i = 1;
			t.m(i);
			new FileOutputStream(new File(""));
			t.finalize();
		} catch (Exception | Error e){
			t.n();
			e.printStackTrace();
		} finally {
			t.z(i);
		}
	}
}