package com.bms.redisx.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:tier_global.properties")
@ConfigurationProperties(prefix = "tier_global", ignoreUnknownFields = false)
public class TierGlobalConfigProperties {
	
	/*
	 * Pull these from the config server
	 */
	// Service Discovery
	private String tenantServiceUrl;
	
	// Redis
	private String redisClusterHost;
	private int redisClusterPort;
	
	// S3 Buckets
	private String dataBucketTopic;
	private String configBucket;
	private String dataBucket;
	private String deploymentBucket;
	private String opsBucket;
	private String testBucket;
	
	// Kafka
	private String topicRawEvents;
	private String topicEnhancedEvents;
	
	public String getTenantServiceUrl() {
		return tenantServiceUrl;
	}
	public void setTenantServiceUrl(String tenantServiceUrl) {
		this.tenantServiceUrl = tenantServiceUrl;
	}
	public String getRedisClusterHost() {
		return redisClusterHost;
	}
	public void setRedisClusterHost(String redisClusterHost) {
		this.redisClusterHost = redisClusterHost;
	}
	public int getRedisClusterPort() {
		return redisClusterPort;
	}
	public void setRedisClusterPort(int redisClusterPort) {
		this.redisClusterPort = redisClusterPort;
	}
	public String getDataBucketTopic() {
		return dataBucketTopic;
	}
	public void setDataBucketTopic(String dataBucketTopic) {
		this.dataBucketTopic = dataBucketTopic;
	}
	public String getConfigBucket() {
		return configBucket;
	}
	public void setConfigBucket(String configBucket) {
		this.configBucket = configBucket;
	}
	public String getDataBucket() {
		return dataBucket;
	}
	public void setDataBucket(String dataBucket) {
		this.dataBucket = dataBucket;
	}
	public String getDeploymentBucket() {
		return deploymentBucket;
	}
	public void setDeploymentBucket(String deploymentBucket) {
		this.deploymentBucket = deploymentBucket;
	}
	public String getOpsBucket() {
		return opsBucket;
	}
	public void setOpsBucket(String opsBucket) {
		this.opsBucket = opsBucket;
	}
	public String getTestBucket() {
		return testBucket;
	}
	public void setTestBucket(String testBucket) {
		this.testBucket = testBucket;
	}
	public String getTopicRawEvents() {
		return topicRawEvents;
	}
	public void setTopicRawEvents(String topicRawEvents) {
		this.topicRawEvents = topicRawEvents;
	}
	public String getTopicEnhancedEvents() {
		return topicEnhancedEvents;
	}
	public void setTopicEnhancedEvents(String topicEnhancedEvents) {
		this.topicEnhancedEvents = topicEnhancedEvents;
	}
	@Override
	public String toString() {
		return "TierGlobalConfigProperties [tenantServiceUrl="
				+ tenantServiceUrl + ", redisClusterHost=" + redisClusterHost + ", redisClusterPort=" + redisClusterPort
				+ ", dataBucketTopic=" + dataBucketTopic + ", configBucket=" + configBucket + ", dataBucket="
				+ dataBucket + ", deploymentBucket=" + deploymentBucket + ", opsBucket=" + opsBucket + ", testBucket="
				+ testBucket + ", topicRawEvents=" + topicRawEvents + ", topicEnhancedEvents=" + topicEnhancedEvents
				+ "]";
	}
	

}
