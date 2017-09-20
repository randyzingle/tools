package com.bms.domain;

import java.sql.Timestamp;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class PerformanceRun {
	
	public PerformanceRun() {}
	
	public PerformanceRun(String runName, Timestamp startTime, Long numberOfRecords) {
		super();
		this.runName = runName;
		this.startTime = startTime;
		this.numberOfRecords = numberOfRecords;
	}

	@Id
	private String runName;
	private Timestamp startTime;
	private Long numberOfRecords;
	@OneToMany(cascade=CascadeType.ALL, mappedBy="performanceRun")
	private Set<ProducerProperties> producerProperties;
	
	public String getRunName() {
		return runName;
	}
	public void setRunName(String runName) {
		this.runName = runName;
	}
	public Timestamp getStartTime() {
		return startTime;
	}
	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}
	public Long getNumberOfRecords() {
		return numberOfRecords;
	}
	public void setNumberOfRecords(Long numberOfRecords) {
		this.numberOfRecords = numberOfRecords;
	}
	public Set<ProducerProperties> getProducerProperties() {
		return producerProperties;
	}
	public void setProducerProperties(Set<ProducerProperties> producerProperties) {
		this.producerProperties = producerProperties;
	}
	

}
