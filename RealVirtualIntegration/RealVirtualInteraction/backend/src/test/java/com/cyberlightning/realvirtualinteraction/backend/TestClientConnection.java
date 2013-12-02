package com.cyberlightning.realvirtualinteraction.backend;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import com.cyberlightning.realvirtualinteraction.backend.sockets.WebSocketWorker;

public class TestClientConnection {
	
	
	
	private WebSocketWorker webSocketWorker = new WebSocketWorker(null);
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void TestWebSocketHandshake() {
		String queryHeader = "GET /chat HTTP/1.1"+
				   "Host: server.example.com"+
				   "Upgrade: WebSocket"+
				   "Connection: Upgrade"+
				  " Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ=="+
				   "Origin: http://example.com"+
				   "Sec-WebSocket-Protocol: chat, superchat"+
				   "Sec-WebSocket-Version: 13";
		String responseHeader = "HTTP/1.1 101 Switching Protocols\r\n"+
				"Upgrade: websocket\r\n"+
				"Connection: Upgrade\r\n"+
				"Sec-WebSocket-Accept: 05s3PM535kPX5QN7f7jHbHrBr4s=\r\n\r\n";
		System.out.println("TestWebSocketHandshake(): Client query header: " + queryHeader);
		webSocketWorker.parseRequestLine(queryHeader);
		assertEquals("Sample response and test query should match",responseHeader, webSocketWorker.serverResponse);
		System.out.println("Response query should match: " + responseHeader);
		System.out.println("Expected result: true : Actual result: true ");
		
	}
}
