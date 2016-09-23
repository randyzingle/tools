package com.bms;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/*
 * In a typical Spring integration test, you'd annotate the test class with @ContextConfiguration to
 * specify how the tests should load the Spring application context. 
 * 
 * The SpringApplicationConfiguration annotation loads the context in a spring boot way from the
 * Sboot1Application configuration class. 
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SuppressWarnings(value = { "deprecation" })
@SpringApplicationConfiguration(classes = Sboot1Application.class)
@WebAppConfiguration
public class Sboot1ApplicationTests {

	// this will pass if the application context loads
	@Test
	public void contextLoads() {
	}

}
