package com.cyberlightning.webserver;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.cyberlightning.webserver.sockets.HttpSocket;
import com.cyberlightning.webserver.sockets.WebSocket;



public class Application  {

	public static Executor executor = Executors.newFixedThreadPool(StaticResources.MAX_NUM_OF_THREADS);


	public static void main(String[] args) throws Exception, IOException {
		@SuppressWarnings("unused")
		WebSocket webSocketServer = new WebSocket();
		@SuppressWarnings("unused")
		HttpSocket httpSocket = new HttpSocket();
	
		
		//CoapSocket udpServer = new CoapSocket();
		//udpServer.run();
		
	}
}
