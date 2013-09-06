package com.cyberlightning.webserver.sockets;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;

import com.cyberlightning.webserver.Application;
import com.cyberlightning.webserver.StaticResources;
import com.cyberlightning.webserver.entities.Client;
import com.cyberlightning.webserver.interfaces.IMessageEvent;
import com.cyberlightning.webserver.services.ProfileService;

public class WebSocket extends Thread implements IMessageEvent {
	
	private ArrayList<String> sendBuffer;
	private Socket webSocket;
	private ServerSocket tcpSocket;
	
	
	private boolean isHandshake = false;
	private String serverResponse;

	public static final String WEB_SOCKET_SERVER_RESPONSE = 
			"HTTP/1.1 101 Switching Protocols\r\n"	+
			"Upgrade: websocket\r\n"	+
			"Connection: Upgrade\r\n" +
			"Sec-WebSocket-Accept: ";

			
	public WebSocket () {
		
		Application.executor.execute(this);
	}

	@Override
	public void run() {

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
				DataOutputStream outboundBuffer = new DataOutputStream(webSocket.getOutputStream());
				
				while (inboundBuffer.ready()) {
					parseRequestLine(inboundBuffer.readLine());
				}
				
				if (isHandshake) {
					
					outboundBuffer.writeBytes(this.serverResponse);
					outboundBuffer.flush();
					this.isHandshake = false;
				}

			
				if (this.sendBuffer.size() > 0) {
					
					outboundBuffer.write(broadcast(this.sendBuffer.get(this.sendBuffer.size() - 1)));
					this.sendBuffer.remove(this.sendBuffer.size() - 1);
					outboundBuffer.flush();
				}
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

         }
     }
	
	public byte[] broadcast(String mess) throws IOException{
	    byte[] rawData = mess.getBytes();

	    int frameCount  = 0;
	    byte[] frame = new byte[10];

	    frame[0] = (byte) 129;

	    if(rawData.length <= 125){
	        frame[1] = (byte) rawData.length;
	        frameCount = 2;
	    }else if(rawData.length >= 126 && rawData.length <= 65535){
	        frame[1] = (byte) 126;
	        byte len = (byte) rawData.length;
	        frame[2] = (byte)((len >> 8 ) & (byte)255);
	        frame[3] = (byte)(len & (byte)255); 
	        frameCount = 4;
	    }else{
	        frame[1] = (byte) 127;
	        byte len = (byte) rawData.length;
	        frame[2] = (byte)((len >> 56 ) & (byte)255);
	        frame[3] = (byte)((len >> 48 ) & (byte)255);
	        frame[4] = (byte)((len >> 40 ) & (byte)255);
	        frame[5] = (byte)((len >> 32 ) & (byte)255);
	        frame[6] = (byte)((len >> 24 ) & (byte)255);
	        frame[7] = (byte)((len >> 16 ) & (byte)255);
	        frame[8] = (byte)((len >> 8 ) & (byte)255);
	        frame[9] = (byte)(len & (byte)255);
	        frameCount = 10;
	    }

	    int bLength = frameCount + rawData.length;

	    byte[] reply = new byte[bLength];

	    int bLim = 0;
	    for(int i=0; i<frameCount;i++){
	        reply[bLim] = frame[i];
	        bLim++;
	    }
	    for(int i=0; i<rawData.length;i++){
	        reply[bLim] = rawData[i];
	        bLim++;
	    }
	    return reply;
	   

	}
	
	private void parseRequestLine(String _request)  {
		
		if (_request.contains("Sec-WebSocket-Key: ")) {
			this.isHandshake = true;
			this.serverResponse = WEB_SOCKET_SERVER_RESPONSE + generateSecurityKeyAccept(_request.replace("Sec-WebSocket-Key: ", "")) + "\r\n\r\n";
		} if (_request.contains("Host: ")) {
			registerClient(_request.replace("Host: ", ""));
		}
	}
	
	private void registerClient(String _client) {
		String ip4v = "";
		int port = 0;
		
		for (int i = 0; i < _client.length();i++) {
			while(Character.toString(_client.charAt(i)).compareTo(":") != 0) {
				if (!Character.isSpaceChar(_client.charAt(i))) ip4v += _client.charAt(i);
				i++;
			}
			if (Character.isDigit(_client.charAt(i))) port += _client.charAt(i);
		}
		ProfileService.getInstance().registerClient(new Client(ip4v, port, StaticResources.CLIENT_PROTOCOL_TCP));
	}
	
	private String generateSecurityKeyAccept (String _secKey) {
		
		try {
			MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
			byte[] secKeyByte = (_secKey + StaticResources.MAGIC_STRING).getBytes();
			secKeyByte = sha1.digest(secKeyByte);
			_secKey = Base64.getEncoder().encodeToString(secKeyByte);
			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return _secKey;
	}
	


	@Override
	public void httpMessageEvent(String msg) { 
		// TODO Auto-generated method stub
		
	}

	@Override
	public void coapMessageEvent(DatagramPacket _datagramPacket) {
		this.sendBuffer.add(_datagramPacket.getData().toString());
		
	}

	@Override
	public void webSocketMessageEvent(String msg) {
		// TODO Auto-generated method stub
		
	}
	
}

