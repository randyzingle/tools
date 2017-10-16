package com.bms.redis.com.bms.redis.beans;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Gets created as a bean in CentralConfig.java
 * Created by razing on 10/7/17.
 */
public class HelloBean {

    private String company;

    public HelloBean(CompanyName companyName) {
        this.company = companyName.getCompany();
    }

    /**
     * This will run up front when all the beans are being loaded into the ApplicationContext
     */
    @PostConstruct
    public void doneBuilding() {
        System.out.println("Created the HelloBean with company: " + this.company);
    }

    /**
     * This will run when the SpringBoot application is shutting down
     */
    @PreDestroy
    public void shutDown() {
        System.out.println("Killing the HelloBean");
    }

    public String sayHello() {
        return "Hello from " + company;
    }
}
