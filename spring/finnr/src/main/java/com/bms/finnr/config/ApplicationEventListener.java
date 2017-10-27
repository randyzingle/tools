package com.bms.finnr.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationEventListener {
	
	@Autowired
	ApplicationConfiguration appConfig;
	
	@EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {
        bigPrint("Context Refreshed");
        System.out.println(appConfig.toString());
    }
	
	@EventListener
	public void handleApplicationReady(ApplicationReadyEvent event) {
		bigPrint("ApplicationReady");
		System.out.println(appConfig.toString());
	}
	
	public static void bigPrint(String message) {
		System.out.println("***********************************");
		System.out.println("*  " + message);
		System.out.println("***********************************");
	}

}
