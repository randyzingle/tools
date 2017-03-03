package com.bms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Sboot1Application {

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(Sboot1Application.class, args);
		PathHolderBean phb = ctx.getBean(PathHolderBean.class);
		System.out.println("FROM PATHHOLDER: " + phb.getJarPath());
		Sboot1Application sa = new Sboot1Application();
		sa.loadfile();
	}
	
	private void loadfile() {		
		Runtime.getRuntime().exit(0);
	}
	
}
