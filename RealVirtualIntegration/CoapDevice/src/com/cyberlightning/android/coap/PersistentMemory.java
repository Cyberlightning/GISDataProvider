package com.cyberlightning.android.coap;



public class PersistentMemory {

	private static final PersistentMemory _persistentMemory = new PersistentMemory();

	public final String SERVICE_TYPE = "_coap._udp";
	public static final String SERVICE_NAME = "Base Station Service";
	public final int DEFAULT_PORT = 5683;
	public final int DEFAULT_BUFFER_SIZE = 1024;
	private static final long defaultBroadcastInterval = 6000;
	private long broadcastInterval = 0; //TODO local file storage
	
	
	private PersistentMemory() {
		
	}
	
	public static PersistentMemory getInstance () {
		return _persistentMemory;
	}
	
	public long getBroadcastInterval () {
		if (broadcastInterval == 0) return defaultBroadcastInterval;
		else return broadcastInterval;
	}
}
