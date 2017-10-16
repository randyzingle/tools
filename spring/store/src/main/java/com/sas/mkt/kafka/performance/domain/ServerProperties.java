package com.sas.mkt.kafka.performance.domain;

import java.util.Properties;

public class ServerProperties {

	private static final String JAVA_VM_VENDER = "java.vm.vendor";
	private static final String JAVA_VERSION = "java.version";
	private static final String OS_NAME = "os.name";
	private static final String OS_VERSION = "os.version";
	private static final String OS_ARCH = "os.arch";
	
	private String javaVmVendor;
	private String javaVersion;
	private String osName;
	private String osVersion;
	private String osArch;
	private int numberProcessors;
	
//	public static void main (String[] args) {
//		System.out.println(ServerProperties.getServerProperties());
//	}
	
	public static ServerProperties getServerProperties() {
		ServerProperties sp = new ServerProperties();
		Properties p = System.getProperties();

		sp.setJavaVmVendor(p.getProperty(JAVA_VM_VENDER).toString());
		sp.setJavaVersion(p.getProperty(JAVA_VERSION).toString());
		sp.setOsName(p.getProperty(OS_NAME).toString());
		sp.setOsVersion(p.getProperty(OS_VERSION).toString());
		sp.setOsArch(p.getProperty(OS_ARCH).toString());
		sp.numberProcessors = Runtime.getRuntime().availableProcessors();

		return sp;
	}
	
	public String getJavaVmVendor() {
		return javaVmVendor;
	}
	public void setJavaVmVendor(String javaVmVendor) {
		this.javaVmVendor = javaVmVendor;
	}
	public String getJavaVersion() {
		return javaVersion;
	}
	public void setJavaVersion(String javaVersion) {
		this.javaVersion = javaVersion;
	}
	public String getOsName() {
		return osName;
	}
	public void setOsName(String osName) {
		this.osName = osName;
	}
	public String getOsVersion() {
		return osVersion;
	}
	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}
	public String getOsArch() {
		return osArch;
	}
	public void setOsArch(String osArch) {
		this.osArch = osArch;
	}
	public int getNumberProcessors() {
		return numberProcessors;
	}
	@Override
	public String toString() {
		return "ServerProperties [javaVmVendor=" + javaVmVendor + ", javaVersion=" + javaVersion + ", osName=" + osName
				+ ", osVersion=" + osVersion + ", osArch=" + osArch + "]";
	}
	
	
}
