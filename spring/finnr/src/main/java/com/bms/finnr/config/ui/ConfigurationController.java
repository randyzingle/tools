package com.bms.finnr.config.ui;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.bms.finnr.config.ApplicationConfiguration;
import com.bms.finnr.config.ApplicationConstants;
import com.bms.finnr.config.ConfigUtils;
import com.bms.finnr.config.GlobalConfiguration;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class ConfigurationController {
    
    private final static Logger logger = LoggerFactory.getLogger(ConfigurationController.class);
    
    private static String APP_ENV = ApplicationConstants.PSN_SYSTEM_ENVIRONMENT + "-application";
    private static String APP_PRO = ApplicationConstants.PSN_SYSTEM_PROPERTIES + "-application";
    private static String GLO_ENV = ApplicationConstants.PSN_SYSTEM_ENVIRONMENT + "-global";
    private static String GLO_PRO = ApplicationConstants.PSN_SYSTEM_PROPERTIES + "-global";
    
    @Autowired
    Environment env;
    
    @Autowired
    ApplicationConfiguration appConf;
    
    @RequestMapping(value = "/run", method = RequestMethod.GET)
    public String run() {
        String s = dumpFullPropertySourceData();
        return s;
    }
    
    @RequestMapping(value = "/props", method = RequestMethod.GET)
    public String props() {
        String s = getPropertySources();
        return s;
    }
    
    private String getPropertySources() {
        String s = "";
        HashMap<String, TreeMap<String, String>> pmap = getPropertySourceMap();
        TreeMap<String, PropTuple> orderMap;
        FullDisplayConfig fdc = new FullDisplayConfig();
        fdc.component = appConf.getComponentName();
        
        // get application-specific properties
        orderMap = new TreeMap<>();
        loadTuples(orderMap, pmap.get(ApplicationConstants.PSN_APPLICATION_CONFIG), ApplicationConstants.PSN_APPLICATION_CONFIG);
        loadTuples(orderMap, pmap.get(APP_ENV), APP_ENV);
        loadTuples(orderMap, pmap.get(APP_PRO), APP_PRO);
        loadTuples(orderMap, pmap.get(ApplicationConstants.PSN_CONFIG_SERVER_APPLICATION), ApplicationConstants.PSN_CONFIG_SERVER_APPLICATION);
        SubDisplayConfig application = getSubDisplayConfig(orderMap, "Application-Specific Configuration");
        fdc.application = application;
        
        // get tier-global properties
        orderMap = new TreeMap<>();
        loadTuples(orderMap, pmap.get(ApplicationConstants.PSN_CONFIG_SERVER_GLOBAL), ApplicationConstants.PSN_CONFIG_SERVER_GLOBAL);
        loadTuples(orderMap, pmap.get(ApplicationConstants.PSN_TIER_GLOBAL_OVERRIDES), ApplicationConstants.PSN_TIER_GLOBAL_OVERRIDES);
        loadTuples(orderMap, pmap.get(GLO_ENV), GLO_ENV);
        loadTuples(orderMap, pmap.get(GLO_PRO), GLO_PRO);
        SubDisplayConfig global = getSubDisplayConfig(orderMap, "Tier-Global Configuration");
        fdc.global = global;
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        try {
            s = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(fdc);
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            logger.error(e.getMessage());
        }
        logger.debug(s);
        
        return s;
    }
    
    private SubDisplayConfig getSubDisplayConfig(TreeMap<String, PropTuple> orderMap, String title) {
        SubDisplayConfig sdc = new SubDisplayConfig();
        sdc.title = title;
        List<PropTuple> plist = new ArrayList<>();
        Set<String> keys = orderMap.keySet();
        for (String key: keys) {
            plist.add(orderMap.get(key));
        }
        sdc.items = plist;
        return sdc;
    }
    
    private void loadTuples(TreeMap<String, PropTuple> orderMap, TreeMap<String, String> props, String source) {
        Set<String> keys = props.keySet();
        for (String key: keys) {
            PropTuple pt = new PropTuple(key, props.get(key), source);
            orderMap.put(key, pt);
        }
    }
    
    private String dumpFullPropertySourceData() {
        HashMap<String, TreeMap<String, String>> pmap = getPropertySourceMap();
        ObjectMapper mapper = new ObjectMapper();
        String jsonResult = "hello";
        try {
            jsonResult = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(pmap);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
        }
        logger.debug(jsonResult);
        return jsonResult;
    }
    
    private HashMap<String, TreeMap<String, String>> getPropertySourceMap() {
        HashMap<String, TreeMap<String, String>> pmap = new HashMap<>();     
        TreeMap<String, String> appConfigMap = new TreeMap<>();
        TreeMap<String, String> tgoConfigMap = new TreeMap<>();
        TreeMap<String, String> csaConfigMap = new TreeMap<>();
        TreeMap<String, String> csgConfigMap = new TreeMap<>();
        TreeMap<String, String> appEnvConfigMap = new TreeMap<>();
        TreeMap<String, String> appSysConfigMap = new TreeMap<>();
        TreeMap<String, String> gloEnvConfigMap = new TreeMap<>();
        TreeMap<String, String> gloSysConfigMap = new TreeMap<>();
        pmap.put(ApplicationConstants.PSN_APPLICATION_CONFIG, appConfigMap);
        pmap.put(ApplicationConstants.PSN_TIER_GLOBAL_OVERRIDES, tgoConfigMap);
        pmap.put(ApplicationConstants.PSN_CONFIG_SERVER_APPLICATION, csaConfigMap);
        pmap.put(ApplicationConstants.PSN_CONFIG_SERVER_GLOBAL, csgConfigMap);
        pmap.put(APP_ENV, appEnvConfigMap);
        pmap.put(APP_PRO, appSysConfigMap);
        pmap.put(GLO_ENV, gloEnvConfigMap);
        pmap.put(GLO_PRO, gloSysConfigMap);
        ConfigurableEnvironment ce = (ConfigurableEnvironment) env;
        MutablePropertySources sources = ce.getPropertySources();
        ConfigUtils.bigPrint("Property Sources");
        int size = sources.size();
        System.out.println("Number of property sources: " + size);
        Class capp = ApplicationConfiguration.class;
        Field[] fapp = capp.getDeclaredFields();
        Class cglo = GlobalConfiguration.class;
        Field[] fglo = cglo.getDeclaredFields();
        List<Field> applist = new ArrayList<>();
        List<Field> glolist = new ArrayList<>();
        applist.addAll(Arrays.asList(fapp));
        glolist.addAll(Arrays.asList(fglo));
        for (PropertySource ps : sources) {
            String name = ps.getName();
            System.out.println("Property source name: " + name);
            for (Field f : applist) {
                String s = f.getName();              
                String prop = null;
                if (ps.getName().equals(ApplicationConstants.PSN_APPLICATION_CONFIG)) {
                    String ss = ApplicationConstants.APPLICATION_PROPERTY_PREFIX + s;
                    prop = (String) ps.getProperty(ss);
                    if (prop != null) {
                        System.out.println("    " + s + ": " + ss + ": " + prop);
                        appConfigMap.put(s, prop);
                    }
                
                } else if (ps.getName().equals(ApplicationConstants.PSN_CONFIG_SERVER_APPLICATION)) {
                    prop = (String) ps.getProperty(s);
                    if (prop != null) {
                        System.out.println("    " + s + ": " + s + ": " + prop);
                        csaConfigMap.put(s, prop);
                    }
                } else if (ps.getName().equals(ApplicationConstants.PSN_SYSTEM_ENVIRONMENT)) {
                    String ss = convertToEnv(s);
                    prop = (String) ps.getProperty(ss);
                    if (prop != null) {
                        System.out.println("    " + s + ": " + ss + ": " + prop);
                        appEnvConfigMap.put(s, prop);
                    }
                } else if (ps.getName().equals(ApplicationConstants.PSN_SYSTEM_PROPERTIES)) {
                    String ss = ApplicationConstants.APPLICATION_PROPERTY_PREFIX + s;
                    prop = (String) ps.getProperty(ss);
                    if (prop != null) {
                        System.out.println("    " + s + ": " + ss + ": " + prop);
                        appSysConfigMap.put(s, prop);
                    }
                }
            }
            for (Field f : glolist) {
                String s = f.getName();              
                String prop = null;
                if (ps.getName().equals(ApplicationConstants.PSN_TIER_GLOBAL_OVERRIDES)) {
                    String ss = ApplicationConstants.GLOBAL_PROPERTY_PREFIX+ s;
                    prop = (String) ps.getProperty(ss);
                    if (prop != null) {
                        System.out.println("    " + s + ": " + ss + ": " + prop);
                        tgoConfigMap.put(s, prop);
                    }
                } else if (ps.getName().equals(ApplicationConstants.PSN_CONFIG_SERVER_GLOBAL)) {
                    prop = (String) ps.getProperty(s);
                    if (prop != null) {
                        System.out.println("    " + s + ": " + s + ": " + prop);
                        csgConfigMap.put(s, prop);
                    }
                } else if (ps.getName().equals(ApplicationConstants.PSN_SYSTEM_ENVIRONMENT)) {
                    String ss = convertToEnv(s);
                    prop = (String) ps.getProperty(ss);
                    if (prop != null) {
                        System.out.println("    " + s + ": " + ss + ": " + prop);
                        gloEnvConfigMap.put(s, prop);
                    }
                } else if (ps.getName().equals(ApplicationConstants.PSN_SYSTEM_PROPERTIES)) {
                    String ss = ApplicationConstants.APPLICATION_PROPERTY_PREFIX + s;
                    prop = (String) ps.getProperty(ss);
                    if (prop != null) {
                        System.out.println("    " + s + ": " + ss + ": " + prop);
                        gloSysConfigMap.put(s, prop);
                    }
                }
            }
        }
        return pmap;
    }

    private String convertToEnv(String s) {
        char[] cs = s.toCharArray();
        StringBuffer buff = new StringBuffer();
        buff.append("APPLICATION_");
        for (char c : cs) {
            if (Character.isUpperCase(c)) {
                buff.append("_");
                buff.append(c);
            } else if (c == '.') {
                buff.append("_");
            } else {
                buff.append(Character.toUpperCase(c));
            }
        }
        return buff.toString();
    }
    
    private class PropTuple {
        private final String name;
        private final String value;
        private final String source;
        private PropTuple(String name, String value, String source) {
            this.name = name;
            this.value = value;
            this.source = source;
        }
    }
    
    private class SubDisplayConfig {
        private String title;
        private List<PropTuple> items = new ArrayList<>();
    }
    
    private class FullDisplayConfig {
        private String component;
        private SubDisplayConfig application;
        private SubDisplayConfig global;
    }
}
