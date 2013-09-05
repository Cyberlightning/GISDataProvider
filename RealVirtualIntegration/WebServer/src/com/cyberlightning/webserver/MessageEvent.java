package com.cyberlightning.webserver;

import java.net.DatagramPacket;

public interface MessageEvent {
	public void httpMessageEvent(String msg);
	public void udpMessageEvent(DatagramPacket _datagramPacket);
}
