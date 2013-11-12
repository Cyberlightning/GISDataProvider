package com.cyberlightning.realvirtualsensorsimulator;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Observable;


import android.os.Message;

public class ClientSocket extends Observable implements Runnable {
	
	private DatagramSocket localCoapSocket;
	
	private int port;
	
	public static final int DEFAULT_BUFFER_SIZE = 1024;
	public static final int DEFAULT_INBOUND_SOCKET = 55555;
	public static final int MESSAGE_TYPE_INBOUND = 1;
	public static final int MESSAGE_TYPE_OUTBOUND = 2;
	
	public ClientSocket() {
		this(DEFAULT_INBOUND_SOCKET);
	}
	
	public ClientSocket(int _port) {
		this.port = _port;
	}
	@Override
	public void run() {
		
		try {
			
			this.localCoapSocket = new DatagramSocket(this.port);
			this.localCoapSocket.setReceiveBufferSize(DEFAULT_BUFFER_SIZE);
			
			byte[] receiveByte = new byte[DEFAULT_BUFFER_SIZE]; 
			DatagramPacket receivedPacket = new DatagramPacket(receiveByte, receiveByte.length);
			
			while(true) {
				localCoapSocket.receive(receivedPacket);
				handleInboundMessage(receivedPacket);
			}

			
		} catch(IOException e) {
			e.printStackTrace();
		} 	
		return; //Exits thread

	}

    private void handleInboundMessage(DatagramPacket _packet) {
    	
    	String payload = null;
		try {
			payload = new String (_packet.getData(), "utf8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    	MessageObject msg = new MessageObject(_packet.getAddress(), _packet.getPort(), payload);
		setChanged();
		notifyObservers(Message.obtain(null, MESSAGE_TYPE_INBOUND, msg));
	}
    
	public class SocketSender implements Runnable {
		
		@Override
		public void run() {
			
			while(true) {
				
			}
			
		}
		
	}
	
	

}
