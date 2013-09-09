package com.cyberlightning.android.coap.sensor;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.cyberlightning.android.coap.StaticResources;



public class UdpSocket implements Runnable {
	
	private boolean isRunning = true;
	private InetAddress serverAddress;

	public boolean initializeUdpSocket(String serverAddr, int serverPort) {
		
		try {
			
			this.serverAddress = InetAddress.getByName(serverAddr);
			
		
		} catch (Exception e) {
			
		}
		
		return true;
	}
	
	@Override
	public void run() {
		
		while(isRunning){
		
		try {
			
			
			
			DatagramSocket socket = new DatagramSocket();
			byte[] buf;
			
			buf = ("Default message").getBytes();
			DatagramPacket packet = new DatagramPacket(buf, buf.length, this.serverAddress, StaticResources.SERVER_UDP_PORT);
			updatetrack("Client: Sending ‘" + new String(buf) + "’n");
			socket.send(packet);
			updatetrack("Client: Message sentn");
			updatetrack("Client: Succeed!n");
			
		} catch (Exception e) {
			updatetrack("Client: Error!n");
		}
	}
	}
	
	public void updatetrack(String s){
//		Message msg = new Message();
//		String textTochange = s;
//		msg.obj = textTochange;
//		Handler.sendMessage(msg);
		System.out.println(s);
	}
}

