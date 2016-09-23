package com.bms;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/*
 * Unit tests can be written as normal JUnit tests. Integration tests will have to understand and load
 * some form of the Spring application context. 
 * 
 * In a typical Spring integration test, you'd annotate the test class with @ContextConfiguration to
 * specify how the tests should load the Spring application context. 
 * 
 * The @SpringApplicationConfiguration annotation loads the context in a spring boot way from the
 * Sboot1Application configuration class. 
 * 
 * The SpringJUnit4ClassRunner helps load a Spring application context in JUnit-based application tests
 * and enables autowiring of beans into the test class. 
 */

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes=Sboot1Application.class)
public class Sboot1ApplicationTests {

	// this will pass if the application context loads
	@Test
	public void contextLoads() {
	}

}
