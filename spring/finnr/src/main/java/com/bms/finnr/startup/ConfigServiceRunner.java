package com.bms.finnr.startup;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
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
import com.bms.finnr.config.configserver.ReflectionFieldInfo;

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
    
    private final static Logger logger = LoggerFactory.getLogger( ConfigServiceRunner.class );

    @Autowired
    Environment env;

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

    public Map<String, Object> loadConfigServerProperties(Configuration config, List<ConfigProperty> cp) {
        // Well use this below for global properties only, to see if we have a tier global property override
        ConfigurableEnvironment ce = (ConfigurableEnvironment) env;
        PropertySource<?> tierGlobalPropertySource = ce.getPropertySources().get(ApplicationConstants.PSN_TIER_GLOBAL_OVERRIDES);

        // We'll store the values in our Configuration class but also as a PropertySource so we can tell *where* the property came from
        Map<String, Object> csmap = new HashMap<>();

        // The properties need to *relaxed-match* the field names in our Configuration class, get the fields:
        Class<?> c = config.getClass();
        Field[] fields = c.getDeclaredFields();
        HashMap<String, ReflectionFieldInfo> fieldMap = new HashMap<>(fields.length);
        for (Field f : fields) {
            ReflectionFieldInfo ftype = new ReflectionFieldInfo(f.getName(), f.getType());
            fieldMap.put(ConfigUtils.normalizeName(f.getName()), ftype);
            // System.out.println("### " + f.getName() + " : " + f.getType());
            try {
                Method getMethod = c.getDeclaredMethod(ftype.getMethod, new Class<?>[0]);
                Method setMethod = c.getDeclaredMethod(ftype.setMethod, ftype.type);
                Object object = getMethod.invoke(config);
                String so = null;
                if (object != null)
                    so = object.toString();
                ConfigurationOverride co = new ConfigurationOverride(ftype.name, so, ftype.type, setMethod, getMethod);
                config.getOriginalProperties().put(ftype.name, co);
            } catch (Exception ex) {
                logger.error(ex.getMessage());
            }

        }
        // loop through all the properties we grabbed from the config service and see if any match
        for (ConfigProperty cps : cp) {
            String n = ConfigUtils.normalizeName(cps.getName());
            if (fieldMap.containsKey(n)) {
                ReflectionFieldInfo ftype = fieldMap.get(n);
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
                    ConfigUtils.setValue(config, setMethod, ftype.type.getName(), cps.getValue());
                } catch (NumberFormatException ex) {
                    logger.error(ex.getMessage());
                } catch (NoSuchMethodException | SecurityException ex) {
                    logger.error(ex.getMessage());
                } catch (Exception ex) {
                    logger.error(ex.getMessage());
                }
            }
        }
        return csmap;
    }




    public void loadApplicationProperties() {
        // the application properties should be based on the tierNm and componentNm
        String tierName = appConfig.getTierName();
        String componentName = appConfig.getComponentName();
        String name = "";
        String auditComponent = appConfig.getComponentName();

        List<ConfigProperty> cpList = configClient.getProperties(tierName, componentName, name, auditComponent);

        Map<String, Object> csmap = loadConfigServerProperties(appConfig, cpList);

        // We'll store the values in our ApplicationConfigProperties but also as a PropertySource so we can tell *where* the property came from
        ConfigurableEnvironment ce = (ConfigurableEnvironment) env;
        MutablePropertySources sources = ce.getPropertySources();

        // use addFirst() so that the config server values will override the local properties
        // this will let us set feature flags in the config server that will override everything in a running app
        sources.addFirst(new MapPropertySource(ApplicationConstants.PSN_CONFIG_SERVER_APPLICATION, csmap));
        logger.debug(appConfig.toString());
    }

    private void loadTierGlobalProperties() {
        String tierName = "tier_global";
        String componentName = "";
        String name = "";
        String auditComponent = appConfig.getComponentName();

        // Get whatever Service Discovery info we need
        componentName = ApplicationConstants.COMPONENTNM_SERVICE_DISCOVERY;
        List<ConfigProperty> cpList = configClient.getProperties(tierName, componentName, name, auditComponent);

        // Get whatever Kafka info we need
        componentName = ApplicationConstants.COMPONENTNM_MKT_KAFKA;
        cpList.addAll(configClient.getProperties(tierName, componentName, name, auditComponent));

        // Get whatever Redis info we need
        componentName = ApplicationConstants.COMPONENTNM_AWS_REDIS;
        cpList.addAll(configClient.getProperties(tierName, componentName, name, auditComponent));

        // Get whatever S3 Bucket info we need
        componentName = ApplicationConstants.COMPONENTNM_AWS_S3_BUCKETS;
        cpList.addAll(configClient.getProperties(tierName, componentName, name, auditComponent));

        // Get whatever Kafka info we need
        componentName = ApplicationConstants.COMPONENTNM_MKT_KAFKA;
        cpList.addAll(configClient.getProperties(tierName, componentName, name, auditComponent));

        Map<String, Object> csmap = loadConfigServerProperties(globalConfig, cpList);

        // We'll store the values in our ApplicationConfigProperties but also as a PropertySource so we can tell *where* the property came from
        ConfigurableEnvironment ce = (ConfigurableEnvironment) env;
        MutablePropertySources sources = ce.getPropertySources();

        // *NOTE ADDLAST* local properties for the global resources will override the
        // config server
        sources.addLast(new MapPropertySource(ApplicationConstants.PSN_CONFIG_SERVER_GLOBAL, csmap));
        logger.debug(globalConfig.toString());

    }

}
