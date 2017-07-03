package com.bms.main.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Hello {
	
	@Value("${agent.startup.msg}")
	private String startup;
	
	@RequestMapping("/hello")
	public String hello() {
		String s = "hello from rest endpoint, startup property: " + startup;
		System.out.println(s);
		return s;
	}
	
}
