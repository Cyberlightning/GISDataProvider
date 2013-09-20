package com.cyberlightning.android.coap;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Observable;

import com.cyberlightning.android.coap.memory.RomMemory;

import android.os.Message;

public class CoapSocket extends Observable  implements Runnable,ICoapSocket {
	
	private DatagramSocket localCoapSocket;
	private int port;
	
	public CoapSocket() {
		this(RomMemory.DEFAULT_PORT);
	}
	public CoapSocket(int _port) {
		this.port = _port;
	}
	@Override
	public void run() {
		
		try {
			
			localCoapSocket = new DatagramSocket(this.port);
			localCoapSocket.setReceiveBufferSize(RomMemory.DEFAULT_BUFFER_SIZE);
			byte[] receiveByte = new byte[RomMemory.DEFAULT_BUFFER_SIZE]; 
			DatagramPacket receivedPacket = new DatagramPacket(receiveByte, receiveByte.length);
	
			
			while(true) {
				
				localCoapSocket.receive(receivedPacket);
				if (receivedPacket.getSocketAddress() != null) {
					notifyObservers(receivedPacket);
					receivedPacket = null; //clear packet holder
				}
			}
			//TODO handle socket closed
			
		} catch(IOException e) {
			e.printStackTrace();
		} 
		
		return; 
		
		
	}
	

	@Override
	public void broadCastMessage(Message _msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendMessage(Message _msg) {
		// TODO Auto-generated method stub
		
	}

}
