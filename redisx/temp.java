package com.bms.finnr.startup;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.bms.finnr.config.GlobalConfiguration;
import com.bms.finnr.config.configserver.ConfigProperty;
import com.bms.finnr.config.configserver.ConfigServerClient;

/**
 * This class loads application specific and tier_global properties from the
 * Configuration Service. The method, onApplicationEvent, is called once the
 * Spring application context is fully loaded so all of the configuration
 * properties will have been loaded from property files and the environment.
 *
 * The Configuration Server is queried with tierNm=appConfig.getTierName() and
 * componentNm=appConfig.getComponentName()
 *
 * Defaults for all application specific properties are given in
 * application.properties. These will be overridden by environment variables,
 * system properties, and configuration server values (for the given tierNm and
 * componentNm). We are assuming that values for application-specific properties
 * are only set in the config server as overrides or features flags which is why
 * that has top precedence.
 *
 * The order of precedence for properties as a list (top one wins):
 * <ol>
 * <li>Configuration Server value</li>
 * <li>System Property (-Dname=value pair given on java command line)</li>
 * <li>OS Environment variable</li>
 * <li>application.properties</li>
 * </ol>
 *
 * When running in EC2 the URL for the configuration server must be passed in as
 * an environment variable. When running locally you can use and environment
 * variable or set the value in application.properties.
 *
 * This is the first ApplicationRunner that runs (getOrder() = 0)
 *
 * @author razing
 *
 */
