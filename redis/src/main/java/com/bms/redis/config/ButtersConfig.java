package com.bms.redis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by razing on 10/8/17.
 */
@ConfigurationProperties(prefix="butters")
public class ButtersConfig {

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    private String name;
    private String age;
    private String color;

    @Override
    public String toString() {
        return "ButtersConfig{" +
                "name='" + name + '\'' +
                ", age='" + age + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}
