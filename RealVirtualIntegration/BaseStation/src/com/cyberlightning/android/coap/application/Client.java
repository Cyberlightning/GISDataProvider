package com.cyberlightning.android.coap.application;

public class Client {
	private String address;
	
	public Client (String _address) {
		this.setAddress(_address);
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
}