@Component
public class ConfigServerRunner implements ApplicationRunner, Ordered {

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
		System.out.println(appConfig.getTierName());
		System.out.println(appConfig.getComponentName());
		loadApplicationProperties();
		loadTierGlobalProperties();
//		displayPropertySources();
	}

	public Map<String, Object> loadConfigServerProperties(Configuration config, String tierName, String componentName,
			String name, String auditComponent) {
		// We'll pull the full list with one call to keep the load down on the config
		// server (can throw a RuntimeException)
		// TODO catch and log Runtime exception
		List<ConfigProperty> cp = configClient.getProperties(tierName, componentName, name, auditComponent);

		// We'll store the values in our Configuration class but also as a
		// PropertySource so we can tell *where* the property came from
		Map<String, Object> csmap = new HashMap<>();

		// The properties should match the field names in our Configuration class
		Class<?> c = config.getClass();
		Field[] fields = c.getDeclaredFields();
		HashMap<String, FType> fieldMap = new HashMap<>(fields.length);
		for (Field f : fields) {
			FType ft = new FType(f.getName(), f.getType());
			fieldMap.put(f.getName().toUpperCase(), ft);
//			System.out.println("### " + f.getName() + " : " + f.getType());
		}

		for (ConfigProperty cps : cp) {
			String n = cps.getName().toUpperCase().replaceAll("_", "").replaceAll("-","").replaceAll("\\.", "");
			if (fieldMap.containsKey(n)) {
				FType ftype = fieldMap.get(n);
				Method setMethod;
				Method getMethod;
				try {
					Class<?> ctype = ftype.type[0];
//					ConfigUtils.bigPrint(ftype.setMethod + " : " + ctype);
					setMethod = c.getDeclaredMethod(ftype.setMethod, ctype);
					setMethod.setAccessible(true);
					getMethod = c.getDeclaredMethod(ftype.getMethod, null);
					getMethod.setAccessible(true);
					// GlobalConfig: only use config server value if we haven't set this field locally
					if (config instanceof GlobalConfiguration && getMethod.invoke(config, null) != null)
						continue;
					if (ctype.getName().equals(String.class.getName())) {
						setMethod.invoke(config, cps.getValue());
					} else if (ctype.getName().equals(Boolean.class.getName())) {
						setMethod.invoke(config, Boolean.valueOf(cps.getValue()));
					} else if (ctype.getName().equals(Byte.class.getName())) {
						setMethod.invoke(config, Byte.valueOf(cps.getValue()));
					} else if (ctype.getName().equals(Double.class.getName())) {
						setMethod.invoke(config, Double.valueOf(cps.getValue()));
					} else if (ctype.getName().equals(Float.class.getName())) {
						setMethod.invoke(config, Float.valueOf(cps.getValue()));
					} else if (ctype.getName().equals(Integer.class.getName())) {
						setMethod.invoke(config, Integer.valueOf(cps.getValue()));
					} else if (ctype.getName().equals(Long.class.getName())) {
						setMethod.invoke(config, Long.valueOf(cps.getValue()));
					} else if (ctype.getName().equals(Short.class.getName())) {
						setMethod.invoke(config, Short.valueOf(cps.getValue()));
					}
					csmap.put(cps.getName(), cps.getValue());
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException | SecurityException | InvocationTargetException
						| IllegalAccessException e) {
					e.printStackTrace();
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
		Map<String, Object> csmap = loadConfigServerProperties(appConfig, tierName, componentName, name,
				auditComponent);

		// We'll store the values in our ApplicationConfigProperties but also as a
		// PropertySource so we can tell *where* the property came from
		ConfigurableEnvironment ce = (ConfigurableEnvironment) env;
		MutablePropertySources sources = ce.getPropertySources();

		// use addFirst() so that the config server values will override the local
		// properties
		// this will let us set feature flags in the config server that will override
		// everything in a running app
		sources.addFirst(new MapPropertySource("CONFIG_SERVER_APPLICATION", csmap));
		System.out.println(appConfig);
	}

	private void loadTierGlobalProperties() {
		String tierName = "tier_global";
		String name = "";
		String componentName = null;
		String auditComponent = appConfig.getComponentName();
		List<ConfigProperty> cp = null;

		// Store props in TierGlobalConfigProperties but also as a PropertySource so we can tell *where* the property came from

		ConfigurableEnvironment ce = (ConfigurableEnvironment) env;
		MutablePropertySources sources = ce.getPropertySources();
		Map<String, Object> csmap = new HashMap<>();

		// Get whatever Service Discovery info we need
		componentName = ApplicationConstants.COMPONENTNM_SERVICE_DISCOVERY;
		cp = configClient.getProperties(tierName, componentName, name, auditComponent);
		for (ConfigProperty cps : cp) {
			if (cps.getName().equals(ApplicationConstants.TENANT_SERVICE_URL)) {
				if(globalConfig.getTenantServiceUrl() == null) globalConfig.setTenantServiceUrl(cps.getValue());
				csmap.put(cps.getName(), cps.getValue());
			}
		}

		// Get whatever Kafka info we need
		componentName = ApplicationConstants.COMPONENTNM_MKT_KAFKA;
		cp = configClient.getProperties(tierName, componentName, name, auditComponent);
		for (ConfigProperty cps : cp) {
			if (cps.getName().equals(ApplicationConstants.TOPIC_NAME_ENHANCED_EVENTS)) {
				if(globalConfig.getTopicEnhancedEvents() == null) globalConfig.setTopicEnhancedEvents(cps.getValue());
				csmap.put(cps.getName(), cps.getValue());
			}
			if (cps.getName().equals(ApplicationConstants.TOPIC_NAME_RAW_EVENTS)) {
				if(globalConfig.getTopicRawEvents() == null) globalConfig.setTopicRawEvents(cps.getValue());
				csmap.put(cps.getName(), cps.getValue());
			}
		}

		// Get whatever Redis info we need
		componentName = ApplicationConstants.COMPONENTNM_AWS_REDIS;
		cp = configClient.getProperties(tierName, componentName, name, auditComponent);
		for (ConfigProperty cps : cp) {
			if (cps.getName().equals(ApplicationConstants.REDIS_HOST)) {
				if(globalConfig.getRedisClusterHost() == null) globalConfig.setRedisClusterHost(cps.getValue());
				csmap.put(cps.getName(), cps.getValue());
			}
			if (cps.getName().equals(ApplicationConstants.REDIS_PORT)) {
				try {
					int port = Integer.parseInt(cps.getValue());
					if(globalConfig.getRedisClusterPort() == 0) globalConfig.setRedisClusterPort(port);
					csmap.put(cps.getName(), cps.getValue());
				} catch (NumberFormatException ex) {
					ex.printStackTrace();
				}
			}
		}

		// Get whatever S3 Bucket info we need
		componentName = ApplicationConstants.COMPONENTNM_AWS_S3_BUCKETS;
		cp = configClient.getProperties(tierName, componentName, name, auditComponent);
		for (ConfigProperty cps : cp) {
			if (cps.getName().equals(ApplicationConstants.S3_DATA_BUCKET_TOPIC)) {
				globalConfig.setDataBucketTopic(cps.getValue());
				csmap.put(cps.getName(), cps.getValue());
			}
			if (cps.getName().equals(ApplicationConstants.S3_CONFIG_BUCKET)) {
				globalConfig.setConfigBucket(cps.getValue());
				csmap.put(cps.getName(), cps.getValue());
			}
			if (cps.getName().equals(ApplicationConstants.S3_DATA_BUCKET)) {
				globalConfig.setDataBucket(cps.getValue());
				csmap.put(cps.getName(), cps.getValue());
			}
			if (cps.getName().equals(ApplicationConstants.S3_DEPLOYMENT_BUCKET)) {
				globalConfig.setDeploymentBucket(cps.getValue());
				csmap.put(cps.getName(), cps.getValue());
			}
			if (cps.getName().equals(ApplicationConstants.S3_OPS_BUCKET)) {
				globalConfig.setOpsBucket(cps.getValue());
				csmap.put(cps.getName(), cps.getValue());
			}
			if (cps.getName().equals(ApplicationConstants.S3_TEST_BUCKET)) {
				globalConfig.setTestBucket(cps.getValue());
				csmap.put(cps.getName(), cps.getValue());
			}
		}
		// *NOTE ADDLAST* local properties for the global resources will override the config server
		sources.addLast(new MapPropertySource("CONFIG_SERVER_GLOBAL", csmap));
		System.out.println(globalConfig);
	}

	private void displayPropertySources() {
		ConfigurableEnvironment ce = (ConfigurableEnvironment) env;
		MutablePropertySources sources = ce.getPropertySources();
		ConfigUtils.bigPrint("Property Sources");
		int size = sources.size();
		System.out.println("Number of property sources: " + size);
		Class c = ApplicationConfiguration.class;
		Field[] fields = c.getDeclaredFields();
		for (PropertySource ps : sources) {
			String name = ps.getName();
			System.out.println("Property source name: " + name);
			if (ps.getName().equals("CONFIG_SERVER_GLOBAL")) {
				System.out.println("   " + ApplicationConstants.REDIS_HOST + ": " + (String)ps.getProperty(ApplicationConstants.REDIS_HOST));
				System.out.println("   " + ApplicationConstants.REDIS_PORT + ": " + (String)ps.getProperty(ApplicationConstants.REDIS_PORT));
			}
			if (ps.getName().equals("class path resource [tier_global_overrides.properties]")) {
				System.out.println("   " + ApplicationConstants.REDIS_HOST + ": " + (String)ps.getProperty("tier_global.redisClusterHost"));
				System.out.println("   " + ApplicationConstants.REDIS_PORT + ": " + (String)ps.getProperty("tier_global.redisClusterPort"));
			}
			for (Field f : fields) {
				String s = f.getName();
				// System.out.println(s);
				String propa = (String) ps.getProperty("application." + s);
				if (propa != null) {
					System.out.println("   application." + s + ": " + propa);
				}
				String prop = (String) ps.getProperty(s);
				if (prop != null) {
					System.out.println("   " + s + ": " + prop);
				}
				String senv = convertToEnv(s);
				// System.out.println(senv);
				String propenv = (String) ps.getProperty(senv);
				if (propenv != null) {
					System.out.println("   " + senv + ": " + propenv);
				}
			}
		}
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

	private class FType {
		private String name;
		private String setMethod;
		private String getMethod;
		private Class<?>[] type = new Class[1];

		private FType(String name, Class<?> type) {
			this.name = name;
			this.type[0] = type;
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
			return "FType [name=" + name + ", setMethod=" + setMethod + ", getMethod=" + getMethod + ", type="
					+ Arrays.toString(type) + "]";
		}
	}

//	private void loadTierGlobalProperties() {
//		String tierName = "tier_global";
//		String componentName = "";
//		String name = "";
//		String auditComponent = appConfig.getComponentName();
//		Map<String, Object> csmap = new HashMap<>();
//
//		// We'll store the values in our ApplicationConfigProperties but also as a
//		// PropertySource so we can tell *where* the property came from
//		ConfigurableEnvironment ce = (ConfigurableEnvironment) env;
//		MutablePropertySources sources = ce.getPropertySources();
//
//		// Get whatever Service Discovery info we need
//		componentName = ApplicationConstants.COMPONENTNM_SERVICE_DISCOVERY;
//		csmap.putAll(loadConfigServerProperties(globalConfig, tierName, componentName, name, auditComponent));
//
//		// Get whatever Kafka info we need
//		componentName = ApplicationConstants.COMPONENTNM_MKT_KAFKA;
//		csmap.putAll(loadConfigServerProperties(globalConfig, tierName, componentName, name, auditComponent));
//
//		// Get whatever Redis info we need
//		componentName = ApplicationConstants.COMPONENTNM_AWS_REDIS;
//		csmap.putAll(loadConfigServerProperties(globalConfig, tierName, componentName, name, auditComponent));
//
//		// Get whatever S3 Bucket info we need
//		componentName = ApplicationConstants.COMPONENTNM_AWS_S3_BUCKETS;
//		csmap.putAll(loadConfigServerProperties(globalConfig, tierName, componentName, name, auditComponent));
//
//		// *NOTE ADDLAST* local properties for the global resources will override the
//		// config server
//		sources.addLast(new MapPropertySource("CONFIG_SERVER_GLOBAL", csmap));
//		System.out.println(globalConfig);
//
//	}
}
