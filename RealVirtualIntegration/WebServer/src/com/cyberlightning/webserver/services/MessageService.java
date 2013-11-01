package com.cyberlightning.webserver.services;


import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.cyberlightning.webserver.StaticResources;
import com.cyberlightning.webserver.entities.MessageHeader;
import com.cyberlightning.webserver.interfaces.IMessageEvent;

/**
 * 
 * @author Tomi
 *
 */
public class MessageService implements Runnable  {
	
	private static final MessageService _messageHandler = new MessageService();
	private HashMap<String,IMessageEvent> registeredReceivers= new HashMap<String,IMessageEvent>();
	public  Map<String, Object> messageBuffer = new ConcurrentHashMap<String, Object>();
	private Map<String,String> messageLinks = new ConcurrentHashMap<String,String>();

	/**
	 * 
	 */
	private MessageService() {
		
	}
	
	/**
	 * 
	 * @param _senderUuid
	 * @return
	 */
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
	

	@Override
	public void run() {
		
		while(true) {
			
			if (this.messageBuffer.isEmpty()) continue;
			Iterator<String> keys = this.messageLinks.keySet().iterator();
			while (keys.hasNext())  {
				String key = keys.next();
				ArrayList<String> receivers = this.resolveReceivers(key);
				for (String receiver: receivers) {
					this.registeredReceivers.get(receiver).onMessageReceived(0, this.messageBuffer.get(key));
				} 
			}
		}	
	}

}
