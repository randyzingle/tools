package com.bms.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Class that holds all of the amazon-prefixed properties
 * @author razing
 * We have these properites in application.properties
 * amazon.amazonID=baldur1
 * amazon.url=www.amazon.com
 * amazon.password=secretstuff
 *
 */
@Component // load this into the application context
@ConfigurationProperties("amazon") // load all properties that have amazon. prefix
public class AmazonProperties {
	private String amazonID;
	private String url;
	private String password;
	
	// setter methods that will be used by property injection
	public void setAmazonID(String amazonID) {
		this.amazonID = amazonID;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	// getter methods that allow other classes to access these values
	public String getAmazonID() {
		return amazonID;
	}
	public String getUrl() {
		return url;
	}
	public String getPassword() {
		return password;
	}
	

}
