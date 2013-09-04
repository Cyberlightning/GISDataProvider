package com.cyberlightning.webserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UdpServer implements MessageSender,Runnable  {
	
	private DatagramSocket serverSocket;


	@Override
	public void run() {
		
		try {
			serverSocket = new DatagramSocket(9876);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         
		byte[] receiveData = new byte[512];
        byte[] sendData = new byte[512];
         
        while(true) {
        	
        	DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
				serverSocket.receive(receivePacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            String sentence = new String( receivePacket.getData());
            InetAddress IPAddress = receivePacket.getAddress();
               int port = receivePacket.getPort();
               String capitalizedSentence = sentence.toUpperCase();
               sendData = capitalizedSentence.getBytes();
               DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
               try {
				serverSocket.send(sendPacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            }
   }
		
	

	@Override
	public void sendMessage(String msg) {
		// TODO Auto-generated method stub
		
	}

}
