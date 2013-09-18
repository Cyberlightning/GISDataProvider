package com.cyberlightning.android.coap;

import java.net.InetAddress;

public class NetworkDevice {
	
	private InetAddress address;
	private int port;
	private String name;
	private String type;
	private boolean isBaseStation;
	
	public NetworkDevice (InetAddress _address, int _port, String _name, String _type) {
		this.address = _address;
		this.port = _port;
		this.name = _name;
		this.type = _type;
	}
	
	public InetAddress getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public boolean isBaseStation() {
		return isBaseStation;
	}

	public void setBaseStation(boolean isBaseStation) {
		this.isBaseStation = isBaseStation;
	}
}
