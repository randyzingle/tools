package com.bms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @SpringBootApplication is equivalent to
 * @Configuration - designates a class as a configuration class
 * @ComponentScan - enables component scanning -> Beans are automatically discovered and registered in the Spring Application Context
 * @EnableAutoConfiguration - searches the classpath and autoconfigures dbs, queues, etc based on what it finds
 * 
 * @author razing
 *
 */
@SpringBootApplication
public class Application {
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
