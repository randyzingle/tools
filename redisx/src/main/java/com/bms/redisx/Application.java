package com.bms.redisx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.bms.redisx.config.ApplicationConfigProperties;
import com.bms.redisx.config.BaseConfigEngine;
import com.bms.redisx.config.TierGlobalConfigProperties;

@SpringBootApplication
public class Application {
	
	public static void main(String[] args) {
			
		ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);
		
		System.out.println(ctx.getBeanDefinitionCount());
		BaseConfigEngine bce = ctx.getBean(BaseConfigEngine.class	);
		bce.disectProps();
		
		ApplicationConfigProperties acp = ctx.getBean(ApplicationConfigProperties.class);
		System.out.println(acp);
		
		TierGlobalConfigProperties tgcp = ctx.getBean(TierGlobalConfigProperties.class);
		System.out.println(tgcp);
		
	}
}
