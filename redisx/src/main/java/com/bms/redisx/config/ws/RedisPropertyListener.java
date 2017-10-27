package com.bms.redisx.config.ws;

import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import com.bms.redisx.config.ApplicationConfigProperties;

@Service
public class RedisPropertyListener {
	
	private WebSocketHandler webSocketHandler;
	private StompSession stompSession;

	public RedisPropertyListener() {
		this.webSocketHandler = new WebSocketHandler();
		try {
			ListenableFuture<StompSession> f = webSocketHandler.connect();
			this.stompSession = f.get();
			System.out.println("WebSocket Connected:: " + stompSession.isConnected());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void sendApplicationProperties(ApplicationConfigProperties acp) {
		webSocketHandler.sendProperties(stompSession, acp);
	}
}
