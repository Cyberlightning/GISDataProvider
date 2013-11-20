package com.cyberlightning.webserver.services;


import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.cyberlightning.webserver.StaticResources;
import com.cyberlightning.webserver.interfaces.IMessageEvent;
import com.cyberlightning.webserver.sockets.MessageObject;
import com.cyberlightning.webserver.sockets.UdpSocket;

/**
 * 
 * @author CyberLightning <tomi.sarni@cyberlightning.com>
 *
 */
public class MessageService implements Runnable  {
	
	private static final MessageService _messageHandler = new MessageService();
	private HashMap<String,IMessageEvent> registeredReceivers= new HashMap<String,IMessageEvent>();
	private Map<String,ArrayList<String>> messageLinks = new ConcurrentHashMap<String,ArrayList<String>>();
	public CopyOnWriteArrayList<MessageObject> messageBuffer = new CopyOnWriteArrayList<MessageObject>();
	private volatile static boolean isStarted = false;
	private boolean suspendFlag = true;
	private Thread thread ;
	
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
	public void startThread() {
		this.thread = new Thread(this);
		this.thread.start();
		isStarted = true;
	}
	
	public void addToMessageBuffer(MessageObject _msg) {
		this.messageBuffer.add(_msg);

	}
	
	public static Boolean isStarted() {
		return isStarted;
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
	
	public void suspendThread() {
	      suspendFlag = true;
	}

	public synchronized void wakeThread() {
	      suspendFlag = false;
	       notify();
	}

	
	@Override
	public void run() {
		
		while(true) {
			synchronized(this) {
	            
				while(suspendFlag) {
					
					
					try {
						wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
	            }
	        }
			
			if (this.messageBuffer.isEmpty()) continue;
			
				Iterator<MessageObject> o = this.messageBuffer.iterator();
				while (o.hasNext()) {
					MessageObject msg = o.next();
				
					switch (msg.originType) {
					case StaticResources.UDP_RECEIVER:
						
						DataStorageService.getInstance().addToBuffer(msg.originUUID, (DatagramPacket) msg.payload);
						ArrayList<String> receivers = this.resolveReceivers(msg.originUUID);
						for (String receiver: receivers) {
							this.registeredReceivers.get(receiver).onMessageReceived(msg);
						}
						this.messageBuffer.remove(msg);
						break;
					case StaticResources.UDP_RESPONSE: 
						this.registeredReceivers.get(msg.originUUID).onMessageReceived(msg);
						break;	
					case StaticResources.TCP_CLIENT:
						this.registeredReceivers.get(UdpSocket.uuid).onMessageReceived(msg); //TODO implement a better way for this, what if there are multiple sockets? 
						this.messageBuffer.remove(msg);
						break;
					case StaticResources.HTTP_CLIENT:
						this.registeredReceivers.get(UdpSocket.uuid).onMessageReceived(msg); //TODO implement a better way for this, what if there are multiple sockets? 
						this.messageBuffer.remove(msg);
						break;
						
					}
				}
				suspendThread();
		}	
	}

}
