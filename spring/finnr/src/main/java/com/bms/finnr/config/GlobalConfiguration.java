package com.bms.finnr.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="tier_global", ignoreUnknownFields=false, ignoreInvalidFields=false)
public class GlobalConfiguration {
	private String redisHost;
	private int redisPort;
	private String configServiceUrl;
	public String getRedisHost() {
		return redisHost;
	}
	public void setRedisHost(String redisHost) {
		this.redisHost = redisHost;
	}
	public int getRedisPort() {
		return redisPort;
	}
	public void setRedisPort(int redisPort) {
		this.redisPort = redisPort;
	}
	public String getConfigServiceUrl() {
		return configServiceUrl;
	}
	public void setConfigServiceUrl(String configServiceUrl) {
		this.configServiceUrl = configServiceUrl;
	}
	@Override
	public String toString() {
		return "GlobalConfiguration [redisHost=" + redisHost + ", redisPort=" + redisPort + ", configServiceUrl="
				+ configServiceUrl + "]";
	}
	
}
