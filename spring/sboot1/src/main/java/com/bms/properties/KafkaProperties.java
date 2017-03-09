package com.bms.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component // load this into the application context
@ConfigurationProperties("kafka") // load all properties that have kafka prefix
public class KafkaProperties {
	
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

	public String getImNotHere() {
		return imNotHere;
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
