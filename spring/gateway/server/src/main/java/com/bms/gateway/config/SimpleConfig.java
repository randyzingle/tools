package com.bms.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SimpleConfig {
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	/*
	 * Order of priority to look up this value
	 * 1. System property: -Ddog.name="Butters"
	 * 2. Environment variable: DOG_NAME=Baldur
	 * 3. application.properties: dog.name=Mymir
	 * 4. default value given after the ':' - Sam
	 */

	@Value("${dog.name:Sam}")
	private String name;
	
}
