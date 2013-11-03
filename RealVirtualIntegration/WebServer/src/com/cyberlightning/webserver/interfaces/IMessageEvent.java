package com.cyberlightning.webserver.interfaces;

import com.cyberlightning.webserver.sockets.MessageObject;

public interface IMessageEvent {
	public void onMessageReceived(MessageObject _msg);
}
