package com.bms.finnr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class FinnrApplication {

	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(FinnrApplication.class, args);
		
		//org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext
		System.out.println("ApplicationContext: " + ctx.getClass());
	}
}
