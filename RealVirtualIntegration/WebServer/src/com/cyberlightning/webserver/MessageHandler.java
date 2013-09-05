package com.cyberlightning.webserver;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;


public class MessageHandler {
	
	private static final MessageHandler _messageHandler = new MessageHandler();
	private List<MessageEvent> registeredReceivers= new ArrayList<MessageEvent>();

	private MessageHandler() {
		
	}
	
	public static MessageHandler getInstance () {
		return _messageHandler;
	}
	
	public void registerReceiver(MessageEvent receiver) {
		this.registeredReceivers.add(receiver);
	}
	
	public void broadcastHttpMessageEvent(String msg) {
		
		for (MessageEvent client : this.registeredReceivers) {
			client.httpMessageEvent(msg);
		}
	}
	
	public void broadcastUdpMessageEvent(DatagramPacket _datagramPacket) {
		
		for (MessageEvent client : this.registeredReceivers) {
			client.udpMessageEvent(_datagramPacket);
		}
	}
}
