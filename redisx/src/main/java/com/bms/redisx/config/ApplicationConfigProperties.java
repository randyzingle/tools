package com.bms.redisx.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mkt.events", ignoreUnknownFields = false)
public class ApplicationConfigProperties {
	private String handlerChain;
	private String sourceTopic;
	private String destinationTopic;
	private String consumerGroup;
	private boolean readFromStart;
	private boolean geoAutoReload;
	private String configServiceUrl;
	private String tenantServiceUrl;
	private String kafkaTopicPrefix;
	private String tierName;

	public String getTierName() {
		return tierName;
	}

	public void setTierName(String tierName) {
		this.tierName = tierName;
	}

	public String getHandlerChain() {
		return handlerChain;
	}

	public void setHandlerChain(String handlerChain) {
		this.handlerChain = handlerChain;
	}

	public String getSourceTopic() {
		return this.sourceTopic;
	}

	public void setSourceTopic(String sourceTopic) {
		this.sourceTopic = sourceTopic;
	}

	public String getDestinationTopic() {
		return this.destinationTopic;
	}

	public void setDestinationTopic(String destinationTopic) {
		this.destinationTopic = destinationTopic;
	}

	public String getConsumerGroup() {
		return this.consumerGroup;
	}

	public void setConsumerGroup(String consumerGroup) {
		this.consumerGroup = consumerGroup;
	}

	public void setReadFromStart(boolean readFromStart) {
		this.readFromStart = readFromStart;
	}

	public boolean isReadFromStart() {
		return readFromStart;
	}

	public void setGeoAutoReload(boolean geoAutoReload) {
		this.geoAutoReload = geoAutoReload;
	}

	public boolean isGeoAutoReload() {
		return geoAutoReload;
	}

	public String getConfigServiceUrl() {
		return configServiceUrl;
	}

	public void setConfigServiceUrl(String configServiceUrl) {
		this.configServiceUrl = configServiceUrl;
	}

	public String getTenantServiceUrl() {
		return tenantServiceUrl;
	}

	public void setTenantServiceUrl(String tenantServiceUrl) {
		this.tenantServiceUrl = tenantServiceUrl;
	}

	public String getKafkaTopicPrefix() {
		return this.kafkaTopicPrefix;
	}

	public void setKafkaTopicPrefix(String kafkaTopicPrefix) {
		this.kafkaTopicPrefix = kafkaTopicPrefix;
	}

}
