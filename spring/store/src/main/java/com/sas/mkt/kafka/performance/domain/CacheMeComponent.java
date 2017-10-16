package com.sas.mkt.kafka.performance.domain;

import java.util.ArrayList;

import org.springframework.stereotype.Component;

@Component
public class CacheMeComponent {

	private String name;
	private ArrayList<String> data;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<String> getData() {
		return data;
	}
	public void setData(ArrayList<String> data) {
		this.data = data;
	}
	
}
