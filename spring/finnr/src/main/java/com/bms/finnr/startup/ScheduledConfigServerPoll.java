package com.bms.finnr.startup;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
public class ScheduledConfigServerPoll {
	
	@Autowired
    private ApplicationEventPublisher applicationEventPublisher;
	
	@Autowired
	private ConfigServerRunner configServerRunner;
	
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
   	
    // default schedule will have us hitting the config server every 10 min = 600000 ms. 
    // Override in application.properties. 
    @Scheduled(fixedRateString = "${application.configServerPingRateMs:600000}")
    public void hitConfigServer() {
//    		ConfigEvent ce = new ConfigEvent(this, "scheduled config server hit");
//		applicationEventPublisher.publishEvent(ce);
        System.out.printf("Hitting the configuration server at %s%n", dateFormat.format(new Date()));
        configServerRunner.loadApplicationProperties();
    }
}