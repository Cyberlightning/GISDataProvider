package com.cyberlightning.webserver.services;

import java.net.DatagramPacket;
import java.util.ArrayList;

import com.cyberlightning.webserver.interfaces.IMessageEvent;
import com.cyberlightning.webserver.entities.Client;


public class ProfileService implements IMessageEvent { 

	private static final ProfileService _profileService = new ProfileService();
	private ArrayList<Client> connectedClients;

	private ProfileService() {
		MessageService.getInstance().registerReceiver(this);
	}
	
	public static ProfileService getInstance() {
		return _profileService;
	}
	
	public boolean registerClient(Client _client) { //returns true if client already registered
		
		boolean containsClient = false;
		
		for (int i = 0; i < this.connectedClients.size(); i++) {
			if (this.connectedClients.get(i).getAddress().compareTo(_client.getAddress()) == 0) {
				containsClient = true;
				this.connectedClients.get(i).setActivityTimeStamp(System.currentTimeMillis());
			}
		}
		if (!containsClient) {
			_client.setActivityTimeStamp(System.currentTimeMillis());
			this.connectedClients.add(_client);
		}
		
		return containsClient;
	}

	@Override
	public void httpMessageEvent(String msg) {
		// TODO Auto-generated method stub	
	}

	@Override
	public void coapMessageEvent(DatagramPacket _datagramPacket) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void webSocketMessageEvent(String msg) {
		// TODO Auto-generated method stub
		
	}
	
	
	
	
}
