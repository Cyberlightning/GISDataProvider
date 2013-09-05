package com.cyberlightning.webserver.entities;

import java.net.InetAddress;

public class Client {
	
	private InetAddress address;
	private int port;
	private int protocol;
	private long activityTimeStamp;

	
	public Client (){
	}
	
	public Client(InetAddress _address, int _port, int _protocol) {
		this.address = _address;
		this.port = _port;
		this.protocol = _protocol;
	}
	
	public InetAddress getAddress() {
		return this.address;
	}
	
	public int getPort() {
		return this.port;
	}
	
	public int getProtocol() {
		return this.protocol;
	}
	
	public long getActivityTimeStamp () {
		return this.activityTimeStamp;
	}
	
	public void setActivityTimeStamp (long _timeStamp) {
		this.activityTimeStamp = _timeStamp;
	}
	
	
	
	
	
}