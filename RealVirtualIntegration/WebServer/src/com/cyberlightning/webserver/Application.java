package com.cyberlightning.webserver;

import java.io.IOException;

import com.cyberlightning.webserver.services.MessageService;
import com.cyberlightning.webserver.services.DataStorageService;
import com.cyberlightning.webserver.sockets.UdpSocket;
import com.cyberlightning.webserver.sockets.HttpSocket;
import com.cyberlightning.webserver.sockets.WebSocket;


public class Application  {

	public static void main(String[] args) throws Exception, IOException {
		
		Runnable dataBase = DataStorageService.getInstance();
		Thread dbThread = new Thread(dataBase);
		dbThread.start();
		
		Runnable websocket = new WebSocket();
		Thread webThread = new Thread(websocket);
		webThread.start();
		
		Runnable httpSocket = new HttpSocket();
		Thread httpThread = new Thread(httpSocket);
		httpThread.start();

		Runnable coapSocket = new UdpSocket();
		Thread coapThread = new Thread(coapSocket);
		coapThread.start();
		
		MessageService.getInstance().startThread();

	}
	
	
}
