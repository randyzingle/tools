package com.bms.finnr.startup;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

import com.bms.finnr.config.ApplicationConfiguration;
import com.bms.finnr.config.ApplicationConstants;
import com.bms.finnr.config.ConfigUtils;
import com.bms.finnr.config.Configuration;
import com.bms.finnr.config.ConfigurationOverride;
import com.bms.finnr.config.GlobalConfiguration;
import com.bms.finnr.config.configserver.ConfigProperty;
import com.bms.finnr.config.configserver.ConfigServerClient;
import com.bms.finnr.scheduled.ApplicationConfigurationEvent;

/**
 * This class loads application specific and tier_global properties from the Configuration Service. The method, public void run(ApplicationArguments args), is
 * called once the Spring application context is fully loaded so all of the configuration properties will have been loaded from property files and the
 * environment, by the time this runs.
 *
 * The Configuration Server is queried with tierNm=appConfig.getTierName() and componentNm=appConfig.getComponentName(), to get back a list of all properties
 * for that tier/component.
 *
 * Defaults for all application specific properties are given in application.properties. These will be overridden by environment variables, system properties,
 * and configuration server values (for the given tierNm and componentNm). We are assuming that values for application-specific properties are only set in the
 * config server as overrides or features flags which is why that has top precedence.
 *
 * The order of precedence for Application properties is (top one wins):
 * <ol>
 * <li>Configuration Server value</li>
 * <li>System Property (-Dname=value pair given on java command line)</li>
 * <li>OS Environment variable</li>
 * <li>application.properties</li>
 * </ol>
 *
 * The order of precedence for Global properties is (top one wins):
 * <ol>
 * <li>System Property (-Dname=value pair given on java command line)</li>
 * <li>OS Environment variable</li>
 * <li>application.properties</li>
 * <li>Configuration Server value</li>
 * </ol>
 *
 * When running in EC2 the URL for the configuration server and the tierName must be passed in as environment variables. When running locally you can use
 * environment variables or set the values in application.properties.
 *
 * This is the first ApplicationRunner that runs (getOrder() = 0)
 *
 * @author razing
 *
 */
@Component
public class ConfigServiceRunner implements ApplicationRunner, Ordered {

    // private HashMap<String, String> tempMap = new HashMap<>();

    @Autowired
    Environment env;

    @Autowired
    ApplicationEventPublisher publisher;

    @Autowired
    ApplicationConfiguration appConfig;

    @Autowired
    GlobalConfiguration globalConfig;

