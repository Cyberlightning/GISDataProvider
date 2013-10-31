package com.cyberlightning.webserver.services;


import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.cyberlightning.webserver.StaticResources;
import com.cyberlightning.webserver.entities.MessageHeader;
import com.cyberlightning.webserver.interfaces.IMessageEvent;


public class MessageService implements Runnable  {
	
	private static final MessageService _messageHandler = new MessageService();
	private HashMap<String,IMessageEvent> registeredReceivers= new HashMap<String,IMessageEvent>();
	public  Map<MessageHeader, Object> messageBuffer = new ConcurrentHashMap<MessageHeader, Object>();
	private HashMap<String,String> messageLinks = new HashMap<String,String>();

	private MessageService() {
		
	}
	
	private ArrayList<String> resolveReceivers(String _senderUuid) {
		
		ArrayList<String> receivers = new ArrayList<String>();
		Iterator<String> i = this.messageLinks.keySet().iterator();
		while (i.hasNext()) {
			String receiver = i.next();
			if (receiver.contentEquals(_senderUuid)) {
				receivers.add(this.messageLinks.get(receiver));
			} else if (this.messageLinks.get(receiver).contentEquals(_senderUuid)) {
				receivers.add(receiver);
			}
		}
		return receivers;
	}
	
	public static MessageService getInstance () {
		return _messageHandler;
	}
	
	public void registerReceiver(IMessageEvent receiver, String _uuid) {
	
		this.registeredReceivers.put(_uuid, receiver);
	}
	
	public void unregisterReceiver(String _uuid) {
		
		this.registeredReceivers.remove(_uuid);
	}
	
//	public void onMessageReceived(int _type, Object _msg) {
//		
//		for (IMessageEvent receiver : this.registeredReceivers) {
//			receiver.onMessageReceived(_type, _msg);
//		}
//	}
//	
	

	@Override
	public void run() {
		while(true) {
			if (this.messageBuffer.isEmpty()) continue;
			Iterator<MessageHeader> i = this.messageBuffer.keySet().iterator();
			
			while (i.hasNext()) {
				MessageHeader key = i.next();
				
				switch (key.origin) {
				case StaticResources.UDP_RECEIVER: DataStorageService.getInstance().eventBuffer.put(key.senderUuid, (DatagramPacket)this.messageBuffer.get(key));
					break;
				case StaticResources.HTTP_CLIENT:
					break;
				default:
					break;
				}

				ArrayList<String> receivers = this.resolveReceivers(key.senderUuid);
				for (String receiver: receivers) {
					this.registeredReceivers.get(receiver).onMessageReceived(0, this.messageBuffer.get(key));
				}
			}
			
			
		}
		
	}
	
	

	
}
