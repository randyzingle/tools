package com.bms.rest;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bms.rest.domain.Greeting;

// @RestController provides defaults for all methods in this class
@RestController
/* 
 * RequestMapping is option at this level and if not present will default to /
 * We could have including a path variable in this such as "{userId}/srv"
 * This would be available as @PathVariable String userId
 */
@RequestMapping("/srv") 
public class GreetingService {
	
	private static final String template = "Hello %s!";
	private final AtomicLong counter = new AtomicLong();
	
	/*
	 * Spring boot will automatically wire up an HttpMessageConverter that can convert generic
	 * POJOs to JSON and will do so unless the HTTP request has an Accept header that
	 * specifies another format.
	 * Accept application/json or NO Accept header will both return JSON by default
	 * This works both ways, incoming (POST, PUT) requests will convert JSON to Java Objects
	 */
	@RequestMapping(value="/greeting", method=RequestMethod.GET) // method will default to GET if not present
	public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name) {
		return new Greeting(counter.incrementAndGet(), String.format(template, name));
	}

}
