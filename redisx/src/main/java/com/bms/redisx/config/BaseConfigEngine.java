package com.bms.redisx.config;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;

@Component
public class BaseConfigEngine {
	
	@Autowired
	Environment env;
	
	public void disectProps() {
		ConfigurableEnvironment ce = (ConfigurableEnvironment)env;
		MutablePropertySources mps = ce.getPropertySources();
		Map<String, Object> sysEnv = ce.getSystemEnvironment();
		Map<String, Object> sysProp = ce.getSystemProperties();
	
		Set<String> envset = sysEnv.keySet();
		Set<String> proset = sysProp.keySet();
		System.out.println(envset);
		System.out.println(proset);
		System.out.println();
	}

}
