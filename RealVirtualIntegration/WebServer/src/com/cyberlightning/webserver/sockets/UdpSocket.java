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
			Thread t = new Thread((Runnable)(new TestRoutine()));
			t.start();
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
			double ds2 = Math.random()*10;
			double ds3 = Math.random()*10;
			String uid = UUID.randomUUID().toString();
				int random = (int)ds;
				int random2 = (int)ds2;
				int random3 = (int)ds3;
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				String s = "{\"5de5f289-c7f3-4994-8dfb-3639d3f7c8d0\": {\"BC:6A:29:AB:E8:B5\": {\"attributes\": { \"name\": \"Texas CL2541 Sensor\",\"gps\": [65.01,28.25]},\"sensors\": [{\"value\": {\"unit\": \"m/s2\",\"primitive\": \"double\", \"time\": \"2013-11-07 15:41\",\"values\": [ 0,-0.015625,1]},\"uuid\": \"f000aa10-0451-4000-b000-000000000000\",\"parameters\": {\"toggleable\": \"true\",\"options\": \"boolean\"},\"attributes\": {\"type\": \"accelerometer\",\"vendor\": \"Texas Instruments\"}},{\"value\": { \"unit\": \"Celsius\",\"primitive\": \"double\",\"time\": \"2013-11-07 15:41\",\"values\": 27.21875},\"uuid\": \"f000aa00-0451-4000-b000-000000000000\",\"parameters\": {\"toggleable\": \"true\",\"options\": \"boolean\"},\"attributes\": {\"type\": \"temperature\",\"vendor\": \"Texas Instruments\"}}]}}}";
				
				
				
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
