package com.cyberlightning.webserver;

import java.io.IOException;

import com.cyberlightning.webserver.sockets.HttpSocket;
import com.cyberlightning.webserver.sockets.WebSocket;



public class Application  {

	public static void main(String[] args) throws Exception, IOException {
		@SuppressWarnings("unused")
		HttpSocket httpServer = new HttpSocket();
		//CoapSocket udpServer = new CoapSocket();
		//udpServer.run();
		WebSocket webSocketServer = new WebSocket();
		webSocketServer.run();
	}
}
