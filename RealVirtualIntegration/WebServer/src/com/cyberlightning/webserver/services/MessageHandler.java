package com.cyberlightning.webserver.services;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;

import com.cyberlightning.webserver.interfaces.IMessageEvent;


public class MessageHandler {
	
	private static final MessageHandler _messageHandler = new MessageHandler();
	private List<IMessageEvent> registeredReceivers= new ArrayList<IMessageEvent>();

	private MessageHandler() {
		
	}
	
	public static MessageHandler getInstance () {
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
			client.udpMessageEvent(_datagramPacket);
		}
	}
}
