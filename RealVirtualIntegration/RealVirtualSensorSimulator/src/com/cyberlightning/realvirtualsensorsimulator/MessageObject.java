package com.cyberlightning.realvirtualsensorsimulator;

import java.net.InetAddress;

public class MessageObject {
	
	private InetAddress address;
	private int port;
	private String payload;
	
	public MessageObject(InetAddress _originAddress, int _originPort, String _msgPayload ) {
		this.address = _originAddress;
		this.port = _originPort;
		this.payload = _msgPayload;
	}
	
	public String getPayload() {
		return this.payload;
	}

	public int getPort() {
		return this.port;
	}

	public InetAddress getAddress() {
		return this.address;
	}

}
