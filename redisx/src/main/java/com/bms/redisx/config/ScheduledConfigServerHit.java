package com.bms.redisx.config;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledConfigServerHit {
	
	@Autowired
    private ApplicationEventPublisher applicationEventPublisher;
	
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
   	
    // default schedule will have us hitting the config server every 10 min. Override in application.properties. 
    @Scheduled(fixedRateString = "${config.server.request.rate.ms:600000}")
    public void hitConfigServer() {
    	ConfigEvent ce = new ConfigEvent(this, "scheduled config server hit");
		applicationEventPublisher.publishEvent(ce);
        System.out.printf("Hitting the configuration server at %s%n", dateFormat.format(new Date()));
    }
}
