package com.bms.finnr.scheduled;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.bms.finnr.startup.ConfigServiceRunner;


@Component
public class ScheduledConfigServicePoll {
	
	@Autowired
    private ApplicationEventPublisher applicationEventPublisher;
	
	@Autowired
	private ConfigServiceRunner configServerRunner;
	
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
   	
    // default schedule will have us hitting the config server every 10 min = 600000 ms. Wait 5 min before first run
    // Override in application.properties. 
//    @Scheduled(initialDelay=300000, fixedRateString = "${application.configServerPingRateMs:600000}")
    @Scheduled(initialDelay=30000, fixedRateString = "${application.configServerPingRateMs:600000}")
    public void hitConfigServer() {
        System.out.printf("Hitting the configuration server at %s%n", dateFormat.format(new Date()));
        configServerRunner.refreshProperties();
    }
}