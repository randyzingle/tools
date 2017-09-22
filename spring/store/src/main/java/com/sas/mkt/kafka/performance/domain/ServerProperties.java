package com.sas.mkt.kafka.performance.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class ServerProperties {

	private static final String JAVA_VM_VENDER = "java.vm.vendor";
	private static final String JAVA_VERSION = "java.version";
	private static final String OS_NAME = "os.name";
	private static final String OS_VERSION = "os.version";
	private static final String OS_ARCH = "os.arch";
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	private String javaVmVendor;
	private String javaVersion;
	private String osName;
	private String osVersion;
	private String osArch;
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
	@Override
	public String toString() {
		return "ServerProperties [javaVmVendor=" + javaVmVendor + ", javaVersion=" + javaVersion + ", osName=" + osName
				+ ", osVersion=" + osVersion + ", osArch=" + osArch + "]";
	}
	
	
}
