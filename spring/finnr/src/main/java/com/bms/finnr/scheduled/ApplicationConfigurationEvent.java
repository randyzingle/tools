package com.bms.finnr.scheduled;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import com.bms.finnr.config.ConfigurationOverride;

public class ApplicationConfigurationEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;
    private final List<ConfigurationOverride> overrideList;

    public ApplicationConfigurationEvent(Object source, List<ConfigurationOverride> overrideList) {
        super(source);
        this.overrideList = overrideList;
    }

    public List<ConfigurationOverride> getOverrideList() {
        return overrideList;
    }
    
}
