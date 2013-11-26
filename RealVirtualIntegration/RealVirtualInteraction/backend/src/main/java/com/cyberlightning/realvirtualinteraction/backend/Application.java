package com.cyberlightning.realvirtualinteraction.backend;

import java.io.IOException;

import com.cyberlightning.realvirtualinteraction.backend.services.DataStorageService;
import com.cyberlightning.realvirtualinteraction.backend.services.MessageService;
import com.cyberlightning.realvirtualinteraction.backend.sockets.HttpSocket;
import com.cyberlightning.realvirtualinteraction.backend.sockets.UdpSocket;
import com.cyberlightning.realvirtualinteraction.backend.sockets.WebSocket;




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
