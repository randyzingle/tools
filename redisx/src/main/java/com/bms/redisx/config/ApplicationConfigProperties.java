package com.bms.redisx.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:application.properties")
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationConfigProperties {
	// application specific properties - set locally
	private String handlerChain;
	private String consumerGroup;
	private boolean readFromStart;
	private boolean geoAutoReload;
	private String componentName;
	private String tierName;
	private String kafkaTopicPrefix;
	// config server - passed in via ENV in EC2, application.properties in local runs
	private String configServiceUrl;
	private boolean useRedisConfig;
	
	public boolean isUseRedisConfig() {
		return useRedisConfig;
	}
	public void setUseRedisConfig(boolean useRedisConfig) {
		this.useRedisConfig = useRedisConfig;
	}
	public String getHandlerChain() {
		return handlerChain;
	}
	public void setHandlerChain(String handlerChain) {
		this.handlerChain = handlerChain;
	}
	public String getConsumerGroup() {
		return consumerGroup;
	}
	public void setConsumerGroup(String consumerGroup) {
		this.consumerGroup = consumerGroup;
	}
	public boolean isReadFromStart() {
		return readFromStart;
	}
	public void setReadFromStart(boolean readFromStart) {
		this.readFromStart = readFromStart;
	}
	public boolean isGeoAutoReload() {
		return geoAutoReload;
	}
	public void setGeoAutoReload(boolean geoAutoReload) {
		this.geoAutoReload = geoAutoReload;
	}
	public String getComponentName() {
		return componentName;
	}
	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}
	public String getTierName() {
		return tierName;
	}
	public void setTierName(String tierName) {
		this.tierName = tierName;
	}
	public String getKafkaTopicPrefix() {
		return kafkaTopicPrefix;
	}
	public void setKafkaTopicPrefix(String kafkaTopicPrefix) {
		this.kafkaTopicPrefix = kafkaTopicPrefix;
	}
	public String getConfigServiceUrl() {
		return configServiceUrl;
	}
	public void setConfigServiceUrl(String configServiceUrl) {
		this.configServiceUrl = configServiceUrl;
	}
	@Override
	public String toString() {
		return "ApplicationConfigProperties [handlerChain=" + handlerChain + ", consumerGroup=" + consumerGroup
				+ ", readFromStart=" + readFromStart + ", geoAutoReload=" + geoAutoReload + ", componentName="
				+ componentName + ", tierName=" + tierName + ", kafkaTopicPrefix=" + kafkaTopicPrefix
				+ ", configServiceUrl=" + configServiceUrl + "]";
	}

}
