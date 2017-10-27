package com.bms.finnr.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="application", ignoreUnknownFields=false, ignoreInvalidFields=false)
public class ApplicationConfiguration {

	private String name;
	private Boolean redisEnabled;
	private boolean configServerEnabled;
	private int configServerPingRateMin;
	private String foo;
	private String bar;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Boolean getRedisEnabled() {
		return redisEnabled;
	}
	public void setRedisEnabled(Boolean redisEnabled) {
		this.redisEnabled = redisEnabled;
	}
	public boolean isConfigServerEnabled() {
		return configServerEnabled;
	}
	public void setConfigServerEnabled(boolean configServerEnabled) {
		this.configServerEnabled = configServerEnabled;
	}
	public int getConfigServerPingRateMin() {
		return configServerPingRateMin;
	}
	public void setConfigServerPingRateMin(int configServerPingRateMin) {
		this.configServerPingRateMin = configServerPingRateMin;
	}
	public String getFoo() {
		return foo;
	}
	public void setFoo(String foo) {
		this.foo = foo;
	}
	public String getBar() {
		return bar;
	}
	public void setBar(String bar) {
		this.bar = bar;
	}
	@Override
	public String toString() {
		return "ApplicationConfiguration [name=" + name + ", redisEnabled=" + redisEnabled + ", configServerEnabled="
				+ configServerEnabled + ", configServerPingRateMin=" + configServerPingRateMin + ", foo=" + foo
				+ ", bar=" + bar + "]";
	}
	
	
}
