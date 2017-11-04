package com.bms.finnr.startup;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

import com.bms.finnr.config.ApplicationConfiguration;
import com.bms.finnr.config.ApplicationConstants;
import com.bms.finnr.config.ConfigUtils;
import com.bms.finnr.config.GlobalConfiguration;

@Component
public class PropertyDisplayRunner implements ApplicationRunner, Ordered {

	@Autowired
	Environment env;

	@Override
	public int getOrder() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		ConfigUtils.bigPrint("in PropertyDisplayRunner");
		displayPropertySources();
	}
	private void displayPropertySources() {
		ConfigurableEnvironment ce = (ConfigurableEnvironment) env;
		MutablePropertySources sources = ce.getPropertySources();
		ConfigUtils.bigPrint("Property Sources");
		int size = sources.size();
		System.out.println("Number of property sources: " + size);
		Class capp = ApplicationConfiguration.class;
		Field[] fapp = capp.getDeclaredFields();
		Class cglo = GlobalConfiguration.class;
		Field[] fglo = cglo.getDeclaredFields();
		List<Field> flist = new ArrayList<>();
		flist.addAll(Arrays.asList(fapp));
		flist.addAll(Arrays.asList(fglo));
		for (PropertySource ps : sources) {
			String name = ps.getName();
			System.out.println("Property source name: " + name);
			for (Field f : flist) {
				String s = f.getName();
				
				String prop = null;
				if (ps.getName().equals(ApplicationConstants.PSN_APPLICATION_CONFIG)) {
					String ss = ApplicationConstants.APPLICATION_PROPERTY_PREFIX + s;
					prop = (String) ps.getProperty(ss);
					if (prop != null) System.out.println("    " + s + ": " + ss + ": " + prop);
				} else if (ps.getName().equals(ApplicationConstants.PSN_TIER_GLOBAL_OVERRIDES)) {
					String ss = ApplicationConstants.GLOBAL_PROPERTY_PREFIX+ s;
					prop = (String) ps.getProperty(ss);
					if (prop != null) System.out.println("    " + s + ": " + ss + ": " + prop);
				} else if (ps.getName().equals(ApplicationConstants.PSN_CONFIG_SERVER_APPLICATION)) {
					prop = (String) ps.getProperty(s);
					if (prop != null) System.out.println("    " + s + ": " + s + ": " + prop);
				} else if (ps.getName().equals(ApplicationConstants.PSN_CONFIG_SERVER_GLOBAL)) {
                    prop = (String) ps.getProperty(s);
                    if (prop != null) System.out.println("    " + s + ": " + s + ": " + prop);
                } else if (ps.getName().equals(ApplicationConstants.PSN_SYSTEM_ENVIRONMENT)) {
					String ss = convertToEnv(s);
					prop = (String) ps.getProperty(ss);
					if (prop != null) System.out.println("    " + s + ": " + ss + ": " + prop);
				} else if (ps.getName().equals(ApplicationConstants.PSN_SYSTEM_PROPERTIES)) {
					String ss = ApplicationConstants.APPLICATION_PROPERTY_PREFIX + s;
					prop = (String) ps.getProperty(ss);
					if (prop != null) System.out.println("    " + s + ": " + ss + ": " + prop);
				}
			}
		}
	}

	private String convertToEnv(String s) {
		char[] cs = s.toCharArray();
		StringBuffer buff = new StringBuffer();
		buff.append("APPLICATION_");
		for (char c : cs) {
			if (Character.isUpperCase(c)) {
				buff.append("_");
				buff.append(c);
			} else if (c == '.') {
				buff.append("_");
			} else {
				buff.append(Character.toUpperCase(c));
			}
		}
		return buff.toString();
	}
}
