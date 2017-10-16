package com.bms.redis;

import com.bms.redis.com.bms.redis.beans.HelloBean;
import com.bms.redis.config.ButtersConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.clients.jedis.Jedis;

import java.util.concurrent.CountDownLatch;

@SpringBootApplication
public class RedisApplication {

	private static final Logger log = LoggerFactory.getLogger(RedisApplication.class);

	public static void main(String[] args) throws InterruptedException {
		ApplicationContext ctx = SpringApplication.run(RedisApplication.class, args);
		StringRedisTemplate template = ctx.getBean(StringRedisTemplate.class);
		CountDownLatch latch = ctx.getBean(CountDownLatch.class);

		log.info("Sending message...");
		template.convertAndSend("chat", "Hello from Redis!");

		 jedisStuff();

		HelloBean hello = ctx.getBean(HelloBean.class);
		System.out.println(hello.sayHello());

		Environment env = ctx.getEnvironment();
		String[] profiles = env.getActiveProfiles();
		System.out.println("Found " + profiles.length + " profiles");
		System.out.println("PROFILES: " + arrayString(profiles));

		String foo = env.getProperty("foo");
		System.out.println("FOO: " + foo);

		ButtersConfig bc = ctx.getBean(ButtersConfig.class);
		System.out.println(bc);

		String bar = env.getProperty("bar");
		System.out.println("BAR: " + bar);

		latch.await();
		System.exit(0);
	}

	public static String arrayString(String[] a) {
		StringBuffer buf = new StringBuffer();
		for (String s: a) {
			buf.append(s).append(" ");
		}
		return buf.toString();
	}

	public static void jedisStuff() {
//		Jedis jedis = new Jedis("baldur", 6379);
//		jedis.auth("bakaphel");
		Jedis jedis = new Jedis("localhost", 6379);
		jedis.set("foo", "bar");
		jedis.set("baldur", "king of all things");
		jedis.select(1);
		jedis.set("baldur", "lord of the dogs");
	}
}
