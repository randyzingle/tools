package com.bms.finnr.config;

import java.util.HashMap;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:tier_global_overrides.properties")
@ConfigurationProperties(prefix = "tier_global", ignoreUnknownFields=false, ignoreInvalidFields=false)
public class GlobalConfiguration extends Configuration {
    
	/*
	 * tier_global_ovverides.properities should only be populated in development environments to 
	 * do things like point to a locally running redis server vs the AWS redis cluster.  
	 */
	// Service Discovery
	private String mktTenantServiceUrl;
	
	// Redis
	private String redisClusterPrimaryEndpoint;
	private int redisClusterPrimaryEndpointPort;
	
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
	private String version;
	private String sasMktKafkaSchemaRegistry;

	public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public String getMktTenantServiceUrl() {
		return mktTenantServiceUrl;
	}
	public void setMktTenantServiceUrl(String mktTenantServiceUrl) {
		this.mktTenantServiceUrl = mktTenantServiceUrl;
	}
	public String getRedisClusterPrimaryEndpoint() {
		return redisClusterPrimaryEndpoint;
	}
	public void setRedisClusterPrimaryEndpoint(String redisClusterPrimaryEndpoint) {
		this.redisClusterPrimaryEndpoint = redisClusterPrimaryEndpoint;
	}
	public int getRedisClusterPrimaryEndpointPort() {
		return redisClusterPrimaryEndpointPort;
	}
	public void setRedisClusterPrimaryEndpointPort(int redisClusterPrimaryEndpointPort) {
		this.redisClusterPrimaryEndpointPort = redisClusterPrimaryEndpointPort;
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
    public String getSasMktKafkaSchemaRegistry() {
        return sasMktKafkaSchemaRegistry;
    }
    public void setSasMktKafkaSchemaRegistry(String sasMktKafkaSchemaRegistry) {
        this.sasMktKafkaSchemaRegistry = sasMktKafkaSchemaRegistry;
    }
    @Override
    public String toString() {
        return "GlobalConfiguration [mktTenantServiceUrl=" + mktTenantServiceUrl + ", redisClusterPrimaryEndpoint=" + redisClusterPrimaryEndpoint
                + ", redisClusterPrimaryEndpointPort=" + redisClusterPrimaryEndpointPort + ", dataBucketTopic=" + dataBucketTopic + ", configBucket="
                + configBucket + ", dataBucket=" + dataBucket + ", deploymentBucket=" + deploymentBucket + ", opsBucket=" + opsBucket + ", testBucket="
                + testBucket + ", topicRawEvents=" + topicRawEvents + ", topicEnhancedEvents=" + topicEnhancedEvents + ", version=" + version
                + ", sasMktKafkaSchemaRegistry=" + sasMktKafkaSchemaRegistry + "]";
    }

}