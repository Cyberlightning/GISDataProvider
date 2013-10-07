package com.cyberlightning.android.coap;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Observable;
import java.util.Random;

import com.cyberlightning.android.coap.interfaces.*;
import com.cyberlightning.android.coap.message.*;
import com.cyberlightning.android.coap.memory.RomMemory;

import android.os.Message;

public class CoapSocket extends Observable  implements Runnable,ICoapSocket {
	
	private DatagramSocket localCoapSocket;
	private int port;
	public boolean isConnectected = false;
	
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
			this.isConnectected = true;
			
			while(true) {
				localCoapSocket.receive(receivedPacket);
				handleInboundMessage(receivedPacket);
			}
			//TODO handle socket closed
			
		} catch(IOException e) {
			e.printStackTrace();
		} 
		this.isConnectected = false;
		return; 
		
		
	}
	
	/** */
    private void handleInboundMessage(DatagramPacket _packet) {
    	
    	ByteBuffer buffer = ByteBuffer.wrap(_packet.getData());
    	
		CoapMessage msg = null;
		String payload = null;

		try {
			msg = AbstractCoapMessage.parseMessage(buffer.array(), buffer.array().length);
			payload = new String(msg.getPayload(), "utf8");
		} catch (Exception e) {
			e.printStackTrace();
		} 
		//MessageEvent messageEvent = new MessageEvent(payload, _packet.getAddress().getHostAddress(), false, "");
		
		setChanged();
		notifyObservers(Message.obtain(null, 2, payload));
		
		
		//TODO input logic to handle ACK,NON,RST, .. 

	}
	
	@Override
	public void broadCastMessage(Message _msg, HashMap<String,NetworkDevice> _devices) {
		CoapRequest coapRequest = this.createRequest(true, CoapRequestCode.POST); //clientChannel.createRequest(true, CoapRequestCode.POST);
	    coapRequest.setContentType(CoapMediaType.json);
		//coapRequest.setUriPath("/devices");
	    
		coapRequest.setPayload(_msg.obj.toString());
		CoapMessage coapMessage = (CoapMessage)coapRequest;
		//coapRequest.setUriQuery(jsonSensorsList.toString());
		ByteBuffer buf = ByteBuffer.wrap(coapMessage.serialize());

		Iterator<String> i = _devices.keySet().iterator();
		while(i.hasNext()) {
			NetworkDevice nd = _devices.get(i.next());
			DatagramPacket packet = new DatagramPacket(buf.array(), buf.array().length, nd.getAddress(), nd.getPort());
			try {
				
				this.localCoapSocket.send(packet);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void sendMessage(Message _msg) {
		//TODO auto-generated
	}
	
	private BasicCoapRequest createRequest(boolean reliable, CoapRequestCode requestCode) {
	    BasicCoapRequest msg = new BasicCoapRequest(reliable ? CoapPacketType.CON : CoapPacketType.NON, requestCode,this.getNewMessageID());
	    return msg;
	}
	  
	/** Creates a new, global message id for a new COAP message */ 
	private synchronized int getNewMessageID() {
	    Random random = new Random();
	    return random.nextInt(RomMemory.MAX_MESSAGE_ID + 1);
	}

}
