package com.cyberlightning.webserver;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.cyberlightning.webserver.services.TranslationService;
import com.cyberlightning.webserver.sockets.CoapSocket;
import com.cyberlightning.webserver.sockets.HttpSocket;
import com.cyberlightning.webserver.sockets.WebSocket;



public class Application  {




	public static void main(String[] args) throws Exception, IOException {
		
		
		//@SuppressWarnings("unused")
		WebSocket webSocket = new WebSocket(StaticResources.WEB_SOCKET_PORT);
		webSocket.run();
		//webSocket.start();
		
		@SuppressWarnings("unused")
		HttpSocket httpSocket = new HttpSocket();
		httpSocket.start();
		@SuppressWarnings("unused")
		CoapSocket coapSocket = new CoapSocket();
		coapSocket.start();
	
		
	}
}
