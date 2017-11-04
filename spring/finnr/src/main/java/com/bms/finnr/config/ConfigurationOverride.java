package com.bms.finnr.config;

import java.lang.reflect.Method;

public class ConfigurationOverride {

	private String name;
	private String value;
	private Class<?> type;
	private Method setterMethod;
	private Method getterMethod;
    public ConfigurationOverride(String name, String value, Class<?> type, Method setterMethod, Method getterMethod) {
        super();
        this.name = name;
        this.value = value;
        this.type = type;
        this.setterMethod = setterMethod;
        this.getterMethod = getterMethod;
    }
    public String getName() {
        return name;
    }
    public String getValue() {
        return value;
    }
    public Class<?> getType() {
        return type;
    }
    public Method getSetterMethod() {
        return setterMethod;
    }
    public Method getGetterMethod() {
        return getterMethod;
    }
    @Override
    public String toString() {
        return "ConfigurationOverride [name=" + name + ", value=" + value + ", type=" + type + ", setterMethod=" + setterMethod + ", getterMethod="
                + getterMethod + "]";
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ConfigurationOverride other = (ConfigurationOverride) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }
	




}
