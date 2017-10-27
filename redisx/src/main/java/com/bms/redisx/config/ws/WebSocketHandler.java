package com.bms.redisx.config.ws;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.springframework.web.socket.sockjs.frame.Jackson2SockJsMessageCodec;

import com.bms.redisx.config.ApplicationConfigProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WebSocketHandler {

	private final static WebSocketHttpHeaders headers = new WebSocketHttpHeaders();

	public ListenableFuture<StompSession> connect() {

		Transport webSocketTransport = new WebSocketTransport(new StandardWebSocketClient());
		List<Transport> transports = Collections.singletonList(webSocketTransport);

		SockJsClient sockJsClient = new SockJsClient(transports);
		sockJsClient.setMessageCodec(new Jackson2SockJsMessageCodec());

		WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);

		String url = "ws://{host}:{port}/marketingEvents/websocket/";
		return stompClient.connect(url, headers, new MyHandler(), "localhost", 8080);
	}

	private class MyHandler extends StompSessionHandlerAdapter {
		public void afterConnected(StompSession stompSession, StompHeaders stompHeaders) {
			System.out.println("Connected to WebSocket");
		}
	}

	// incoming messages
	public void subscribeProperties(StompSession stompSession) throws ExecutionException, InterruptedException {
		stompSession.subscribe("/topic/status", new StompFrameHandler() {
			public Type getPayloadType(StompHeaders stompHeaders) {
				return byte[].class;
			}

			public void handleFrame(StompHeaders stompHeaders, Object o) {
				System.out.println("Received message: " + new String((byte[]) o));
			}
		});
	}

	// outgoing messages
	public void sendStatus(StompSession stompSession, String message) {
		try {
			stompSession.send("/topic/status", message.getBytes());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void sendProperties(StompSession stompSession, ApplicationConfigProperties acp) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			String message = mapper.writeValueAsString(acp);
			stompSession.send("/topic/properties", message.getBytes());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
}
