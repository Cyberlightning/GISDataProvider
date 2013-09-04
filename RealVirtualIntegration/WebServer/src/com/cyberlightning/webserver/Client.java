package com.cyberligthtning.webserver

public class Client {
	
	private InetAddress address;
	private int port;
	private int protocol;

	
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
	
	
	
}