package com.cyberlightning.webserver.sockets;

import java.net.InetSocketAddress;
import java.util.ArrayList;

public class MessageObject  {
	
	public Object payload ;
	public int port;
	public int originType;
	public String originUUID;
	public ArrayList<InetSocketAddress> targetAddresses;
	
	public MessageObject(String _uuid, int _originType, Object _payload){
		this.payload = _payload;
		this.originUUID = _uuid;
		this.originType = _originType;
	}
	
	public MessageObject(String _uuid, int _originType, ArrayList<InetSocketAddress> _targetAddresses, Object _payload){
		this.payload = _payload;
		this.originUUID = _uuid;
		this.targetAddresses = _targetAddresses;
		this.originType = _originType;
	}
}
