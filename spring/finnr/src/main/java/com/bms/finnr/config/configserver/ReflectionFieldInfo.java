package com.bms.finnr.config.configserver;

public class ReflectionFieldInfo {
    public String name;
    public String setMethod;
    public String getMethod;
    public Class<?> type;

    public ReflectionFieldInfo(String name, Class<?> type) {
        this.name = name;
        this.type = type;
        char[] c = name.toCharArray();
        if (c.length > 0)
            c[0] = Character.toUpperCase(c[0]);
        this.setMethod = "set" + String.valueOf(c);
        if (type.getName().equals(Boolean.class.getName()) || type.getName().equals("boolean")) {
            this.getMethod = "is" + String.valueOf(c);
        } else {
            this.getMethod = "get" + String.valueOf(c);
        }
    }

    @Override
    public String toString() {
        return "FType [name=" + name + ", setMethod=" + setMethod + ", getMethod=" + getMethod + ", type=" + type + "]";
    }

}
