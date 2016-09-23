package com.bms.rest;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.bms.client.Quote;
import com.bms.quest.Knight;

@RestController // equivalent to @Controller + @ResponseBody
public class GreetingController {

	private static final String template = "Hello, %s";
	private final AtomicLong counter = new AtomicLong();
	
	@Autowired
	private Knight braveKnight;

	@RequestMapping("/greeting")
	public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
		return new Greeting(counter.incrementAndGet(), String.format(template, name));
	}

	@RequestMapping("/quote")
	public Quote getQuote() {
		RestTemplate rt = new RestTemplate();
		Quote quote = rt.getForObject("http://gturnquist-quoters.cfapps.io/api/random", Quote.class);
		System.out.println(quote);
		return quote;
	}

	@RequestMapping("/quest")
	public void goOnQuest() {
		braveKnight.embarkOnQuest();
	}
}
