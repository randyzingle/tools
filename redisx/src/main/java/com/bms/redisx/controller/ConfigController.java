package com.bms.redisx.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.bms.redisx.config.ApplicationConfigProperties;
import com.bms.redisx.config.ConfigEvent;
import com.bms.redisx.config.TierGlobalConfigProperties;
import com.bms.redisx.config.server.ConfigPropertyShort;
import com.bms.redisx.config.server.ConfigurationClient;

@RestController
public class ConfigController {
	
	@Autowired
	Environment env;
	
	@Autowired
	ApplicationConfigProperties appprops;
	
	@Autowired
	TierGlobalConfigProperties tgcprops;
	
	// use this to send the config event to our config application listener
	// we won't need this once the config server is hooked up to Redis
	@Autowired
    private ApplicationEventPublisher applicationEventPublisher;
	
	private String tierName = "tier_global";
	private String componentName = "";	
	private String name = "";
	
	private String auditComponent = "baldur";

	@RequestMapping(value = "/post", method = RequestMethod.POST)
	public String run(@RequestBody String s) {
		s = env.toString();
		// fire up new thread - cache stuff - etc
		return s;
	}
	
	@RequestMapping(value = "/run", method = RequestMethod.GET)
	public String run() {
		String s = env.toString();
		System.out.println(appprops);
//		checkAllProps();
		disectProps();
		// fire up new thread - cache stuff - etc
		return s;
	}
	
	@RequestMapping(value="/bark", method=RequestMethod.GET)
	public String bark() {
        System.out.println(appprops);
        System.out.println(tgcprops);
        return "bark bark";
	}
	
	@RequestMapping(value="/config", method=RequestMethod.GET)
	public String config() {
		ConfigEvent ce = new ConfigEvent(this, "config yourself!");
		applicationEventPublisher.publishEvent(ce);
		return "configged";
	}
	
	private void disectProps() {
		ConfigurableEnvironment ce = (ConfigurableEnvironment)env;
		MutablePropertySources mps = ce.getPropertySources();
		Map<String, Object> sysEnv = ce.getSystemEnvironment(); // OS environment variables
		Map<String, Object> sysProp = ce.getSystemProperties(); // Java system properties
		boolean hasApp = mps.contains("applicationConfig: [classpath:/application.properties]");
		boolean hasTG = mps.contains("class path resource [tier_global_overrides.properties]");
		boolean hasConfigApp = mps.contains("CONFIG_SERVER_APPLICATION");
		boolean hasConfigTG = mps.contains("CONFIG_SERVER_GLOBAL");
		if (hasApp) {
			PropertySource ps = mps.get("applicationConfig: [classpath:/application.properties]");
			System.out.println(ps);
		}
		if (hasTG) {
			PropertySource ps = mps.get("class path resource [tier_global_overrides.properties]");
			System.out.println(ps);
		}
		if (hasConfigApp) {
			PropertySource ps = mps.get("CONFIG_SERVER_APPLICATION");
			System.out.println(ps);
		}
		if (hasConfigTG) {
			PropertySource ps = mps.get("CONFIG_SERVER_GLOBAL");
			System.out.println(ps);
		}
	}
	
	private void printList(String name, List<ConfigPropertyShort> list) {
		System.out.println(name + ": ************************");
		for (ConfigPropertyShort cp: list) {
			System.out.println("   " + cp);
		}
		System.out.println();
	}
	
	private void checkAllProps() {
		List<ConfigPropertyShort> bucketList = getBuckets();
		printList("buckets", bucketList);
		List<ConfigPropertyShort> redisList = getRedis();
		printList("redis", redisList);
		List<ConfigPropertyShort> eventsList = getTier();
		printList("mkt-events", eventsList);
	}
	
	private List<ConfigPropertyShort> getTier() {
		List<ConfigPropertyShort> eventsList = null;
		eventsList = new ConfigurationClient().getPropertyFromConfigServer("dev", "mkt-events", "", auditComponent);	
		return eventsList;
	}
	
	private List<ConfigPropertyShort> getRedis() {
		List<ConfigPropertyShort> bucketList = null;
		bucketList = new ConfigurationClient().getPropertyFromConfigServer("tier_global", "aws-redis", "", auditComponent);	
		return bucketList;
	}
	
	private List<ConfigPropertyShort> getBuckets() {
		List<ConfigPropertyShort> bucketList = null;
		bucketList = new ConfigurationClient().getPropertyFromConfigServer("tier_global", "aws-s3-buckets", "", auditComponent);	
		return bucketList;
	}

}
