package com.bms.finnr.startup;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import com.bms.finnr.config.ApplicationEventListener;

@Component
public class RedisRunner implements ApplicationRunner, Ordered{

	@Override
	public int getOrder() {
		return 1;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		ApplicationEventListener.bigPrint("in RedisRunner");
		System.out.println(args.getOptionNames());
	}

}
