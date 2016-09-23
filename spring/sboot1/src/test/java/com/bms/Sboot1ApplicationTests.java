package com.bms;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

import com.bms.properties.AmazonProperties;

/*
 * Unit tests can be written as normal JUnit tests. Integration tests will have to understand and load
 * some form of the Spring application context. 
 * 
*/

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT) // bootstrap w SpringBoot, load application.properties
public class Sboot1ApplicationTests {

	// this will pass if the application context loads
	@Test
	public void contextLoads() {
	}
	
	// check to see what's in the app context
	@Autowired
	private AmazonProperties amazonProperties;
	
	@Test
	public void testAmazonProperties() {
		assertEquals("baldur1", amazonProperties.getAmazonID());
		assertEquals("secretstuff", amazonProperties.getPassword());
		assertEquals("www.amazon.com", amazonProperties.getUrl());
	}
	

	

}