    @Autowired
    ConfigServerClient configClient;

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ConfigUtils.bigPrint("in ConfigServerRunner");
        loadApplicationProperties();
        loadTierGlobalProperties();
    }

    public void refreshProperties() {
        long startTime = System.currentTimeMillis();
        List<ConfigurationOverride> masterList = new ArrayList<>();
        masterList.addAll(refreshApplicationProperties());
        masterList.addAll(refreshGlobalProperties());
        // let interested parties know if we have a changes to some configuration properties
        if (!masterList.isEmpty()) {
            ApplicationConfigurationEvent ace = new ApplicationConfigurationEvent(this, masterList);
            publisher.publishEvent(ace);
        }
        long endTime = System.currentTimeMillis();
        // ConfigUtils.bigPrint("Total runtime: " + (endTime - startTime) + " ms");
        ConfigUtils.bigPrint(appConfig.toString());
        ConfigUtils.bigPrint(globalConfig.toString());
    }

    public List<ConfigurationOverride> refreshApplicationProperties() {
        String tierName = appConfig.getTierName();
        String componentName = appConfig.getComponentName();
        String name = "";
        String auditComponent = appConfig.getComponentName();
        Map<String, Object> csmap = new HashMap<>();
        List<ConfigurationOverride> overrideList = updateApplicationConfig(appConfig, csmap, tierName, componentName, name, auditComponent);
        ConfigurableEnvironment ce = (ConfigurableEnvironment) env;
        MutablePropertySources sources = ce.getPropertySources();
        sources.replace(ApplicationConstants.PSN_CONFIG_SERVER_APPLICATION, new MapPropertySource(ApplicationConstants.PSN_CONFIG_SERVER_APPLICATION, csmap));
        return overrideList;
    }

    public List<ConfigurationOverride> refreshGlobalProperties() {
        String tierName = "tier_global";
        String componentName = "";
        String name = "";
        String auditComponent = appConfig.getComponentName();

        Map<String, Object> csmap = new HashMap<>();
        List<ConfigurationOverride> overrideList = new ArrayList<>();

        // Get whatever Service Discovery info we need
        componentName = ApplicationConstants.COMPONENTNM_SERVICE_DISCOVERY;
        overrideList.addAll(updateGlobalConfig(globalConfig, csmap, tierName, componentName, name, auditComponent));

        // Get whatever Kafka info we need
        componentName = ApplicationConstants.COMPONENTNM_MKT_KAFKA;
        overrideList.addAll(updateGlobalConfig(globalConfig, csmap, tierName, componentName, name, auditComponent));

        // Get whatever Redis info we need
        componentName = ApplicationConstants.COMPONENTNM_AWS_REDIS;
        overrideList.addAll(updateGlobalConfig(globalConfig, csmap, tierName, componentName, name, auditComponent));

        // Get whatever S3 Bucket info we need
        componentName = ApplicationConstants.COMPONENTNM_AWS_S3_BUCKETS;
        overrideList.addAll(updateGlobalConfig(globalConfig, csmap, tierName, componentName, name, auditComponent));

        ConfigurableEnvironment ce = (ConfigurableEnvironment) env;
        MutablePropertySources sources = ce.getPropertySources();
        sources.replace(ApplicationConstants.PSN_CONFIG_SERVER_GLOBAL, new MapPropertySource(ApplicationConstants.PSN_CONFIG_SERVER_GLOBAL, csmap));
        return overrideList;
    }

    private List<ConfigurationOverride> updateGlobalConfig(Configuration config, Map<String, Object> csmap, String tierName, String componentName, String name,
            String auditComponent) {
        ConfigurableEnvironment ce = (ConfigurableEnvironment) env;
        PropertySource<?> tierGlobalPropertySource = ce.getPropertySources().get(ApplicationConstants.PSN_TIER_GLOBAL_OVERRIDES);
        List<ConfigProperty> cp = configClient.getProperties(tierName, componentName, name, auditComponent);
        // get the list of fields
        Class<?> c = config.getClass();
        Field[] fields = c.getDeclaredFields();
        HashMap<String, FType> fieldMap = new HashMap<>(fields.length);
        for (Field f : fields) {
            FType ftype = new FType(f.getName(), f.getType());
            fieldMap.put(normalizeName(f.getName()), ftype);
            // System.out.println("### " + f.getName() + " : " + f.getType());
        }
        HashMap<String, ConfigurationOverride> originalProperties = config.getOriginalProperties();
        HashMap<String, ConfigurationOverride> oldo = config.getOldOverrides();
        HashMap<String, ConfigurationOverride> newo = new HashMap<>();
        for (ConfigProperty cps : cp) {
            String n = normalizeName(cps.getName());
            if (fieldMap.containsKey(n)) {
                FType ftype = fieldMap.get(n);
                csmap.put(ftype.name, cps.getValue());
                if (tierGlobalPropertySource.containsProperty(ApplicationConstants.GLOBAL_PROPERTY_PREFIX + ftype.name)) {
                    continue;
                }
                try {
                    Class<?> ctype = ftype.type;
                    Method setMethod = c.getDeclaredMethod(ftype.setMethod, ctype);
                    Method getMethod = c.getDeclaredMethod(ftype.getMethod, new Class<?>[0]);
                    ConfigurationOverride co = new ConfigurationOverride(ftype.name, cps.getValue(), ftype.type, setMethod, getMethod);
                    setValue(config, setMethod, ftype.type.getName(), cps.getValue());
                    newo.put(ftype.name, co);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        List<ConfigurationOverride> updateList = new ArrayList<>();
        Set<String> keysNew = newo.keySet();
        Set<String> keysOld = oldo.keySet();

        for (String snew : keysNew) {
            ConfigurationOverride conew = newo.get(snew);
            System.out.println("Checking: " + snew);
            if (oldo.containsKey(snew)) {
                System.out.println("found it in oldo");
                // in both the old and new overrides
                ConfigurationOverride coold = oldo.get(snew);
                if (coold.equals(conew))
                    continue;
                if (!coold.equals(conew)) {
                    // in both files but they have different values: update
                    updateList.add(conew);
                }
            }
        }
        // all done - set the old overrides to the new ones
        config.setOldOverrides(new HashMap<>(newo));
        return updateList;
    }

    private List<ConfigurationOverride> updateApplicationConfig(Configuration config, Map<String, Object> csmap, String tierName, String componentName,
            String name, String auditComponent) {
        List<ConfigProperty> cp = configClient.getProperties(tierName, componentName, name, auditComponent);
        // get the list of fields
        Class<?> c = config.getClass();
        Field[] fields = c.getDeclaredFields();
        HashMap<String, FType> fieldMap = new HashMap<>(fields.length);
        for (Field f : fields) {
            FType ftype = new FType(f.getName(), f.getType());
            fieldMap.put(normalizeName(f.getName()), ftype);
        }
        // create new override list
        HashMap<String, ConfigurationOverride> originalProperties = config.getOriginalProperties();
        HashMap<String, ConfigurationOverride> oldo = config.getOldOverrides();
        HashMap<String, ConfigurationOverride> newo = new HashMap<>();
        // System.out.println("number original props: " + orgo.size());
        // System.out.println("number of old overrides: " + oldo.size());
        for (ConfigProperty cps : cp) {
            String n = normalizeName(cps.getName());
            if (fieldMap.containsKey(n)) {
                FType ftype = fieldMap.get(n);
                csmap.put(ftype.name, cps.getValue());
                try {
                    Class<?> ctype = ftype.type;
                    Method setMethod = c.getDeclaredMethod(ftype.setMethod, ctype);
                    Method getMethod = c.getDeclaredMethod(ftype.getMethod, new Class<?>[0]);
                    ConfigurationOverride co = new ConfigurationOverride(ftype.name, cps.getValue(), ftype.type, setMethod, getMethod);
                    newo.put(ftype.name, co);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        Set<String> keysNew = newo.keySet();
        Set<String> keysOld = oldo.keySet();
        // System.out.println("number of old overrides: " + oldo.size());
        // System.out.println("number of new overrides: " + newo.size());
        // ConfigUtils.bigPrint("old overrides");
        // for (String s : keysOld) {
        // System.out.println(oldo.get(s));
        // }
        // ConfigUtils.bigPrint("new overrides");
        // for (String s : keysNew) {
        // System.out.println(newo.get(s));
        // }
        // loop through the new values and see if there are any updates (new fields or changed fields)
        List<ConfigurationOverride> updateList = new ArrayList<>();
        List<ConfigurationOverride> deleteList = new ArrayList<>();

        for (String snew : keysNew) {
            ConfigurationOverride conew = newo.get(snew);
            if (oldo.containsKey(snew)) {
                // in both the old and new overrides
                ConfigurationOverride coold = oldo.get(snew);
                if (coold.equals(conew))
                    continue;
                if (!coold.equals(conew)) {
                    // in both files but they have different values: update
                    updateList.add(conew);
                }
            } else {
                // in new but not in old: update
                updateList.add(conew);
            }
        }
        // loop through the old values and see if they are missing from the new values: delete
        for (String sold : keysOld) {
            if (!newo.containsKey(sold)) {
                // field has been deleted
                ConfigurationOverride coold = oldo.get(sold);
                deleteList.add(coold);
            }
        }

        // System.out.println("# Number of updated fields: " + updateList.size());
        // System.out.println("# Number of deleted fields: " + deleteList.size());
        // System.out.println("# Updated entries: ");
        for (ConfigurationOverride co : updateList) {
            // System.out.println(" " + co.toString());
            setValue(config, co.getSetterMethod(), co.getType().getName(), co.getValue());
        }
        // System.out.println("# Deleted entries: ");
        for (ConfigurationOverride co : deleteList) {
            ConfigurationOverride original = originalProperties.get(co.getName());
            setValue(config, co.getSetterMethod(), co.getType().getName(), original.getValue());
            // System.out.println(" " + co.toString());
        }

        // all done - set the old overrides to the new ones
        config.setOldOverrides(new HashMap<>(newo));

        List<ConfigurationOverride> overrideList = new ArrayList<>();
        overrideList.addAll(updateList);
        overrideList.addAll(deleteList);
        return overrideList;
    }

    public Map<String, Object> loadConfigServerProperties(Configuration config, String tierName, String componentName, String name, String auditComponent) {
        // Well use this below for global properties only, to see if we have a tier global property override
        ConfigurableEnvironment ce = (ConfigurableEnvironment) env;
        PropertySource<?> tierGlobalPropertySource = ce.getPropertySources().get(ApplicationConstants.PSN_TIER_GLOBAL_OVERRIDES);

        // We'll pull the full list with one call to keep the load down on the config server (can throw a RuntimeException)
        // TODO catch and log Runtime exception
        List<ConfigProperty> cp = configClient.getProperties(tierName, componentName, name, auditComponent);

        // We'll store the values in our Configuration class but also as a PropertySource so we can tell *where* the property came from
        Map<String, Object> csmap = new HashMap<>();

        // Store the original properties pre-config server hit:
        HashMap<String, ConfigurationOverride> originalProperties = new HashMap<>();

        // The properties need to *relaxed-match* the field names in our Configuration class, get the fields:
        Class<?> c = config.getClass();
        Field[] fields = c.getDeclaredFields();
        HashMap<String, FType> fieldMap = new HashMap<>(fields.length);
        for (Field f : fields) {
            FType ftype = new FType(f.getName(), f.getType());
            fieldMap.put(normalizeName(f.getName()), ftype);
            // System.out.println("### " + f.getName() + " : " + f.getType());
            try {
                Method getMethod = c.getDeclaredMethod(ftype.getMethod, new Class<?>[0]);
                Method setMethod = c.getDeclaredMethod(ftype.setMethod, ftype.type);
                Object object = getMethod.invoke(config);
                String so = null;
                if (object != null)
                    so = object.toString();
                ConfigurationOverride co = new ConfigurationOverride(ftype.name, so, ftype.type, setMethod, getMethod);
                originalProperties.put(ftype.name, co);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            config.setOriginalProperties(originalProperties);
        }
        // loop through all the properties we grabbed from the config service and see if any match
        for (ConfigProperty cps : cp) {
            String n = normalizeName(cps.getName());
            if (fieldMap.containsKey(n)) {
                FType ftype = fieldMap.get(n);
                csmap.put(ftype.name, cps.getValue());
                // GlobalConfig: only use config server value if we haven't set this field locally
                if (config instanceof GlobalConfiguration) {
                    if (tierGlobalPropertySource.containsProperty(ApplicationConstants.GLOBAL_PROPERTY_PREFIX + ftype.name)) {
                        continue;
                    }
                }
                try {
                    Class<?> ctype = ftype.type;
                    Method setMethod = c.getDeclaredMethod(ftype.setMethod, ctype);
                    Method getMethod = c.getDeclaredMethod(ftype.getMethod, new Class<?>[0]);
                    System.out.println(ftype.name + ": " + cps.getValue());
                    ConfigurationOverride co = new ConfigurationOverride(ftype.name, cps.getValue(), ftype.type, setMethod, getMethod);
                    config.getOldOverrides().put(ftype.name, co);
                    setValue(config, setMethod, ftype.type.getName(), cps.getValue());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException | SecurityException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return csmap;
    }

    private void setValue(Configuration config, Method setMethod, String type, String value) {
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
            ex.printStackTrace();
        }
    }

    private String normalizeName(String s) {
        if (s == null)
            return s;
        return s.toUpperCase().replaceAll("_", "").replaceAll("-", "").replaceAll("\\.", "");
    }

    public void loadApplicationProperties() {
        // the application properties should be based on the tierNm and componentNm
        String tierName = appConfig.getTierName();
        String componentName = appConfig.getComponentName();
        String name = "";
        String auditComponent = appConfig.getComponentName();
        Map<String, Object> csmap = loadConfigServerProperties(appConfig, tierName, componentName, name, auditComponent);

        // We'll store the values in our ApplicationConfigProperties but also as a PropertySource so we can tell *where* the property came from
        ConfigurableEnvironment ce = (ConfigurableEnvironment) env;
        MutablePropertySources sources = ce.getPropertySources();

        // use addFirst() so that the config server values will override the local properties
        // this will let us set feature flags in the config server that will override everything in a running app
        sources.addFirst(new MapPropertySource(ApplicationConstants.PSN_CONFIG_SERVER_APPLICATION, csmap));
        System.out.println(appConfig);
    }

    private void loadTierGlobalProperties() {
        String tierName = "tier_global";
        String componentName = "";
        String name = "";
        String auditComponent = appConfig.getComponentName();
        Map<String, Object> csmap = new HashMap<>();

        // We'll store the values in our ApplicationConfigProperties but also as a PropertySource so we can tell *where* the property came from
        ConfigurableEnvironment ce = (ConfigurableEnvironment) env;
        MutablePropertySources sources = ce.getPropertySources();

        // Get whatever Service Discovery info we need
        componentName = ApplicationConstants.COMPONENTNM_SERVICE_DISCOVERY;
        csmap.putAll(loadConfigServerProperties(globalConfig, tierName, componentName, name, auditComponent));

        // Get whatever Kafka info we need
        componentName = ApplicationConstants.COMPONENTNM_MKT_KAFKA;
        csmap.putAll(loadConfigServerProperties(globalConfig, tierName, componentName, name, auditComponent));

        // Get whatever Redis info we need
        componentName = ApplicationConstants.COMPONENTNM_AWS_REDIS;
        csmap.putAll(loadConfigServerProperties(globalConfig, tierName, componentName, name, auditComponent));

        // Get whatever S3 Bucket info we need
        componentName = ApplicationConstants.COMPONENTNM_AWS_S3_BUCKETS;
        csmap.putAll(loadConfigServerProperties(globalConfig, tierName, componentName, name, auditComponent));

        // *NOTE ADDLAST* local properties for the global resources will override the
        // config server
        sources.addLast(new MapPropertySource(ApplicationConstants.PSN_CONFIG_SERVER_GLOBAL, csmap));
        System.out.println(globalConfig);

    }

    private class FType {
        private String name;
        private String setMethod;
        private String getMethod;
        private Class<?> type;

        private FType(String name, Class<?> type) {
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

}
