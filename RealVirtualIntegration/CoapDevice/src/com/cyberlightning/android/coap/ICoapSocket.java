package com.cyberlightning.android.coap;

import android.os.Message;

public interface ICoapSocket {
	public void broadCastMessage(Message _msg);
	public void sendMessage(Message _msg);
}
