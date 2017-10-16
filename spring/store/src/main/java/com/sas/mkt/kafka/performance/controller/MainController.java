package com.sas.mkt.kafka.performance.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.sas.mkt.kafka.performance.domain.ProducerPerformanceRun;

@RestController
public class MainController {

	@RequestMapping(value="/run", method=RequestMethod.POST)
	public ProducerPerformanceRun run(@RequestBody ProducerPerformanceRun performanceRun) {
		performanceRun.setRunName(performanceRun.getRunName()+"-thread");
		// fire up new thread - cache stuff - etc
		return performanceRun;
	}
	
}
