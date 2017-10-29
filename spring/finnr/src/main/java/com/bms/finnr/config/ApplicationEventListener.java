package com.bms.finnr.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationEventListener {
	
	@Autowired
	ApplicationConfiguration appConfig;
	
	@Autowired
	GlobalConfiguration globalConfig;

	@EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {
		ConfigUtils.bigPrint("Context Refreshed");
        System.out.println(appConfig.toString());
        System.out.println(globalConfig.toString());
    }
	
	@EventListener
	public void handleApplicationReady(ApplicationReadyEvent event) {
		ConfigUtils.bigPrint("ApplicationReady");
		System.out.println(appConfig.toString());
		System.out.println(globalConfig.toString());
	}

}
