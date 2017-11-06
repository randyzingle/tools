package com.bms.finnr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class FinnrApplication {
    
    private final static Logger logger = LoggerFactory.getLogger( FinnrApplication.class );

	public static void main(String[] args) {    	    
		ApplicationContext ctx = SpringApplication.run(FinnrApplication.class, args);	
		//org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext
		logger.info("ApplicationContext brought to you by BMS: " + ctx.getClass());
	}
}
