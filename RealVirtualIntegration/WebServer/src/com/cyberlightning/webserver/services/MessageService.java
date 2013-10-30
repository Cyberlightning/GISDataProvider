package com.cyberlightning.webserver.services;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.cyberlightning.webserver.interfaces.IMessageEvent;


public class MessageService implements Runnable  {
	
	private static final MessageService _messageHandler = new MessageService();
	private HashMap<String,IMessageEvent> registeredReceivers= new HashMap<String,IMessageEvent>();
	public  Map<String, Object> messageBuffer = new ConcurrentHashMap<String, Object>();
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
			Iterator<String> i = this.messageBuffer.keySet().iterator();
			
			while (i.hasNext()) {
				String key = i.next();
				ArrayList<String> receivers = this.resolveReceivers(key);
				for (String receiver: receivers) {
					this.registeredReceivers.get(receiver).onMessageReceived(0, this.messageBuffer.get(key));
				}
			}
			
			
		}
		
	}
	
	

	
}
