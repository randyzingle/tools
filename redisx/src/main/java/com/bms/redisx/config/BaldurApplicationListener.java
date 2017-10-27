package com.bms.redisx.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.stereotype.Component;

import com.bms.redisx.ApplicationConstants;
import com.bms.redisx.config.server.ConfigPropertyShort;
import com.bms.redisx.config.server.ConfigurationClient;
import com.bms.redisx.config.ws.RedisPropertyListener;

/**
 * This class loads application specific and tier_global properties from the Configuration Service.
 * The method, onApplicationEvent, is called once the Spring application context is fully loaded so all of 
 * the configuration properties will have been loaded from property files and the environment. 
 * 
 * Defaults for ALL application specific properties are given in application.properties. These will be overridden 
 * by environment variables, system properties, and configuration server values (for the given tierNm and componentNm).
 * The order of precedence for properties is (top one wins):
 * <ol>
 * <li>Configuration Server value</li>
 * <li>System Property (-Dname=value pair given on java command line)</li>
 * <li>OS Environment variable</li>
 * <li>application.properties</li>
 * </ol>
 * 
 * When running in EC2 the URL for the configuration server must be passed in as an environment variable.
 * When running locally you can use and environment variable or set the value in application.properties.
 * 
 * @author razing
 *
 */
@Component
public class BaldurApplicationListener {

	@Autowired
	Environment env;
	
	@Autowired
	ApplicationContext ctx;

	@Autowired
	ApplicationConfigProperties applicationProps;
	
	@Autowired
	TierGlobalConfigProperties globalProps;
	
	@Autowired
	ConfigurationClient configClient;
	
	@Autowired
	RedisPropertyListener redisPropertyListener;

	/*
	 * This will be called when the ApplicationContext gets initialized and all the Beans have been loaded.
	 * In particular the config beans, ApplicationConfigProperties and TierGlobalConfigProperties will have been loaded
	 * and initialized with their local properties.
	 * 
	 * This contacts the config server that ApplicationConfigProperties has a reference to and retrieves application-specific
	 * and tier_global properties. 
	 * 
	 * The config server values for application-specific properties will over-write the local ApplicationConfigProperties. 
	 * We're assuming that they are only being set as feature flags or emergency overrides. 
	 * 
	 * The config server values will NOT over-write the local TierGlobalConfigProperties.
	 * We're assuming local values are being set only in a development environment and we are temporarily hitting
	 * alternative S3 buckets, Redis servers, etc. to test dev stuff. 
	 * 
	 * This method will also be hit when the context is refreshed (but this shouldn't happen in the normal course of things)
	 * and when the context is closed (application shutdown)
	 */
	@EventListener
	public void onApplicationEvent(ContextRefreshedEvent event) {
		loadApplicationProperties();
		loadTierGlobalProperties();
		configureRedis();
		redisPropertyListener.sendApplicationProperties(applicationProps);
	}
	
	@EventListener
	public void onApplicationEvent(ConfigEvent event) {
		System.out.println(event.getMessage());
		loadApplicationProperties();
		loadTierGlobalProperties();
	}
	
	private void configureRedis() {
		JedisConnectionFactory factory = ctx.getBean(JedisConnectionFactory.class);
		if (globalProps != null && globalProps.getRedisClusterHost() != null) {
			factory.setHostName(globalProps.getRedisClusterHost());
		}
		if (globalProps != null && globalProps.getRedisClusterPort() != 0) {
			factory.setPort(globalProps.getRedisClusterPort());
		}
	}

	private void loadApplicationProperties() {
		// the application properties should be based on the tierNm and componentNm in the config server
		String tierName = applicationProps.getTierName();
		String componentName = applicationProps.getComponentName();
		String name = "";
		String auditComponent = applicationProps.getComponentName();

		// We'll pull the full list with one call to keep the load down on the config server
		List<ConfigPropertyShort> cp = configClient.getPropertyFromConfigServer(tierName, componentName, name, auditComponent);
		// TODO - set up a timer to hit config service if it's down (once ~5 mins or so) -> set UI flag (config server down)

		// We'll store the values in our ApplicationConfigProperties but also as a PropertySource so we can tell *where* the property came from
		ConfigurableEnvironment ce = (ConfigurableEnvironment) env;
		MutablePropertySources sources = ce.getPropertySources();
		Map<String, Object> csmap = new HashMap<>();

		// do this for all properties that can be overridden by application-specific properties in the config server
		for (ConfigPropertyShort cps : cp) {
			if (cps.getName().equals(ApplicationConstants.HANDLER_CHAIN)) {
				applicationProps.setHandlerChain(cps.getValue());
				csmap.put(cps.getName(), cps.getValue());
			}
			if (cps.getName().equals(ApplicationConstants.CONSUMER_GROUP)) {
				applicationProps.setConsumerGroup(cps.getValue());
				csmap.put(cps.getName(), cps.getValue());
			}
			if (cps.getName().equals(ApplicationConstants.READ_FROM_START)) {
				applicationProps.setReadFromStart(Boolean.getBoolean(cps.getValue()));
				csmap.put(cps.getName(), cps.getValue());
			}
			if (cps.getName().equals(ApplicationConstants.GEO_AUTO_RELOAD)) {
				applicationProps.setGeoAutoReload(Boolean.getBoolean(cps.getValue()));
				csmap.put(cps.getName(), cps.getValue());
			}
			if (cps.getName().equals(ApplicationConstants.KAFKA_TOPIC_PREFIX)) {
				applicationProps.setKafkaTopicPrefix(cps.getValue());
				csmap.put(cps.getName(), cps.getValue());
			}
			
		}
		// *NOTE ADDFIRST* local properties will be overridden by the config server values
		// this will let us set feature flags in the config server that will override everything in a running app
		sources.addFirst(new MapPropertySource("CONFIG_SERVER_APPLICATION", csmap));
		System.out.println(applicationProps);
	}

