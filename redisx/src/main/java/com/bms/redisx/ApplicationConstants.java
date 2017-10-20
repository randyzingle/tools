package com.bms.redisx;

/**
 * These constants are mainly used to lookup configuration properties in the various places that config properties are found.
 * The <b>values</b> for the Strings must match the <b>names</b> of the properties in application.properties, config service, etc. 
 * 
 * So, for example, we should see something like this in the application.properties file:
 * <code>
 * consumerGroup=RawEventsEnrichmentReader
 * </code>
 * 
 * And something like this as a config server entry:
 * <code>
 * tierNm=dev
 * componentNm=mkt-events
 * name=consumerGroup
 * value=RawEventsEnrichmentReader
 * </code>
 * 
 * And something like this as an environment variable:
 * <code>
 * CONSUMER_GROUP=RawEventsEnrichmentReader
 * </code>
 * 
 * @author razing
 *
 */
public class ApplicationConstants {
	
	/*
	 * Component Names in config server
	 */
	public static final String COMPONENTNM_SERVICE_DISCOVERY = "service_discovery";
	public static final String COMPONENTNM_AWS_REDIS = "aws-redis";
	public static final String COMPONENTNM_AWS_S3_BUCKETS = "aws-s3-buckets";
	public static final String COMPONENTNM_MKT_KAFKA = "mkt-kafka";
	
	/*
	 * Application Specific properties
	 */
	public static final String HANDLER_CHAIN = "handlerChain";
	public static final String CONSUMER_GROUP = "consumerGroup";
	public static final String READ_FROM_START = "readFromStart";
	public static final String GEO_AUTO_RELOAD = "geoAutoReload";
	public static final String KAFKA_TOPIC_PREFIX = "kafkaTopicPrefix";
	public static final String TIER_NAME = "tierName"; 
	
	/*
	 * Tier Global properties
	 */
	// Service Discovery, componentNm=service_discovery
	public static final String TENANT_SERVICE_URL = "mkt-tenant.service_URL";
	
	// Kafka, componentNm=mkt-kafka
	public static final String TOPIC_NAME_RAW_EVENTS = "topic.rawEvents";
	public static final String TOPIC_NAME_ENHANCED_EVENTS = "topic.enhancedEvents";
	
	// Redis, componentNm=aws-redis
	public static final String REDIS_HOST = "redis_cluster_primary_endpoint";
	public static final String REDIS_PORT = "redis_cluster_primary_endpoint_port";
	
	// S3 Buckets, componentNm=aws-s3-buckets
	public static final String S3_DATA_BUCKET_TOPIC = "data_bucket_topic";
	public static final String S3_CONFIG_BUCKET = "config_bucket";
	public static final String S3_DATA_BUCKET = "data_bucket";
	public static final String S3_DEPLOYMENT_BUCKET = "deployment_bucket";
	public static final String S3_OPS_BUCKET = "ops_bucket";
	public static final String S3_TEST_BUCKET = "test_bucket";
	
}
