package com.cyberlightning.webserver.services;


import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.cyberlightning.webserver.StaticResources;
import com.cyberlightning.webserver.interfaces.IMessageEvent;
import com.cyberlightning.webserver.sockets.MessageObject;

/**
 * 
 * @author Tomi
 *
 */
public class MessageService implements Runnable  {
	
	private static final MessageService _messageHandler = new MessageService();
	private HashMap<String,IMessageEvent> registeredReceivers= new HashMap<String,IMessageEvent>();
	public List<MessageObject> messageBuffer = Collections.synchronizedList(new ArrayList<MessageObject>());

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
	
	public void registerPublisher(String _address) {
		
	}
	
	public void unRegisterPublisher(String _address) {
		
	}
	
	public void registerSubscriber(String _uuid){
		
	}
	
	public void unregisterSubscriber(String _uuid) {
		
	}

	@Override
	public void run() {
		
		while(true) {
			
			if (this.messageBuffer.isEmpty()) continue;
			
				Iterator<MessageObject> o = this.messageBuffer.iterator();
				while (o.hasNext()) {
					MessageObject msg = o.next();
				
					switch (msg.originType) {
					case StaticResources.UDP_RECEIVER:
						DataStorageService.getInstance().eventBuffer.put(msg.originUUID,(DatagramPacket) msg.payload);
						ArrayList<String> receivers = this.resolveReceivers(msg.originUUID);
						for (String receiver: receivers) {
							this.registeredReceivers.get(receiver).onMessageReceived(msg);
						} 
						break;
					case StaticResources.TCP_CLIENT:
						break;
					case StaticResources.HTTP_CLIENT:
					//TODO
						break;
						
					}
					
				
				}

				
			
			
			
			
		}	
	}

}
