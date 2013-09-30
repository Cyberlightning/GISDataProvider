package com.cyberlightning.webserver.entities;

import java.net.InetAddress;

public class Client {
	
	private InetAddress ip4v;
	private int port;
	private int protocol;
	private int type = 0;
	private long activityTimeStamp;
	public final static int TYPE_BASESTATION = 1;
	public final static int TYPE_BROWSER = 2;

	
	public Client (){
	}
	
	public Client(InetAddress _address, int _port, int _protocol) {
		this.ip4v = _address;
		this.port = _port;
		this.protocol = _protocol;
	}
	
	public InetAddress getAddress() {
		return this.ip4v;
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
	
	public int getType() {
		return this.type;
	}
	
	public void setActivityTimeStamp (long _timeStamp) {
		this.activityTimeStamp = _timeStamp;
	}
	
	public void setType(int _type) {
		this.type = _type;
	}
	
	
	
	
	
}