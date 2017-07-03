package com.bms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.bms.properties.KafkaProperties;
import com.bms.simple.SimpleClient;

@SpringBootApplication
public class Sboot1Application {

	public static void main(String[] args) {
		/*
		 * Get the ApplicationContext for the app. You can use this to get:
		 * - Beans
		 * - Messages
		 * - Environment
		 * - Resources
		 */
		ConfigurableApplicationContext ctx = SpringApplication.run(Sboot1Application.class, args);
		System.out.println(ctx.toString());
		ContextRefreshedListener crl = ctx.getBean(ContextRefreshedListener.class);
		//sa.loadfile();
	}
	
	private void loadfile() {	
		Runtime.getRuntime().exit(0);
	}
	
}
