package com.cyberlightning.webserver.interfaces;

import java.net.DatagramPacket;

public interface IMessageEvent {
	public void httpMessageEvent(String address, String msg);
	public void coapMessageEvent(DatagramPacket _datagramPacket);
	public void webSocketMessageEvent(String msg, String address);
}
