package com.bms.rest;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bms.rest.domain.Greeting;

import io.swagger.annotations.ApiOperation;

/* 
 * @RestController (@Controller & @ResponseBody) exposes this class to web requests
 * @Controller - stereotype annotation (like @Bean & @Repository) - declares the class to be an MVC Controller
 * @ResponseBody - indicates that responses from the rest endpoints constitute the entire content of the HTTP
 * reponse body payload 
 * 
 * @RequestMapping is optional at this level and if not present will default to /
 * We could have including a path variable in this such as "{userId}/srv"
 * This would be available as @PathVariable String userId
 */
@RestController
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
	 * 
	 * @ApiOperation add Swagger docs for the endpoint
	 * @RequestMapping here adds to the path so this will be at http://www.baldursoft.com/<contextroot>/srv/greeting?name=mymir
	 * @RequestParameter add a ?name=value type parameter reader, with an optional default value. 
	 */
	@ApiOperation(value = "Foo Value", notes = "Bar Notes")
	@RequestMapping(value="/greeting", method=RequestMethod.GET) // method will default to GET if not present
	public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name) {
		return new Greeting(counter.incrementAndGet(), String.format(template, name));
	}

}
