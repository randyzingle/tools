package com.bms.finnr.config;

import java.util.HashMap;

public class Configuration {

    HashMap<String, ConfigurationOverride> originalProperties = new HashMap<>();
    HashMap<String, ConfigurationOverride> oldOverrides = new HashMap<>();
    HashMap<String, ConfigurationOverride> newOverrides = new HashMap<>();
    
    public HashMap<String, ConfigurationOverride> getOldOverrides() {
        return oldOverrides;
    }
    public void setOldOverrides(HashMap<String, ConfigurationOverride> oldOverrides) {
        this.oldOverrides = oldOverrides;
    }
    public HashMap<String, ConfigurationOverride> getNewOverrides() {
        return newOverrides;
    }
    public void setNewOverrides(HashMap<String, ConfigurationOverride> newOverrides) {
        this.newOverrides = newOverrides;
    }
    public HashMap<String, ConfigurationOverride> getOriginalProperties() {
        return originalProperties;
    }
    public void setOriginalProperties(HashMap<String, ConfigurationOverride> originalProperties) {
        this.originalProperties = originalProperties;
    }
    
}
