package com.cyberlightning.webserver.interfaces;

import com.cyberlightning.webserver.entities.MessageObject;

public interface IMessageEvent {
	public void onMessageReceived(MessageObject _msg);
}
