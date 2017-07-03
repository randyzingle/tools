package com.bms.main;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

/**
 * Note once you add a property file to the PropertySources collection the properties
 * inside of it will be available everywhere through the Spring Environment env.getProperty("name")
 * or directly via the annotation @Value("${property}")
 * 
 * @author razing
 *
 */
@SpringBootApplication
@PropertySources({ @PropertySource("classpath:agent.properties") })
public class Application
{
    public static void main(String[] args)
    {
    	ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);
        System.out.println("hello from spring");
    }
}