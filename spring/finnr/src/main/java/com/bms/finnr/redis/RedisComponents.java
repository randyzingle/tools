package com.bms.finnr.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import com.bms.finnr.config.ConfigUtils;
import com.bms.finnr.config.GlobalConfiguration;

@Configuration
public class RedisComponents {
	
	@Autowired
	GlobalConfiguration globalConfig;

    @Bean
    public static PropertySourcesPlaceholderConfigurer    propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setHostName(globalConfig.getRedisClusterPrimaryEndpoint());
        factory.setPort(globalConfig.getRedisClusterPrimaryEndpointPort());
        factory.setUsePool(true);
//        ConfigUtils.bigPrint(globalConfig.getRedisClusterHost() + ": " + globalConfig.getRedisClusterPort());
        return factory;
    }

    @Bean
    RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<String, Object>();
        redisTemplate.setConnectionFactory(jedisConnectionFactory());
        return redisTemplate;
    }

    @Bean
    RedisCacheManager cacheManager() {
        RedisCacheManager redisCacheManager = new RedisCacheManager(redisTemplate());
        return redisCacheManager;
    }
}
