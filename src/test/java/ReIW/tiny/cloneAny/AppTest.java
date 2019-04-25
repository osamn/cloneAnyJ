package ReIW.tiny.cloneAny;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class AppTest {

	@Test
	public void testGetGreeting() {
		final App app = new App();
		final String result = app.getGreeting();
		assertNotNull("application has a greeting", result);
	}

}
