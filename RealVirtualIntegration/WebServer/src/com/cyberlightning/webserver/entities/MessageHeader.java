package com.cyberlightning.webserver.entities;

public class MessageHeader {
	
	public String senderUuid;
	public int origin;
	public int target;
	private String senderAddress;
	private int senderPort;

	
	public MessageHeader (String _senderUuid, int _origin ) {
		this.senderUuid = _senderUuid;
		this.origin = _origin;
		
	}
	
	public MessageHeader (String _senderUuid, int _origin, int _target) {
		this.target = _target;
		this.senderUuid = _senderUuid;
		this.origin = _origin;
	}

	public String getSenderAddress() {
		return senderAddress;
	}

	public void setSenderAddress(String senderAddress) {
		this.senderAddress = senderAddress;
	}

	public int getSenderPort() {
		return senderPort;
	}

	public void setSenderPort(int senderPort) {
		this.senderPort = senderPort;
	}
	
	
	
}
