package com.bms.redisx.config.redis;

import java.util.concurrent.CountDownLatch;

import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;

/**
 * This will listen on the Redis channel for configuration notifications.
 * When one arrives it will fire a ConfigEvent which will make the application re-read it's configuration 
 * from the configuration service.
 * 
 * For now it will also be used to publish a config-change event since the config server isn't doing so yet.
 * 
 * Channel name: config-event.<tiername>.<componentName>
 * For example: config-event.dev.mkt-events
 * 
 * In Redis you can pub/sub to a channel or a channel pattern:
 * SUBSCRIBE config-event.dev.mkt-events (list to config events for the dev tier for the mkt-events component)
 * PSUBSCRIBE config-event.dev.* (listen to all channels for the dev tier - all the different components)
 * 
 * @author razing
 *
 */
@Component
public class RedisHandler {
	
	@Bean
	RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
			MessageListenerAdapter listenerAdapter) {

		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.addMessageListener(listenerAdapter, new PatternTopic("chat"));

		return container;
	}

	@Bean
	MessageListenerAdapter listenerAdapter(Receiver receiver) {
		return new MessageListenerAdapter(receiver, "receiveMessage");
	}

	@Bean
	Receiver receiver(CountDownLatch latch) {
		return new Receiver(latch);
	}

	@Bean
	CountDownLatch latch() {
		return new CountDownLatch(1);
	}

	@Bean
	StringRedisTemplate template(RedisConnectionFactory connectionFactory) {
		return new StringRedisTemplate(connectionFactory);
	}
	
	@Bean
	ChannelTopic topic() {
	    return new ChannelTopic("config-event.razing.mkt-events");
	}

}
