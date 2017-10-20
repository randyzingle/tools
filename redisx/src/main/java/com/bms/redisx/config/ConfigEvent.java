package com.bms.redisx.config;

import org.springframework.context.ApplicationEvent;

public class ConfigEvent extends ApplicationEvent {
	private static final long serialVersionUID = -9127427755256837021L;
	private String message;
 
    public ConfigEvent(Object source, String message) {
        super(source);
        this.message = message;
    }
    public String getMessage() {
        return message;
    }
}