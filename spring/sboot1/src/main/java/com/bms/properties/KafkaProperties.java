package com.bms.properties;

import java.util.HashMap;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component // load this into the application context
@ConfigurationProperties("kafka") // load all properties that have kafka prefix
public class KafkaProperties {
	
	private HashMap<String, String> kp;
	public static String KAFKA_CLUSTER = "KAFKA_CLUSTER";
	public static String KAFKA_SCHEMA_REGISTRY = "KAFKA_SCHEMA_REGISTRY";
	public static String KAFKA_SSL_REQUIRED = "KAFKA_SSL_REQUIRED";
	public static String KAFKA_CLIENT_AUTH_REQUIRED = "KAFKA_CLIENT_AUTH_REQUIRED";
	public static String KAFKA_CONNECT_DISTRIBUTED = "KAFKA_CONNECT_DISTRIBUTED";
	public static String KAFKA_CERTIFICATE_STORE_ROOTNAME = "KAFKA_CERTIFICATE_STORE_ROOTNAME";
	public static String KAFKA_KEYSTORE_STORE_PASSWORD = "KAFKA_KEYSTORE_STORE_PASSWORD";
	public static String KAFKA_KEYSTORE_KEY_PASSWORD = "KAFKA_KEYSTORE_KEY_PASSWORD";
	public static String KAFKA_TRUSTSTORE_STORE_PASSWORD = "KAFKA_TRUSTSTORE_STORE_PASSWORD";
	public static String KAFKA_S3_STORE_PATH = "KAFKA_S3_STORE_PATH";
	
	private String cluster;
	private String schemaRegistry;
	private boolean sslRequired;
	private boolean clientAuthRequired;
	private String connectDistributed;
	private String certificateStoreRootname;
	private String keystoreStorePassword;
	private String keystoreKeyPassword;
	private String truststoreStorePassword;
	private String imNotHere;
	private String s3StorePath;
	
	public HashMap<String, String> getKafkaProperties() {
		kp = new HashMap<>();
		kp.put(KAFKA_CLUSTER, getCluster());
		kp.put(KAFKA_SCHEMA_REGISTRY, getSchemaRegistry());
		kp.put(KAFKA_SSL_REQUIRED, Boolean.toString(isSslRequired()));
		kp.put(KAFKA_CLIENT_AUTH_REQUIRED, Boolean.toString(isClientAuthRequired()));
		kp.put(KAFKA_CONNECT_DISTRIBUTED, getConnectDistributed());
		kp.put(KAFKA_CERTIFICATE_STORE_ROOTNAME, getCertificateStoreRootname());
		kp.put(KAFKA_KEYSTORE_STORE_PASSWORD, getKeystoreStorePassword());
		kp.put(KAFKA_KEYSTORE_KEY_PASSWORD, getKeystoreKeyPassword());
		kp.put(KAFKA_TRUSTSTORE_STORE_PASSWORD, getTruststoreStorePassword());
		kp.put(KAFKA_S3_STORE_PATH, getS3StorePath());
		return kp;
	}

	public String getS3StorePath() {
		return s3StorePath;
	}

	public void setS3StorePath(String s3StorePath) {
		this.s3StorePath = s3StorePath;
	}

	public String getImNotHere() {
		return imNotHere;
	}

	@Override
	public String toString() {
		return "KafkaProperties [cluster=" + cluster + ", schemaRegistry=" + schemaRegistry + ", sslRequired="
				+ sslRequired + ", clientAuthRequired=" + clientAuthRequired + ", connectDistributed="
				+ connectDistributed + ", certificateStoreRootname=" + certificateStoreRootname
				+ ", keystoreStorePassword=" + keystoreStorePassword + ", keystoreKeyPassword=" + keystoreKeyPassword
				+ ", truststoreStorePassword=" + truststoreStorePassword + ", imNotHere=" + imNotHere + ", s3StorePath="
				+ s3StorePath + "]";
	}

	public void setImNotHere(String imNotHere) {
		this.imNotHere = imNotHere;
	}

	public String getCluster() {
		return cluster;
	}

	public void setCluster(String cluster) {
		this.cluster = cluster;
	}

	public String getSchemaRegistry() {
		return schemaRegistry;
	}

	public void setSchemaRegistry(String schemaRegistry) {
		this.schemaRegistry = schemaRegistry;
	}

	public boolean isSslRequired() {
		return sslRequired;
	}

	public void setSslRequired(boolean sslRequired) {
		this.sslRequired = sslRequired;
	}

	public boolean isClientAuthRequired() {
		return clientAuthRequired;
	}

	public void setClientAuthRequired(boolean clientAuthRequired) {
		this.clientAuthRequired = clientAuthRequired;
	}

	public String getConnectDistributed() {
		return connectDistributed;
	}

	public void setConnectDistributed(String connectDistributed) {
		this.connectDistributed = connectDistributed;
	}

	public String getCertificateStoreRootname() {
		return certificateStoreRootname;
	}

	public void setCertificateStoreRootname(String certificateStoreRootname) {
		this.certificateStoreRootname = certificateStoreRootname;
	}

	public String getKeystoreStorePassword() {
		return keystoreStorePassword;
	}

	public void setKeystoreStorePassword(String keystoreStorePassword) {
		this.keystoreStorePassword = keystoreStorePassword;
	}

	public String getKeystoreKeyPassword() {
		return keystoreKeyPassword;
	}

	public void setKeystoreKeyPassword(String keystoreKeyPassword) {
		this.keystoreKeyPassword = keystoreKeyPassword;
	}

	public String getTruststoreStorePassword() {
		return truststoreStorePassword;
	}

	public void setTruststoreStorePassword(String truststoreStorePassword) {
		this.truststoreStorePassword = truststoreStorePassword;
	}
}
