package com.bms.finnr.scheduled;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.bms.finnr.config.ApplicationConfiguration;
import com.bms.finnr.config.ApplicationConstants;
import com.bms.finnr.config.ConfigUtils;
import com.bms.finnr.config.Configuration;
import com.bms.finnr.config.ConfigurationOverride;
import com.bms.finnr.config.GlobalConfiguration;
import com.bms.finnr.config.configserver.ConfigProperty;
import com.bms.finnr.config.configserver.ConfigServerClient;
import com.bms.finnr.config.configserver.ReflectionFieldInfo;

@Component
public class ScheduledConfigServicePoll {
    
    private final static Logger logger = LoggerFactory.getLogger( ScheduledConfigServicePoll.class );

    @Autowired
    ApplicationEventPublisher publisher;

    @Autowired
    Environment env;

    @Autowired
    ApplicationConfiguration appConfig;

    @Autowired
    GlobalConfiguration globalConfig;

    @Autowired
    ConfigServerClient configClient;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    // default schedule will have us hitting the config server every 10 min = 600000 ms. Wait 5 min before first run
    // Override in application.properties.
    @Scheduled(initialDelay=300000, fixedRateString = "${application.configServerPingRateMs:600000}")
    public void refreshConfigServiceData() {
        logger.debug("Hitting the configuration server at {}", dateFormat.format(new Date()));
        refreshProperties();
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
        logger.debug("Total runtime for config server hit: {}", (endTime - startTime) + " ms");
        logger.debug(appConfig.toString());
        logger.debug(globalConfig.toString());
    }

    public List<ConfigurationOverride> refreshApplicationProperties() {
        String tierName = appConfig.getTierName();
        String componentName = appConfig.getComponentName();
        String name = "";
        String auditComponent = appConfig.getComponentName();
        Map<String, Object> csmap = new HashMap<>();
        List<ConfigProperty> cpList = configClient.getProperties(tierName, componentName, name, auditComponent);
        List<ConfigurationOverride> overrideList = updateApplicationConfig(appConfig, cpList, csmap);
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
        List<ConfigProperty> cpList = new ArrayList<>();

        // Get whatever Service Discovery info we need
        componentName = ApplicationConstants.COMPONENTNM_SERVICE_DISCOVERY;
        cpList.addAll(configClient.getProperties(tierName, componentName, name, auditComponent));

        // Get whatever Kafka info we need
        componentName = ApplicationConstants.COMPONENTNM_MKT_KAFKA;
        cpList.addAll(configClient.getProperties(tierName, componentName, name, auditComponent));

        // Get whatever Redis info we need
        componentName = ApplicationConstants.COMPONENTNM_AWS_REDIS;
        cpList.addAll(configClient.getProperties(tierName, componentName, name, auditComponent));

        // Get whatever S3 Bucket info we need
        componentName = ApplicationConstants.COMPONENTNM_AWS_S3_BUCKETS;
        cpList.addAll(configClient.getProperties(tierName, componentName, name, auditComponent));

        overrideList.addAll(updateGlobalConfig(globalConfig, cpList, csmap));

        ConfigurableEnvironment ce = (ConfigurableEnvironment) env;
        MutablePropertySources sources = ce.getPropertySources();
        sources.replace(ApplicationConstants.PSN_CONFIG_SERVER_GLOBAL, new MapPropertySource(ApplicationConstants.PSN_CONFIG_SERVER_GLOBAL, csmap));
        return overrideList;
    }

    private List<ConfigurationOverride> updateGlobalConfig(Configuration config, List<ConfigProperty> cpList, Map<String, Object> csmap) {
        ConfigurableEnvironment ce = (ConfigurableEnvironment) env;
        PropertySource<?> tierGlobalPropertySource = ce.getPropertySources().get(ApplicationConstants.PSN_TIER_GLOBAL_OVERRIDES);

        // get the list of fields
        Class<?> c = config.getClass();
        Field[] fields = c.getDeclaredFields();
        HashMap<String, ReflectionFieldInfo> fieldMap = new HashMap<>(fields.length);
        for (Field f : fields) {
            ReflectionFieldInfo ftype = new ReflectionFieldInfo(f.getName(), f.getType());
            fieldMap.put(ConfigUtils.normalizeName(f.getName()), ftype);
        }
        HashMap<String, ConfigurationOverride> originalProperties = config.getOriginalProperties();
        HashMap<String, ConfigurationOverride> oldo = config.getOldOverrides();
        HashMap<String, ConfigurationOverride> newo = new HashMap<>();
        for (ConfigProperty cps : cpList) {
            String n = ConfigUtils.normalizeName(cps.getName());
            if (fieldMap.containsKey(n)) {
                ReflectionFieldInfo ftype = fieldMap.get(n);
                csmap.put(ftype.name, cps.getValue());
                if (tierGlobalPropertySource.containsProperty(ApplicationConstants.GLOBAL_PROPERTY_PREFIX + ftype.name)) {
                    continue;
                }
                try {
                    Class<?> ctype = ftype.type;
                    Method setMethod = c.getDeclaredMethod(ftype.setMethod, ctype);
                    Method getMethod = c.getDeclaredMethod(ftype.getMethod, new Class<?>[0]);
                    ConfigurationOverride co = new ConfigurationOverride(ftype.name, cps.getValue(), ftype.type, setMethod, getMethod);
                    ConfigUtils.setValue(config, setMethod, ftype.type.getName(), cps.getValue());
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
            if (oldo.containsKey(snew)) {
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

    private List<ConfigurationOverride> updateApplicationConfig(Configuration config, List<ConfigProperty> cpList, Map<String, Object> csmap) {
        // get the list of fields
        Class<?> c = config.getClass();
        Field[] fields = c.getDeclaredFields();
        HashMap<String, ReflectionFieldInfo> fieldMap = new HashMap<>(fields.length);
        for (Field f : fields) {
            ReflectionFieldInfo ftype = new ReflectionFieldInfo(f.getName(), f.getType());
            fieldMap.put(ConfigUtils.normalizeName(f.getName()), ftype);
        }
        // create new override list
        HashMap<String, ConfigurationOverride> originalProperties = config.getOriginalProperties();
        HashMap<String, ConfigurationOverride> oldo = config.getOldOverrides();
        HashMap<String, ConfigurationOverride> newo = new HashMap<>();
        for (ConfigProperty cps : cpList) {
            String n = ConfigUtils.normalizeName(cps.getName());
            if (fieldMap.containsKey(n)) {
                ReflectionFieldInfo ftype = fieldMap.get(n);
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

        for (ConfigurationOverride co : updateList) {
            ConfigUtils.setValue(config, co.getSetterMethod(), co.getType().getName(), co.getValue());
        }

        for (ConfigurationOverride co : deleteList) {
            ConfigurationOverride original = originalProperties.get(co.getName());
            ConfigUtils.setValue(config, co.getSetterMethod(), co.getType().getName(), original.getValue());
        }

        // all done - set the old overrides to the new ones
        config.setOldOverrides(new HashMap<>(newo));

        List<ConfigurationOverride> overrideList = new ArrayList<>();
        overrideList.addAll(updateList);
        overrideList.addAll(deleteList);
        return overrideList;
    }

}