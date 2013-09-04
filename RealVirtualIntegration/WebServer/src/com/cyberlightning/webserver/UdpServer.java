package com.cyberlightning.webserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UdpServer implements MessageSender,Runnable  {
	
	private DatagramSocket serverSocket;
	private ArrayList<Client> clientList;
	private ArrayList<byte[]> receiveBuffer;
	private ArrayList<byte[]> sendBuffer;
	
	private acceptConnections = true;



	@Override
	public void run() {
		
		try {
			
		serverSocket = new DatagramSocket(Resources.SERVER_PORT_COAP);
			
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         
		byte[] receivedData = new byte[Resources.UDP_PACKET_SIZE];
        byte[] sendData = new byte[Resources.UDP_PACKET_SIZE];
        
        DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
        
        while(acceptConnections) {
        	
        	try {
				serverSocket.receive(receivedPacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        	if (receivePacket.getData()) {
        		Client _client = new Client(receivedPacket.getAddress(),receivedPacket.getPort(), Resources.CLIENT_PROTOCOL_COAP);
        		registerClient(_client);
        		parseMessage(receivedPacket);
        		receivedPacket = null;
        	}
           
           if (!sendBuffer.isEmpty()) {
        	   try {
   				serverSocket.send(sendBuffer.);
   			} catch (IOException e) {
   				// TODO Auto-generated catch block
   				e.printStackTrace();
   			}   
           }
        	
        }
   }

	@Override
	public void sendMessage(String msg) {
		// TODO Auto-generated method stub
		
	}
	private void parseMessage(DatagramPacket _packet) {
		
	}
	private void registerClient(Client _client) {
		this.clietList.add(_client); //todo add check for duplicate clients
	}

}
