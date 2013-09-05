package com.cyberlightning.webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import com.cyberlightning.webserver.entities.Client;
import com.cyberlightning.webserver.interfaces.IMessageEvent;
import com.cyberlightning.webserver.services.MessageHandler;
import com.cyberlightning.webserver.services.StaticResources;




public class WebSocketServer implements  IMessageEvent,Runnable {
	
	
	private Socket webSocket;
	private ServerSocket tcpSocket;
	
	public static final String WEB_SOCKET_CLIENT_REQUEST = 
			"GET / HTTP/1.1"	+
			"Upgrade: websocket"	+
			"Connection: Upgrade"	+
			"Host: 127.0.0.1:8999"	+
			"Origin: null"	+
			"Sec-WebSocket-Key: 8rYWWxsBPEigeGKDRNOndg=="	+
			"Sec-WebSocket-Version: 13";
	
	public static final String WEB_SOCKET_SERVER_RESPONSE = 
			"HTTP/1.1 101 Switching Protocols"	+
			"Upgrade: websocket"	+
			"Connection: Upgrade"	+
			"Sec-WebSocket-Accept: 3aDXXmPbE5e9i08zb9mygfPlCVw=";
	

	@Override
	public void run() {
		 
		 String clientSentence;
         String capitalizedSentence;
         
      
   
		try {
			tcpSocket = new ServerSocket (StaticResources.WEB_SOCKET_PORT);
			webSocket = tcpSocket.accept();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		while(true) {
           
			try {
				BufferedReader inboundBuffer = new BufferedReader(new InputStreamReader(webSocket.getInputStream()));
				//DataOutputStream outboundBuffer = new DataOutputStream(webSocket.getOutputStream());
				
				while (inboundBuffer.ready()) {
					parseRequest(inboundBuffer.readLine());
				}
			
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
            
           
         }
     }
	
	private void parseRequest(String _request) {
		CharSequence c = "Sec-WebSocket-Key:";
		if (_request.contains(c)) {
			String s = _request.replace(c, "");
			
		}
	}
	
	public byte[] getHandshake (String firstKey, String secondKey, byte[] last8) //Try this one out
    {
        byte[] toReturn = null;
        //Strip out numbers
        int firstNum = Integer.parseInt(firstKey.replaceAll("\\D", ""));
        int secondNum = Integer.parseInt(secondKey.replaceAll("\\D", ""));

        //Count spaces
        int firstDiv = firstKey.replaceAll("\\S", "").length();
        int secondDiv = secondKey.replaceAll("\\S", "").length();

        //Do the division
        int firstShake = firstNum / firstDiv;
        int secondShake = secondNum / secondDiv;

        //Prepare 128 bit byte array
        byte[] toMD5 = new byte[16];
        byte[] firstByte = ByteBuffer.allocate(4).putInt(firstShake).array();
        byte[] secondByte = ByteBuffer.allocate(4).putInt(secondShake).array();

        //Copy the bytes of the numbers you made into your md5 byte array
        System.arraycopy(firstByte, 0, toMD5, 0, 4);
        System.arraycopy(secondByte, 0, toMD5, 4, 4);
        System.arraycopy(last8, 0, toMD5, 8, 8);
        try
        {
            //MD5 everything together
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            toReturn = md5.digest(toMD5);
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }

        return toReturn;
}

	@Override
	public void httpMessageEvent(String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void udpMessageEvent(DatagramPacket _datagramPacket) {
		// TODO Auto-generated method stub
		
	}
	
}

