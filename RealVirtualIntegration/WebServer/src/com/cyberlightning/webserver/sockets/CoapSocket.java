package com.cyberlightning.webserver.sockets;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.simple.JSONObject;

import com.cyberlightning.webserver.StaticResources;
import com.cyberlightning.webserver.entities.Client;
import com.cyberlightning.webserver.interfaces.IMessageEvent;
import com.cyberlightning.webserver.services.MessageService;

public class CoapSocket implements Runnable,IMessageEvent  {
	
	private DatagramSocket serverSocket;
	private ArrayList<DatagramPacket> sendBuffer= new ArrayList<DatagramPacket>();
	private ArrayList<Client> baseStations = new ArrayList<Client>();
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
		this.serverSocket.setReceiveBufferSize(StaticResources.UDP_PACKET_SIZE);
		MessageService.getInstance().registerReceiver(this);	
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         
		while(true) {
        	
        	byte[] receivedData = new byte[StaticResources.UDP_PACKET_SIZE];
    		DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
        	
    		try {
        		
				serverSocket.receive(receivedPacket);
				System.out.println("Basestation packet received from " + receivedPacket.getAddress().getHostAddress());
				
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
	
    private boolean handleConnectedClient(DatagramPacket _datagramPacket ) {
    	
    	Iterator<Client> i = this.baseStations.iterator();
    	boolean isRegistered = false;
    	while (i.hasNext()) {
    		if (i.next().getAddress().getHostAddress().contentEquals(_datagramPacket.getAddress().getHostAddress())) {
    			isRegistered = true;
    		}
    	}
    	if (!isRegistered) {
    		this.baseStations.add(new Client(_datagramPacket.getAddress(), _datagramPacket.getPort(),StaticResources.CLIENT_PROTOCOL_COAP, Client.TYPE_BASESTATION));
    	}
    	return isRegistered;
    }
    
    private byte[] formatJSON(String _msg, String _address) {
    	
    	JSONObject device = new JSONObject();
		device.put("DeviceID", "*");
		JSONObject root = new JSONObject();
		root.put("notificationURI", _address);
		root.put("request", _msg);
		root.put("contextEntities", device);
	
		byte[] b = new byte[1024];
		try {
			b = root.toJSONString().getBytes("UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.out.println("UnsupportedEncodingException: " + e1.getMessage());
		}
		
		return b;
    }
    
    private void sendToClients(String _msg, String _address) {
    	
    	Iterator<Client> i = this.baseStations.iterator();
    	byte[] b = this.formatJSON(_msg, _address);
		

    	while (i.hasNext()) {
    		
    		Client client = i.next();
    		DatagramPacket packet = new DatagramPacket(b, b.length, client.getAddress() , client.getPort());
    		
    		try {
    			this.serverSocket.send(packet);
    			System.out.println("Packet send to basestation :" + client.getAddress().getHostAddress());
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    			System.out.println("IOException: " + e.getMessage());
    		}
    	}
    }
    
	@Override
	public void httpMessageEvent(String _address, String _msg) {
		this.sendToClients(_msg, _address);
		
	}

	@Override
	public void coapMessageEvent(DatagramPacket _datagramPacket) {
//		InputStreamReader input = new InputStreamReader(new ByteArrayInputStream(_datagramPacket.getData()), Charset.forName("UTF-8"));
//		try {
//			StringBuilder str = new StringBuilder();
//			for (int value; (value = input.read()) != -1; )
//			    str.append((char) value);
//			String s = str.toString();
//			String d = str.toString();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} 
		
	}

	@Override
	public void webSocketMessageEvent(String _msg, String _address) {
		this.sendToClients(_msg, _address);
		System.out.println("webSocketMessageEvent: " + _address);
	}
	

	
	
}
