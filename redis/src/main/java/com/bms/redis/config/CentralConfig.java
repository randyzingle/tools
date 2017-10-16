package com.bms.redis.config;

import com.bms.redis.com.bms.redis.beans.CompanyName;
import com.bms.redis.com.bms.redis.beans.HelloBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

/**
 * Created by razing on 10/7/17.
 */
@Configuration
@PropertySource("classpath:baldur.properties")
public class CentralConfig {

    @Autowired
    Environment env;

    @Bean
    public HelloBean helloBean(CompanyName companyName) {
        System.out.println("In hello bean");
        System.out.println("FOO in helloBean: " + env.getProperty("foo"));
        System.out.println("testbean.name in helloBean: " + env.getProperty("testbean.name"));
        System.out.println("foo.name in helloBean: " + env.getProperty("foo.name"));
        return new HelloBean(companyName);
    }

    @Bean
    public ButtersConfig getButters() {
        return new ButtersConfig();
    }

}
