package com.bms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.bms.properties.KafkaProperties;
import com.bms.simple.SimpleClient;

@SpringBootApplication
public class Sboot1Application {

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(Sboot1Application.class, args);
//		PathHolderBean phb = ctx.getBean(PathHolderBean.class);
		SimpleClient phb = new SimpleClient();
		System.out.println("FROM PATHHOLDER - jarPath: " + phb.getJarPath());
		System.out.println("FROM PATHHOLDER - homeDir: " + phb.getHomeDir());
		System.out.println("FROM PATHHOLDER - userDir: " + phb.getUserDir());
		String s = phb.getKeystorePath();
		if (s != null) {
			System.out.println("Found Keystore Path: " + s);
		}
		KafkaProperties kp = ctx.getBean(KafkaProperties.class);
		System.out.println("Cluster: " + kp.getCluster());
		System.out.println("SSL: " + kp.isSslRequired());
		System.out.println("Not here: " + kp.getImNotHere());
		Sboot1Application sa = new Sboot1Application();
		sa.loadfile();
	}
	
	private void loadfile() {	
		Runtime.getRuntime().exit(0);
	}
	
}
