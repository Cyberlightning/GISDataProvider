package com.cyberlightning.android.coap;

import java.net.InetAddress;

public class CoapDevice {
	
	private InetAddress ipAdress;
	private String macAddress;
	private int port;
	
	public CoapDevice (InetAddress _adress, int _port) {
		this.setIpAdress(_adress);
		this.setPort(_port);
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public InetAddress getIpAdress() {
		return ipAdress;
	}

	public void setIpAdress(InetAddress ipAdress) {
		this.ipAdress = ipAdress;
	}
	
	
}
