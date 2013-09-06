package com.cyberlightning.webserver.services;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;

import com.cyberlightning.webserver.interfaces.IMessageEvent;


public class MessageService {
	
	private static final MessageService _messageHandler = new MessageService();
	private List<IMessageEvent> registeredReceivers= new ArrayList<IMessageEvent>();

	private MessageService() {
		
	}
	
	public static MessageService getInstance () {
		return _messageHandler;
	}
	
	public void registerReceiver(IMessageEvent receiver) {
		this.registeredReceivers.add(receiver);
	}
	
	public void broadcastHttpMessageEvent(String msg) {
		
		for (IMessageEvent client : this.registeredReceivers) {
			client.httpMessageEvent(msg);
		}
	}
	
	public void broadcastUdpMessageEvent(DatagramPacket _datagramPacket) {
		
		for (IMessageEvent client : this.registeredReceivers) {
			client.coapMessageEvent(_datagramPacket);
		}
	}
	
	public void broadcastWebSocketMessageEvent(String msg) {
		
		for (IMessageEvent client : this.registeredReceivers) {
			client.webSocketMessageEvent(msg);
		}
	}
}
