package com.bms.finnr.startup;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import com.bms.finnr.config.ConfigUtils;
import com.bms.finnr.config.GlobalConfiguration;

/**
 * Configure the Redis connect. By default we'll used the connection info in the tier_global configuration
 * server component named "aws-redis". We can override this for local development runs by populating the
 * following in tier_global_overrides.properties:
 * <ul>
 * <li>tier_global.redisClusterHost=localhost</li>
 * <li>tier_global.redisClusterPort=6379</li>
 * </ul>
 * 
 * This is the second ApplicationRunner that runs (getOrder() = 1)
 * 
 * @author razing
 *
 */
@Component
public class RedisRunner implements ApplicationRunner, Ordered {
	
	@Autowired
	GlobalConfiguration globalConfig;
	
	@Autowired
	JedisConnectionFactory jedisConnectionFactory;
	
	@Autowired
	RedisTemplate<String, Object> redisTemplate;

	@Override
	public int getOrder() {
		return 2;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
//		ConfigUtils.bigPrint("in RedisRunner");
//		System.out.println("Hitting jedisConnectionFactory");
//		jedisConnectionFactory.setHostName(globalConfig.getRedisClusterHost());
//		jedisConnectionFactory.setPort(globalConfig.getRedisClusterPort());
//		System.out.println("Hitting redisTemplate");
//		redisTemplate.setConnectionFactory(jedisConnectionFactory);
//		ConfigUtils.bigPrint("JEDIS: " + globalConfig.getRedisClusterHost() + ": " + globalConfig.getRedisClusterPort());
//		setSimple();
	}
	
	private void setSimple() {
		int db = jedisConnectionFactory.getDatabase();
		System.out.println("Redis Database: " + db);
		ValueOperations<String, Object> values = redisTemplate.opsForValue();
		String key = "baldur";
		String value = new String("hello baldur on: " + new Date().toString());
//		values.set(key, value, 60L, TimeUnit.SECONDS);
//		key = "mymir";
//		value = new String("hello baldur on: " + new Date().toString());
//		values.set(key, value, 600L, TimeUnit.SECONDS);
		System.out.println("Baldur in Redis: " + values.get("baldur"));
		System.out.println("Mymir in Redis: " + values.get("mymir"));
	}

}