	private void loadTierGlobalProperties() {
		String tierName = "tier_global";		
		String name = "";
		String componentName = null;
		String auditComponent = applicationProps.getComponentName();
		List<ConfigPropertyShort> cp = null;
		
		// Store props in TierGlobalConfigProperties but also as a PropertySource so we can tell *where* the property came from
		
		ConfigurableEnvironment ce = (ConfigurableEnvironment) env;
		MutablePropertySources sources = ce.getPropertySources();
		Map<String, Object> csmap = new HashMap<>();
		
		// Get whatever Service Discovery info we need
		componentName = ApplicationConstants.COMPONENTNM_SERVICE_DISCOVERY;
		cp = configClient.getPropertyFromConfigServer(tierName, componentName, name, auditComponent);
		for (ConfigPropertyShort cps : cp) {
			if (cps.getName().equals(ApplicationConstants.TENANT_SERVICE_URL)) {
				if(globalProps.getTenantServiceUrl() == null) globalProps.setTenantServiceUrl(cps.getValue());
				csmap.put(cps.getName(), cps.getValue());
			}
		}
		
		// Get whatever Kafka info we need
		componentName = ApplicationConstants.COMPONENTNM_MKT_KAFKA;
		cp = configClient.getPropertyFromConfigServer(tierName, componentName, name, auditComponent);
		for (ConfigPropertyShort cps : cp) {
			if (cps.getName().equals(ApplicationConstants.TOPIC_NAME_ENHANCED_EVENTS)) {
				if(globalProps.getTopicEnhancedEvents() == null) globalProps.setTopicEnhancedEvents(cps.getValue());
				csmap.put(cps.getName(), cps.getValue());
			}
			if (cps.getName().equals(ApplicationConstants.TOPIC_NAME_RAW_EVENTS)) {
				if(globalProps.getTopicRawEvents() == null) globalProps.setTopicRawEvents(cps.getValue());
				csmap.put(cps.getName(), cps.getValue());
			}
		}
		
		// Get whatever Redis info we need
		componentName = ApplicationConstants.COMPONENTNM_AWS_REDIS;
		cp = configClient.getPropertyFromConfigServer(tierName, componentName, name, auditComponent);
		for (ConfigPropertyShort cps : cp) {
			if (cps.getName().equals(ApplicationConstants.REDIS_HOST)) {
				if(globalProps.getRedisClusterHost() == null) globalProps.setRedisClusterHost(cps.getValue());
				csmap.put(cps.getName(), cps.getValue());
			}
			if (cps.getName().equals(ApplicationConstants.REDIS_PORT)) {
				try {
					int port = Integer.parseInt(cps.getValue());
					if(globalProps.getRedisClusterPort() == 0) globalProps.setRedisClusterPort(port);
					csmap.put(cps.getName(), cps.getValue());
				} catch (NumberFormatException ex) {
					ex.printStackTrace();
				}
			}
		}
		
		// TODO get the order right - local should override config server for tier_global - complete for the below values
		// Get whatever S3 Bucket info we need
		componentName = ApplicationConstants.COMPONENTNM_AWS_S3_BUCKETS;
		cp = configClient.getPropertyFromConfigServer(tierName, componentName, name, auditComponent);
		for (ConfigPropertyShort cps : cp) {
			if (cps.getName().equals(ApplicationConstants.S3_DATA_BUCKET_TOPIC)) {
				globalProps.setDataBucketTopic(cps.getValue());
				csmap.put(cps.getName(), cps.getValue());
			}
			if (cps.getName().equals(ApplicationConstants.S3_CONFIG_BUCKET)) {
				globalProps.setConfigBucket(cps.getValue());
				csmap.put(cps.getName(), cps.getValue());
			}
			if (cps.getName().equals(ApplicationConstants.S3_DATA_BUCKET)) {
				globalProps.setDataBucket(cps.getValue());
				csmap.put(cps.getName(), cps.getValue());
			}
			if (cps.getName().equals(ApplicationConstants.S3_DEPLOYMENT_BUCKET)) {
				globalProps.setDeploymentBucket(cps.getValue());
				csmap.put(cps.getName(), cps.getValue());
			}
			if (cps.getName().equals(ApplicationConstants.S3_OPS_BUCKET)) {
				globalProps.setOpsBucket(cps.getValue());
				csmap.put(cps.getName(), cps.getValue());
			}
			if (cps.getName().equals(ApplicationConstants.S3_TEST_BUCKET)) {
				globalProps.setTestBucket(cps.getValue());
				csmap.put(cps.getName(), cps.getValue());
			}
		}
		// *NOTE ADDLAST* local properties for the global resources will override the config server
		sources.addLast(new MapPropertySource("CONFIG_SERVER_GLOBAL", csmap));
		System.out.println(globalProps);

	}
}