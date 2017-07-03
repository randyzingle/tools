package com.bms.gateway.server.domain;

public class Greeting {
	private long id;
	private String greeting;
	
	public Greeting(){}
	
	public Greeting(long id, String greeting) {
		this.id = id;
		this.greeting = greeting;
	}
	
	public void setId(long id) {
		this.id = id;
	}

	public void setGreeting(String greeting) {
		this.greeting = greeting;
	}

	public long getId() {
		return id;
	}

	public String getGreeting() {
		return greeting;
	}

	@Override
	public String toString() {
		return "Greeting [id=" + id + ", greeting=" + greeting + "]";
	}
}
