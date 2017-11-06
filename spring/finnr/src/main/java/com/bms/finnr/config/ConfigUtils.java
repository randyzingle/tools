package com.bms.finnr.config;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bms.finnr.FinnrApplication;

public class ConfigUtils {
    
    private final static Logger logger = LoggerFactory.getLogger( ConfigUtils.class );

	public static void bigPrint(String message) {
		System.out.println("***********************************");
		System.out.println("*  " + message);
		System.out.println("***********************************");
	}
	
    public static synchronized void setValue(Configuration config, Method setMethod, String type, String value) {
        try {
            if (type.equals(String.class.getName())) {
                setMethod.invoke(config, value);
            } else if (type.equals(Boolean.class.getName()) || type.equals("boolean")) {
                setMethod.invoke(config, Boolean.valueOf(value));
            } else if (type.equals(Byte.class.getName()) || type.equals("byte")) {
                setMethod.invoke(config, Byte.valueOf(value));
            } else if (type.equals(Double.class.getName()) || type.equals("double")) {
                setMethod.invoke(config, Double.valueOf(value));
            } else if (type.equals(Float.class.getName()) || type.equals("float")) {
                setMethod.invoke(config, Float.valueOf(value));
            } else if (type.equals(Integer.class.getName()) || type.equals("int")) {
                setMethod.invoke(config, Integer.valueOf(value));
            } else if (type.equals(Long.class.getName()) || type.equals("long")) {
                setMethod.invoke(config, Long.valueOf(value));
            } else if (type.equals(Short.class.getName()) || type.equals("short")) {
                setMethod.invoke(config, Short.valueOf(value));
            }
        } catch (InvocationTargetException | IllegalAccessException ex) {
            logger.error(ex.getMessage());
        }
    }

    public static synchronized String normalizeName(String s) {
        if (s == null)
            return s;
        return s.toUpperCase().replaceAll("_", "").replaceAll("-", "").replaceAll("\\.", "");
    }
	
}