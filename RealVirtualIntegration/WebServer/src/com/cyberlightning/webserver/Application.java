package com.cyberlightning.webserver;

import java.io.IOException;
import java.net.DatagramPacket;

import com.cyberlightning.webserver.services.JsonTranslator;
import com.cyberlightning.webserver.services.MessageService;
import com.cyberlightning.webserver.services.DataStorageService;
import com.cyberlightning.webserver.sockets.UdpSocket;
import com.cyberlightning.webserver.sockets.HttpSocket;
import com.cyberlightning.webserver.sockets.WebSocket;


public class Application  {

	public static void main(String[] args) throws Exception, IOException {
		
		
		Runnable websocket = new WebSocket();
		Thread webThread = new Thread(websocket);
		webThread.start();
		
		Runnable httpSocket = new HttpSocket();
		Thread httpThread = new Thread(httpSocket);
		httpThread.start();

		Runnable coapSocket = new UdpSocket();
		Thread coapThread = new Thread(coapSocket);
		coapThread.start();
		
		Runnable dataBase = DataStorageService.getInstance();
		Thread dbThread = new Thread(dataBase);
		dbThread.start();
	
		JsonTranslator.decodeSensorJson("{\"550e8400-e29b-41d4-a716-446655440000\":{\"attributes\":{\"name\":\"Power wall outlet\",\"address\":null},\"actuators\":[{\"uuid\":null,\"attributes\":{\"type\":\"power_switch\"},\"parameters\":{\"callback\":false},\"variables\": [{\"relay\":false, \"type\": \"boolean\" }]}],\"sensors\":[{\"uuid\":null,\"attributes\":{\"type\":\"Power sensor\"},\"parameters\":{\"options\":null},\"values\": [{\"value\": 13,\"time\":\"YY-MM-DD HH:MM\",\"unit\" : \"Celcius\"}]}]}}");
		
		MessageService.getInstance().run(); //consumes Main thread
		
	}
	
	
}
