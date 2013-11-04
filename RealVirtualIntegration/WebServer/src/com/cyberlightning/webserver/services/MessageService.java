package com.cyberlightning.webserver.services;


import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.cyberlightning.webserver.StaticResources;
import com.cyberlightning.webserver.interfaces.IMessageEvent;
import com.cyberlightning.webserver.sockets.MessageObject;
import com.cyberlightning.webserver.sockets.UdpSocket;

/**
 * 
 * @author Tomi
 *
 */
public class MessageService implements Runnable  {
	
	private static final MessageService _messageHandler = new MessageService();
	private HashMap<String,IMessageEvent> registeredReceivers= new HashMap<String,IMessageEvent>();
	public List<MessageObject> messageBuffer = Collections.synchronizedList(new ArrayList<MessageObject>());

	private Map<String,ArrayList<String>> messageLinks = new ConcurrentHashMap<String,ArrayList<String>>();

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
				receivers.addAll(this.messageLinks.get(receiver));
			} else if (this.messageLinks.get(receiver).contains(_senderUuid)) {
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
	
	public void subscribeByIds(ArrayList<String> _publisherUuids, String _subscriberUuid) {
		for (String uuid : _publisherUuids) {
			ArrayList<String> subscriberUuids = new ArrayList<String>();
			if(this.messageLinks.containsKey(uuid)) {
				subscriberUuids = this.messageLinks.get(uuid);
				subscriberUuids.add(_subscriberUuid);
			} else {
				subscriberUuids.add(_subscriberUuid);
			}
			this.messageLinks.put(uuid,subscriberUuids);
		}
	}
	
	public void unsubscribeAllById(String _subscriberUuid) {
		Iterator<ArrayList<String>> i = this.messageLinks.values().iterator();
		while(i.hasNext()) {
			ArrayList<String> j = i.next();
			if (j.contains(_subscriberUuid)){
				int k = j.indexOf(_subscriberUuid);
				j.remove(k);
			}
		}
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
						this.registeredReceivers.get(UdpSocket.uuid).onMessageReceived(msg); //TODO implement a better way for this, what if there are multiple sockets? 
						break;
					case StaticResources.HTTP_CLIENT:
						this.registeredReceivers.get(UdpSocket.uuid).onMessageReceived(msg); //TODO implement a better way for this, what if there are multiple sockets? 
						break;
						
					}
				}
		}	
	}

}
