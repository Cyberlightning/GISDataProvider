package com.cyberlightning.webserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

public class UdpServer implements MessageEvent,Runnable  {
	
	private DatagramSocket serverSocket;
	private ArrayList<Client> clientList;
	private ArrayList<DatagramPacket> sendBuffer;
	private ArrayList<DatagramPacket> receiveBuffer;
	
	private boolean acceptConnections = true;



	@Override
	public void run() {
		
		try {
			
		serverSocket = new DatagramSocket(Resources.SERVER_PORT_COAP);
			
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         
		byte[] receivedData = new byte[Resources.UDP_PACKET_SIZE];
		DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
		IncomingMessageHandler incomingMessageHandler = new IncomingMessageHandler();
        
        while(acceptConnections) {
        	
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
					MessageHandler.getInstance().broadcastUdpMessageEvent(datagramPacket);
					receiveBuffer.remove(messageIndex);
					
				}
				
			}
			
		}
		
		private synchronized void handleConnectedClient(DatagramPacket _datagramPacket) {
			boolean containsClient = false;
			
			for (int i = 0; i < clientList.size(); i++) {
				if (clientList.get(i).getAddress().getHostAddress().compareTo(_datagramPacket.getAddress().getHostAddress()) == 0) {
					containsClient = true;
				}
			}
			
			if (!containsClient) {
				Client udpClient = new Client(_datagramPacket.getAddress(), _datagramPacket.getPort(),Resources.CLIENT_PROTOCOL_UDP);
				udpClient.setActivityTimeStamp(System.currentTimeMillis());
				clientList.add(udpClient);
			}
		}
		

	}

	@Override
	public void httpMessageEvent(String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void udpMessageEvent(DatagramPacket _datagramPacket) {
		this.sendBuffer.add(_datagramPacket);
		
	}
	
	
}
