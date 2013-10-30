package com.cyberlightning.webserver.interfaces;

public interface IMessageEvent {
	public void onMessageReceived(int _type, Object _msg);
	
}
