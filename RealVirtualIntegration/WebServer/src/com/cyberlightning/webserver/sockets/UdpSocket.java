package com.cyberlightning.webserver.sockets;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.cyberlightning.webserver.StaticResources;
import com.cyberlightning.webserver.interfaces.IMessageEvent;
import com.cyberlightning.webserver.services.MessageService;

public class UdpSocket implements Runnable  {
	
	private DatagramSocket serverSocket;
	private int port;
	
	public static final String uuid = UUID.randomUUID().toString();
	public  final int type = StaticResources.UDP_RECEIVER;
	
	/**
	 * 
	 */
	public UdpSocket () {
		this(StaticResources.SERVER_PORT_COAP);
	}
	
	/**
	 * 
	 * @param _port
	 */
	public UdpSocket (int _port) {
		this.port = _port;
	}
	
	@Override
	public void run() {
		
		try {
			
		serverSocket = new DatagramSocket(this.port);
		this.serverSocket.setReceiveBufferSize(StaticResources.UDP_PACKET_SIZE);
		
		Runnable sendWorker = new SendWorker(uuid);
		Thread t = new Thread(sendWorker);
		t.start();
		
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		while(true) {
        	
			if (!MessageService.isStarted()) continue;
			//Thread t = new Thread((Runnable)(new TestRoutine()));
			//t.start();
        	byte[] receivedData = new byte[StaticResources.UDP_PACKET_SIZE];
    		DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
        	
    		try {
        		
				serverSocket.receive(receivedPacket);
				System.out.println("Basestation packet received from " + receivedPacket.getAddress().getHostAddress());
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    		MessageService.getInstance().messageBuffer.add(new MessageObject(uuid,StaticResources.UDP_RECEIVER, receivedPacket));        
          
		}
	}
	private class TestRoutine implements Runnable {

		@Override
		public void run() {
			int j = 0;
			while (j<50) {
			double ds = Math.random()*10;
				int random = (int)ds;
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				String s = "{\"550e8400-e29b-41d4-a716-446655440111\":{\"550e"+ random+"400-e29b-41d4-a716-446655440000\":{\"attributes\":{\"name\":\"Power wall outlet\",\"location\":[60.32,45.42]},\"actuators\":[{\"uuid\":null,\"attributes\":{\"type\":\"power_switch\"},\"parameters\":{\"callback\":false},\"variables\": [{\"relay\":false, \"type\": \"boolean\" }]}],\"sensors\":[{\"uuid\":null,\"attributes\":{\"type\":\"Power sensor\"},\"parameters\":{\"options\":null},\"values\": [{\"value\": 13,\"time\":\"YY-MM-DD HH:MM\",\"unit\" : \"Celcius\"}]}]}}}";
				byte[] b = s.getBytes();
				DatagramPacket d = null;
				byte[] address = {22,22,22,22};
				try {
					d = new DatagramPacket(b, b.length,InetAddress.getByAddress(address), 23233);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				MessageService.getInstance().messageBuffer.add(new MessageObject(uuid,type,d));        
			}
			return;
		}
		
	}
	
	
	private class SendWorker implements Runnable,IMessageEvent {
		
		public List<MessageObject> sendBuffer = Collections.synchronizedList(new ArrayList<MessageObject>());
		public String uuid;
		
		/**
		 * 
		 * @param _uuid
		 */
		public SendWorker (String _uuid) {
			this.uuid = _uuid;
		}
		
		@Override
		public void run() {
			
			MessageService.getInstance().registerReceiver(this,this.uuid);
			
			while (true) {
				 
				if (!sendBuffer.isEmpty()) {
		        	   
		        	   try {
		        		
		        		   Iterator<MessageObject> i = this.sendBuffer.iterator();
				     		while(i.hasNext()) {
				     			
				     			MessageObject msg = i.next();
				     			
				     			if (msg.payload instanceof DatagramPacket) {
				     				serverSocket.send((DatagramPacket)msg.payload);
				     			} else if (msg.payload instanceof String) {
				     				
				     				byte[] b = ((String) msg.payload).getBytes();
				     				
				     				for (InetSocketAddress target : msg.targetAddresses) {
				     					DatagramPacket packet = new DatagramPacket(b,b.length,target.getAddress(),target.getPort());
				     					serverSocket.send(packet);
				     				}
				     			}
				     			sendBuffer.remove(msg);
				     		}

		   				} catch (IOException e) {
		   					// TODO Auto-generated catch block
		   					e.printStackTrace();
		   					break;
		   				} 
		           }
			}
			return; //Exit thread
		}


		@Override
		public void onMessageReceived(MessageObject _msg) {
			this.sendBuffer.add(_msg);
		}
		
		
	}

}
