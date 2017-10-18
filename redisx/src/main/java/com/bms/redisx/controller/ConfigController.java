package com.bms.redisx.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.bms.redisx.config.ApplicationConfigProperties;
import com.bms.redisx.config.ConfigPropertyShort;
import com.bms.redisx.config.ConfigurationClient;

@RestController
public class ConfigController {
	
	@Autowired
	Environment env;
	
	@Autowired
	ApplicationConfigProperties props;
	
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
		System.out.println(props);
//		checkAllProps();
		disectProps();
		// fire up new thread - cache stuff - etc
		return s;
	}
	
	private void disectProps() {
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
		eventsList = ConfigurationClient.getPropertyFromConfigServer("dev", "mkt-events", "", auditComponent);	
		return eventsList;
	}
	
	private List<ConfigPropertyShort> getRedis() {
		List<ConfigPropertyShort> bucketList = null;
		bucketList = ConfigurationClient.getPropertyFromConfigServer("tier_global", "aws-redis", "", auditComponent);	
		return bucketList;
	}
	
	private List<ConfigPropertyShort> getBuckets() {
		List<ConfigPropertyShort> bucketList = null;
		bucketList = ConfigurationClient.getPropertyFromConfigServer("tier_global", "aws-s3-buckets", "", auditComponent);	
		return bucketList;
	}

}
