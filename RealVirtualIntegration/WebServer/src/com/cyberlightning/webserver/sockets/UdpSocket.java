package com.cyberlightning.webserver.sockets;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.cyberlightning.webserver.StaticResources;
import com.cyberlightning.webserver.interfaces.IMessageEvent;
import com.cyberlightning.webserver.services.MessageService;

public class UdpSocket implements Runnable  {
	
	private DatagramSocket serverSocket;
	private Map<String,DatagramPacket> deviceLookUpTable = new ConcurrentHashMap<String,DatagramPacket>();
	private int port;
	
	protected final String uuid = UUID.randomUUID().toString();
	public final int type = StaticResources.UDP_RECEIVER;
	
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
		
		Runnable sendWorker = new SendWorker(this.uuid);
		Thread t = new Thread(sendWorker);
		t.start();
		
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         
		while(true) {
        	
        	byte[] receivedData = new byte[StaticResources.UDP_PACKET_SIZE];
    		DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
        	testMethod();
    		try {
        		
				serverSocket.receive(receivedPacket);
				System.out.println("Basestation packet received from " + receivedPacket.getAddress().getHostAddress());
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		this.deviceLookUpTable.put(receivedPacket.getAddress().getHostAddress(), receivedPacket);
    		MessageService.getInstance().messageBuffer.add(new MessageObject(this.uuid,StaticResources.UDP_RECEIVER, receivedPacket));        
          
		}
	}
	private void testMethod() {
		
			String s = "{\"550e8400-e29b-41d4-a716-446655440111\":{\"550e8400-e29b-41d4-a716-446655440000\":{\"attributes\":{\"name\":\"Power wall outlet\",\"address\":null},\"actuators\":[{\"uuid\":null,\"attributes\":{\"type\":\"power_switch\"},\"parameters\":{\"callback\":false},\"variables\": [{\"relay\":false, \"type\": \"boolean\" }]}],\"sensors\":[{\"uuid\":null,\"attributes\":{\"type\":\"Power sensor\"},\"parameters\":{\"options\":null},\"values\": [{\"value\": 13,\"time\":\"YY-MM-DD HH:MM\",\"unit\" : \"Celcius\"}]}]}}}";
			byte[] b = s.getBytes();
			DatagramPacket d = null;
			try {
				d = new DatagramPacket(b, b.length,InetAddress.getByName("dev.cyberlightning.com"), 23233);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//MessageService.getInstance().messageBuffer.put(new MessageHeader(uuid,  d.getAddress()), d);        
	          
		
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
				     				for (String target : msg.targetAddress) {
				     					DatagramPacket packet;
				     					if (deviceLookUpTable.containsKey(target)) {
				     						packet = new DatagramPacket(b,b.length,deviceLookUpTable.get(target).getAddress(),deviceLookUpTable.get(target).getPort());
				     					} else {
				     						InetAddress inetAddress = InetAddress.getByName(target);
				     						packet = new DatagramPacket(b,b.length,inetAddress,StaticResources.DEFAULT_BASESTATION_PORT);
				     					}
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
			
		}


		@Override
		public void onMessageReceived(MessageObject _msg) {
			this.sendBuffer.add(_msg);
			
		}
		
		
	}
	
	
   
    
    
    
	
	
//	private byte[] formatJSON(String _msg) {
//    	
//    	JSONObject device = new JSONObject();
//		device.put("DeviceID", "*");
//		JSONObject root = new JSONObject();
//		//root.put("notificationURI", _address);
//		root.put("request", _msg);
//		root.put("contextEntities", device);
//	
//		byte[] b = new byte[1024];
//		try {
//			b = root.toJSONString().getBytes("UTF-8");
//		} catch (UnsupportedEncodingException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//			System.out.println("UnsupportedEncodingException: " + e1.getMessage());
//		}
//		
//		return b;
//    }
    
//    private void sendToClients(String _msg, String _address) {
//    	
//    	Iterator<Client> i = this.baseStations.iterator();
//    	byte[] b = this.formatJSON(_msg, _address);
//		
//
//    	while (i.hasNext()) {
//    		
//    		Client client = i.next();
//    		DatagramPacket packet = new DatagramPacket(b, b.length, client.getAddress() , client.getPort());
//    		
//    		try {
//    			this.serverSocket.send(packet);
//    			System.out.println("Packet send to basestation :" + client.getAddress().getHostAddress());
//    		} catch (IOException e) {
//    			// TODO Auto-generated catch block
//    			e.printStackTrace();
//    			System.out.println("IOException: " + e.getMessage());
//    		}
//    	}
//    }
//	 private boolean handleConnectedClient(DatagramPacket _datagramPacket ) {
//	    	
//	    	Iterator<Client> i = this.baseStations.iterator();
//	    	boolean isRegistered = false;
//	    	while (i.hasNext()) {
//	    		if (i.next().getAddress().getHostAddress().contentEquals(_datagramPacket.getAddress().getHostAddress())) {
//	    			isRegistered = true;
//	    		}
//	    	}
//	    	if (!isRegistered) {
//	    		this.baseStations.add(new Client(_datagramPacket.getAddress(), _datagramPacket.getPort(),StaticResources.CLIENT_PROTOCOL_COAP, Client.TYPE_BASESTATION));
//	    	}
//	    	return isRegistered;
//	    }
	
	
}
