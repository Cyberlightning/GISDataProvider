package com.cyberlightning.webserver.entities;

import java.net.InetAddress;
import java.util.ArrayList;
/**
 * 
 * @author Tomi
 *
 */
public class MessageHeader {
	
	private ArrayList<String> targetUUIDs = new ArrayList<String>();
	private InetAddress origin;


	/**
	 * 
	 * @param _senderUuid
	 * @param _origin
	 */
	public MessageHeader (String _senderUuid, InetAddress _origin ) {
		this.targetUUIDs.add(_senderUuid);
		this.origin = _origin;
		
	}
	/**
	 * 
	 * @param _senderUuid
	 * @param _origin
	 */
	public MessageHeader (String[] _senderUuid, InetAddress _origin) {
		for (String uuid : _senderUuid) {
			this.targetUUIDs.add(uuid);
		}
		this.origin = _origin;
	}
	
	public ArrayList<String> getTargetUUids () {
		return this.targetUUIDs;
	}
	
	public InetAddress getOriginAddress () {
		return this.origin;
	}
	
}
