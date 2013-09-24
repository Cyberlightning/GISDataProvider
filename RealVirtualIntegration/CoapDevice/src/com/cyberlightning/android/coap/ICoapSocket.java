package com.cyberlightning.android.coap;

import java.util.HashMap;

import android.os.Message;

public interface ICoapSocket {
	public void broadCastMessage(Message _msg,HashMap<String,NetworkDevice> _allDevices);
	public void sendMessage(Message _msg);
}
