package com.sas.mkt.kafka.performance.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class FlatRun {
	
	@Id
	private String runName;
	private Long messagesPerSec;
	private Long kbytesPerSec;
	private String status;
	private String instanceType;
	private Long numberRecordsAttempted;
	private Double percentMessagesReceived;
	private Long averageRecordSizeBytes;
	private Long numberKeys;
	private Long numberPartitions;
	private int numberProcessors;
	private String retries;
	private String compressionType;
	private String acks;
	private String batchSize;
	private String lingerMs;
	private String maxInFlightRequestsPerConnection;
	public String getRunName() {
		return runName;
	}
	public void setRunName(String runName) {
		this.runName = runName;
	}
	public Long getMessagesPerSec() {
		return messagesPerSec;
	}
	public void setMessagesPerSec(Long messagesPerSec) {
		this.messagesPerSec = messagesPerSec;
	}
	public Long getKbytesPerSec() {
		return kbytesPerSec;
	}
	public void setKbytesPerSec(Long kbytesPerSec) {
		this.kbytesPerSec = kbytesPerSec;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getInstanceType() {
		return instanceType;
	}
	public void setInstanceType(String instanceType) {
		this.instanceType = instanceType;
	}
	public Long getNumberRecordsAttempted() {
		return numberRecordsAttempted;
	}
	public void setNumberRecordsAttempted(Long numberRecordsAttempted) {
		this.numberRecordsAttempted = numberRecordsAttempted;
	}
	public Double getPercentMessagesReceived() {
		return percentMessagesReceived;
	}
	public void setPercentMessagesReceived(Double percentMessagesReceived) {
		this.percentMessagesReceived = percentMessagesReceived;
	}
	public Long getAverageRecordSizeBytes() {
		return averageRecordSizeBytes;
	}
	public void setAverageRecordSizeBytes(Long averageRecordSizeBytes) {
		this.averageRecordSizeBytes = averageRecordSizeBytes;
	}
	public Long getNumberKeys() {
		return numberKeys;
	}
	public void setNumberKeys(Long numberKeys) {
		this.numberKeys = numberKeys;
	}
	public Long getNumberPartitions() {
		return numberPartitions;
	}
	public void setNumberPartitions(Long numberPartitions) {
		this.numberPartitions = numberPartitions;
	}
	public int getNumberProcessors() {
		return numberProcessors;
	}
	public void setNumberProcessors(int numberProcessors) {
		this.numberProcessors = numberProcessors;
	}
	public String getRetries() {
		return retries;
	}
	public void setRetries(String retries) {
		this.retries = retries;
	}
	public String getCompressionType() {
		return compressionType;
	}
	public void setCompressionType(String compressionType) {
		this.compressionType = compressionType;
	}
	public String getAcks() {
		return acks;
	}
	public void setAcks(String acks) {
		this.acks = acks;
	}
	public String getBatchSize() {
		return batchSize;
	}
	public void setBatchSize(String batchSize) {
		this.batchSize = batchSize;
	}
	public String getLingerMs() {
		return lingerMs;
	}
	public void setLingerMs(String lingerMs) {
		this.lingerMs = lingerMs;
	}
	public String getMaxInFlightRequestsPerConnection() {
		return maxInFlightRequestsPerConnection;
	}
	public void setMaxInFlightRequestsPerConnection(String maxInFlightRequestsPerConnection) {
		this.maxInFlightRequestsPerConnection = maxInFlightRequestsPerConnection;
	}
	
	

}
