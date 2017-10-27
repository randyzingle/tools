package com.bms.redisx.config.ws;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {
	
	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableSimpleBroker("/topic");
		// the following is for inbound ws messages. It's the prefix which will be added to the
		// rest endpoint, so /hello in the controller will be mapped to /app/hello (in @MessageMapping annotated methods)
		//config.setApplicationDestinationPrefixes("/app"); 
	}
	
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		// SockJS endpoint
		registry.addEndpoint("/websocket").withSockJS();
	}

}
