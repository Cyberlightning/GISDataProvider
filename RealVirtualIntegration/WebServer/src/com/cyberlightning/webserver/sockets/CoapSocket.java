package com.cyberlightning.webserver.sockets;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

import com.cyberlightning.webserver.StaticResources;
import com.cyberlightning.webserver.entities.Client;
import com.cyberlightning.webserver.interfaces.IMessageEvent;
import com.cyberlightning.webserver.services.MessageService;
import com.cyberlightning.webserver.services.ProfileService;

public class CoapSocket implements Runnable,IMessageEvent  {
	
	private DatagramSocket serverSocket;
	private ArrayList<DatagramPacket> sendBuffer= new ArrayList<DatagramPacket>();
	
	private int port;
	
	public CoapSocket () {
		this(StaticResources.SERVER_PORT_COAP);
		
	}
	
	public CoapSocket (int _port) {
		this.port = _port;
	}
	
	@Override
	public void run() {
		
		try {
			
		serverSocket = new DatagramSocket(this.port);
		MessageService.getInstance().registerReceiver(this);	
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         
		byte[] receivedData = new byte[StaticResources.UDP_PACKET_SIZE];
		DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
		
        
        while(true) {
        	
        	try {
				serverSocket.receive(receivedPacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        	if (receivedPacket.getData() != null) {
        		handleConnectedClient(receivedPacket);
        		MessageService.getInstance().broadcastCoapMessageEvent(receivedPacket);
        		
        	}
           
           if (!sendBuffer.isEmpty()) {
        	   try {
   				serverSocket.send(sendBuffer.get(sendBuffer.size()-1));
   				} catch (IOException e) {
   				// TODO Auto-generated catch block
   				e.printStackTrace();
   				} 
        	   this.sendBuffer.remove(sendBuffer.size() - 1);
           }
           }
	}
	
    private  void handleConnectedClient(DatagramPacket _datagramPacket) {
			Client client = new Client(_datagramPacket.getAddress().getHostAddress(), _datagramPacket.getPort(),StaticResources.CLIENT_PROTOCOL_COAP);
			ProfileService.getInstance().registerClient(client);
	}
	
	@Override
	public void httpMessageEvent(String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void coapMessageEvent(DatagramPacket _datagramPacket) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void webSocketMessageEvent(String msg) {
		// TODO Auto-generated method stub
		
	}
	
	
}
