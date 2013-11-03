package com.cyberlightning.webserver.sockets;

import java.net.InetAddress;
import java.util.ArrayList;

public class MessageObject  {
	
	public Object payload ;
	
	public int originType;
	public String originUUID;
	public ArrayList<InetAddress> targetAddresses;
	
	public MessageObject(String _uuid, int _originType, Object _payload){
		this.payload = _payload;
		this.originUUID = _uuid;
		this.originType = _originType;
	}
	
	public MessageObject(String _uuid, int _originType, ArrayList<InetAddress> _targetAddresses, Object _payload){
		this.payload = _payload;
		this.originUUID = _uuid;
		this.targetAddresses = _targetAddresses;
		this.originType = _originType;
	}
}
