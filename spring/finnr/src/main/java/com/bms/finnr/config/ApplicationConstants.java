package com.bms.finnr.config;
/**
 * These constants are mainly used to help lookup configuration properties in the various places that config properties are found.
 * The <b>values</b> for the Strings must match the <b>names</b> of the properties in application.properties, config service, etc. 
 * Matching means the names match from a Spring relaxed binding standpoint, see section 24.7.2 Relaxed binding, for how this works, here:
 * https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html
 * <p>
 * Note when we attempt to match our properties to property names in the configuration server, we can't rely on Spring Boot magic
 * because Spring Boot doesn't understand the Configuration Service. So we do the following to match properties:
 * <ul>
 * <li>we don't use prefixes (drop tier_global. and application.)
 * <li>drop anything that's not a letter (only include [a-zA-Z]+)
 * <li>convert everything to upper case
 * </ul>
 * For example the Tenant Service URL is a tier global service_discovery property in the Configuration Service.
 * We have:
 * <ul>
 * <li>In GlobalConfiguration.java: private String mktTenantServiceUrl
 * <li>In the Configuration Server: name=mkt-tenant.service_URL
 * </ul>
 * We convert these both to MKTTENANTSERVICEURL and attempt to match them. 
 * </p>
 * <p>
 * Examples showing matches for something in the application.properties file:
 * </p>
 * <code>
 * application.consumerGroup=RawEventsEnrichmentReader
 * </code>
 * 
 * And something like this as a config server entry:
 * <code>
 * tierNm=razing
 * componentNm=mkt-events
 * name=consumerGroup
 * value=RawEventsEnrichmentReader
 * </code>
 * 
 * And something like this as an environment variable:
 * <code>
 * APPLICATION_CONSUMER_GROUP=RawEventsEnrichmentReader
 * </code>
 * 
 * And something like this as a system property:
 * <code>
 * -Dapplication.consumerGroup=RawEventsEnrichmentReader
 * </code>
 * 
 * @author razing
 *
 */
public class ApplicationConstants {
	
	// tier_global component names in the Configuration Service
	public static final String COMPONENTNM_SERVICE_DISCOVERY = "service_discovery";
	public static final String COMPONENTNM_AWS_REDIS = "aws-redis";
	public static final String COMPONENTNM_AWS_S3_BUCKETS = "aws-s3-buckets";
	public static final String COMPONENTNM_MKT_KAFKA = "mkt-kafka"; // soon aws-kafka
	
	// property source names and prefixes
	public static final String PSN_TIER_GLOBAL_OVERRIDES = "class path resource [tier_global_overrides.properties]";
	public static final String PSN_APPLICATION_CONFIG = "applicationConfig: [classpath:/application.properties]";
	public static final String PSN_SYSTEM_ENVIRONMENT = "systemEnvironment";
	public static final String PSN_SYSTEM_PROPERTIES = "systemProperties";
	public static final String PSN_CONFIG_SERVER_GLOBAL = "CONFIG_SERVER_GLOBAL";
	public static final String PSN_CONFIG_SERVER_APPLICATION = "CONFIG_SERVER_APPLICATION";
	
	public static final String APPLICATION_PROPERTY_PREFIX = "application.";
	public static final String GLOBAL_PROPERTY_PREFIX = "tier_global.";
	
}

