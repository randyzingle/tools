package com.bms.cd;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.bms.Application;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(Application.class)
public class CDPlayerTest {
	
	@Autowired
	private CDPlayer cdp;
	
	@Test
	public void cdShouldNotBeNull() {
		cdp.play();
		assertNotNull(cdp);
	}
	
	@Test
	public void contextLoads() {
		
	}
}
