package com.cyberlightning.webserver.services;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.Iterator;

import com.cyberlightning.webserver.interfaces.IMessageEvent;
import com.cyberlightning.webserver.entities.Client;


public class ProfileService   { 

	private static final ProfileService _profileService = new ProfileService();
	private ArrayList<Client> connectedClients = new ArrayList<Client>();

	private ProfileService() {
		
	}
	
	public static ProfileService getInstance() {
		return _profileService;
	}
	
	public boolean registerClient(Client _client) { //returns true if client already registered
		
		boolean containsClient = false;
		
		for (int i = 0; i < this.connectedClients.size(); i++) {
//			if (this.connectedClients.get(i).getAddress().compareTo(_client.getAddress()) == 0) {
//				containsClient = true;
//				this.connectedClients.get(i).setActivityTimeStamp(System.currentTimeMillis());
//			}
		}
		if (!containsClient) {
			_client.setActivityTimeStamp(System.currentTimeMillis());
			this.connectedClients.add(_client);
		}
		
		return containsClient;
	}
	
	
	
}
