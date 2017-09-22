package com.sas.mkt.kafka.performance.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class ProducerProperty {
	
	public ProducerProperty() {};
	
	public ProducerProperty(String name, String value, String type) {
		super();
		this.name = name;
		this.value = value;
		this.type = type;
	}

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String value;
	private String type;
	
	@JsonIgnore
	@ManyToOne
	private ProducerPerformanceRun performanceRun;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ProducerPerformanceRun getPerformanceRun() {
		return performanceRun;
	}

	public void setPerformanceRun(ProducerPerformanceRun performanceRun) {
		this.performanceRun = performanceRun;
	}

	@Override
	public String toString() {
		return "ProducerProperty [name=" + name + ", value=" + value + ", type=" + type + "]";
	}

}
