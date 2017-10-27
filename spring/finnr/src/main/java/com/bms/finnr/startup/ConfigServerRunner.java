package com.bms.finnr.startup;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import com.bms.finnr.config.ApplicationEventListener;

@Component
public class ConfigServerRunner implements ApplicationRunner, Ordered {

	@Override
	public void run(ApplicationArguments args) throws Exception {
		ApplicationEventListener.bigPrint("in ConfigServerRunner");
		System.out.println(args.getOptionNames());
	}

	@Override
	public int getOrder() {
		return 0;
	}

}
