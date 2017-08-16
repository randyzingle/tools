package com.bms.gateway.server.controllers;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.bms.gateway.server.domain.Greeting;

@RestController
public class GreetingController {
	
	private static final String template = "Hello %s!";
	private final AtomicLong counter = new AtomicLong();
	
	@RequestMapping(value="/greeting", method=RequestMethod.GET, produces="application/json")
	public Greeting getGreeting(@RequestParam(value="name", defaultValue="Baldur") String name) {
		return new Greeting(counter.incrementAndGet(), String.format(template, name));
	}
	
	@RequestMapping(value="/greeting", method=RequestMethod.POST, consumes="application/json")
	@ResponseStatus(HttpStatus.CREATED)
	public void getGreeting(@RequestBody Greeting greeting) {
		System.out.println(greeting);
	}

}
