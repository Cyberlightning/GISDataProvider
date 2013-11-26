package com.cyberlightning.realvirtualinteraction.backend.interfaces;

import com.cyberlightning.realvirtualinteraction.backend.sockets.MessageObject;



public interface IMessageEvent {
	public void onMessageReceived(MessageObject _msg);
}
