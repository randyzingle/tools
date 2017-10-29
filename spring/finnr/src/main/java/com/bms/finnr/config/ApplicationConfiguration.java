package com.bms.finnr.config;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix="application", ignoreUnknownFields=false, ignoreInvalidFields=false)
public class ApplicationConfiguration implements Configuration {

	private String componentName; // name of the application = gitlab repo name
	private String handlerChain;
	private String consumerGroup;
	private boolean readFromStart;
	private Boolean geoAutoReload;
	private String sourceTopic;
	private String destinationTopic;
	private String tierName; // name of the tier we are deploying to
	private String kafkaTopicPrefix;
	private boolean useRedisConfig;
	@NotNull
	private String configServiceUrl;
	@Min(30000) // don't hit config server more than once per 30 secs
	private int configServerPingRateMs;
	
	public String getComponentName() {
		return componentName;
	}
	public void setComponentName(String componentName) {
		this.componentName = componentName;
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
	public Boolean isGeoAutoReload() {
		return geoAutoReload;
	}
	public void setGeoAutoReload(Boolean geoAutoReload) {
		this.geoAutoReload = geoAutoReload;
	}
	public String getSourceTopic() {
		return sourceTopic;
	}
	public void setSourceTopic(String sourceTopic) {
		this.sourceTopic = sourceTopic;
	}
	public String getDestinationTopic() {
		return destinationTopic;
	}
	public void setDestinationTopic(String destinationTopic) {
		this.destinationTopic = destinationTopic;
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
	public boolean isUseRedisConfig() {
		return useRedisConfig;
	}
	public void setUseRedisConfig(boolean useRedisConfig) {
		this.useRedisConfig = useRedisConfig;
	}
	public String getConfigServiceUrl() {
		return configServiceUrl;
	}
	public void setConfigServiceUrl(String configServiceUrl) {
		this.configServiceUrl = configServiceUrl;
	}
	public int getConfigServerPingRateMs() {
		return configServerPingRateMs;
	}
	public void setConfigServerPingRateMs(int configServerPingRateMs) {
		this.configServerPingRateMs = configServerPingRateMs;
	}
	@Override
	public String toString() {
		return "ApplicationConfiguration [componentName=" + componentName + ", handlerChain=" + handlerChain
				+ ", consumerGroup=" + consumerGroup + ", readFromStart=" + readFromStart + ", geoAutoReload="
				+ geoAutoReload + ", sourceTopic=" + sourceTopic + ", destinationTopic=" + destinationTopic
				+ ", tierName=" + tierName + ", kafkaTopicPrefix=" + kafkaTopicPrefix + ", useRedisConfig="
				+ useRedisConfig + ", configServiceUrl=" + configServiceUrl + ", configServerPingRateMs="
				+ configServerPingRateMs + "]";
	}
	
	
	
}
