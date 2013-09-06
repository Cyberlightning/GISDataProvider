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

public class CoapSocket implements IMessageEvent,Runnable  {
	
	private DatagramSocket serverSocket;
	private ArrayList<DatagramPacket> sendBuffer;
	private ArrayList<DatagramPacket> receiveBuffer;

	@Override
	public void run() {
		
		try {
			
		serverSocket = new DatagramSocket(StaticResources.SERVER_PORT_COAP);
			
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         
		byte[] receivedData = new byte[StaticResources.UDP_PACKET_SIZE];
		DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
		IncomingMessageHandler incomingMessageHandler = new IncomingMessageHandler();
        
        while(true) {
        	
        	try {
				serverSocket.receive(receivedPacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        	if (receivedPacket.getData() != null) {
        		
        		if (!incomingMessageHandler.isRunning) incomingMessageHandler.run(); //lazy initialization
        		handleIncomingMessage(receivedPacket);
        		receivedPacket = null; 
        	}
           
           if (!sendBuffer.isEmpty()) {
        	   try {
   				serverSocket.send(sendBuffer.get(0));
   				} catch (IOException e) {
   				// TODO Auto-generated catch block
   				e.printStackTrace();
   				}   
           }
        	
        }
	}
	
	private synchronized void handleIncomingMessage(DatagramPacket _datagramPacket) { 
		receiveBuffer.add(_datagramPacket);
	}
	
	class IncomingMessageHandler extends Thread {
		
		public boolean isRunning = false;
		
		@Override
		public void run() {
			
			isRunning = true;
			
			while (true) {
				
				if (receiveBuffer.size() > 0) {
					
					int messageIndex = receiveBuffer.size() - 1;
					DatagramPacket datagramPacket = new DatagramPacket(receiveBuffer.get(messageIndex).getData(),receiveBuffer.get(messageIndex).getData().length);
					handleConnectedClient(datagramPacket);
					MessageService.getInstance().broadcastUdpMessageEvent(datagramPacket);
					receiveBuffer.remove(messageIndex);
					
				}
				
			}
			
		}
		
		private synchronized void handleConnectedClient(DatagramPacket _datagramPacket) {
			Client client = new Client(_datagramPacket.getAddress().getHostAddress(), _datagramPacket.getPort(),StaticResources.CLIENT_PROTOCOL_COAP);
			ProfileService.getInstance().registerClient(client);
		}
		

	}

	@Override
	public void httpMessageEvent(String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void coapMessageEvent(DatagramPacket _datagramPacket) {
		this.sendBuffer.add(_datagramPacket);
		
	}

	@Override
	public void webSocketMessageEvent(String msg) {
		// TODO Auto-generated method stub
		
	}
	
	
}
