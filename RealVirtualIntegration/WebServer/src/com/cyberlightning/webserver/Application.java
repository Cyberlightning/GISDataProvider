package com.cyberlightning.webserver;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.cyberlightning.webserver.services.TranslationService;
import com.cyberlightning.webserver.sockets.CoapSocket;
import com.cyberlightning.webserver.sockets.HttpSocket;
import com.cyberlightning.webserver.sockets.WebSocket;
import com.cyberlightning.webserver.sockets.WebSocket2;
import com.cyberlightning.webserver.sockets.WebSocket3;



public class Application  {




	public static void main(String[] args) throws Exception, IOException {
		
		System.out.print("webserverstarted");
		//@SuppressWarnings("unused")
		WebSocket2 webSocket = new WebSocket2();
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
