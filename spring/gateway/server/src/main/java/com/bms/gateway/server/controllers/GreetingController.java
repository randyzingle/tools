package com.bms.gateway.server.controllers;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.bms.gateway.config.SimpleConfig;
import com.bms.gateway.server.domain.Greeting;

@RestController // render the result directly back to the caller (no model/view)
public class GreetingController {
	
	private static final String template = "Hello %s!";
	private final AtomicLong counter = new AtomicLong();
	
	@Autowired
	SimpleConfig config;
	
	@Value("${hello.text}")
	String hello;
	
	@RequestMapping(value="/greeting", method=RequestMethod.GET, produces="application/json")
	public Greeting getGreeting(@RequestParam(value="name", defaultValue="Baldur") String name) {
		name = config.getName();
		return new Greeting(counter.incrementAndGet(), String.format(template, name));
	}
	
	@RequestMapping(value="/greeting", method=RequestMethod.POST, consumes="application/json")
	@ResponseStatus(HttpStatus.CREATED)
	public void getGreeting(@RequestBody Greeting greeting) { // @RequestBody -> try to deserialize incoming JSON to Greeting object
		System.out.println(greeting);
	}
	
	@RequestMapping(value="/hello", method=RequestMethod.GET, produces="text/html")
	public String hello() {
		return hello;
	}
	
	@RequestMapping(value="/baldur", method=RequestMethod.GET, produces="text/plain")
	public String baldur() {
		ConfigHelper ch = new ConfigHelper();
		Integer tenantId = 120816103;
		ch.getTierNameFromTenantService(tenantId);
		return "baldur";
	}

}
