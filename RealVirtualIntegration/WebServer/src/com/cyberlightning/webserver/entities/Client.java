package com.cyberlightning.webserver.entities;

public class Client {
	
	private String ip4v;
	private int port;
	private int protocol;
	private long activityTimeStamp;

	
	public Client (){
	}
	
	public Client(String _address, int _port, int _protocol) {
		this.ip4v = _address;
		this.port = _port;
		this.protocol = _protocol;
	}
	
	public String getAddress() {
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
	
	public void setActivityTimeStamp (long _timeStamp) {
		this.activityTimeStamp = _timeStamp;
	}
	
	
	
	
	
}