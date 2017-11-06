package com.bms.finnr.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.bms.finnr.FinnrApplication;

@Component
public class ApplicationEventListener {
    
    private final static Logger logger = LoggerFactory.getLogger( ApplicationEventListener.class );
	
	@Autowired
	ApplicationConfiguration appConfig;
	
	@Autowired
	GlobalConfiguration globalConfig;

	@EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {
		ConfigUtils.bigPrint("Context Refreshed");
		logger.info(appConfig.toString());
		logger.info(globalConfig.toString());
    }
	
	@EventListener
	public void handleApplicationReady(ApplicationReadyEvent event) {
		ConfigUtils.bigPrint("ApplicationReady");
		logger.info(appConfig.toString());
		logger.info(globalConfig.toString());
	}

}
