
package com.cyberlightning.webserver.sockets;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;

import com.cyberlightning.webserver.StaticResources;
import com.cyberlightning.webserver.entities.Client;
import com.cyberlightning.webserver.services.MessageService;
import com.cyberlightning.webserver.services.ProfileService;

public class WebSocket implements Runnable  {
	
	private Socket webSocket;
	private ServerSocket tcpSocket;
	private InputStream input;
	private OutputStream output;
	private String serverResponse = new String();
	private ArrayList<Thread> spawnedThreads = new ArrayList<Thread>();
	private HashMap<InetAddress,Long> connectedWebSockets = new HashMap<InetAddress,Long>();

	
	private int port;

	public static final String WEB_SOCKET_SERVER_RESPONSE = 
			"HTTP/1.1 101 Switching Protocols\r\n"	+
			"Upgrade: websocket\r\n"	+
			"Connection: Upgrade\r\n" +
			"Sec-WebSocket-Accept: ";

			
	public WebSocket () {
		this(StaticResources.WEB_SOCKET_PORT);
	}
	
	public WebSocket(int _port) {
		this.port = _port;
		this.intialize();
	}
	
	private void intialize() {
		
		try {
			tcpSocket = new ServerSocket (this.port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {

		while(true) {
			
			try {
				
				this.webSocket = this.tcpSocket.accept();
				System.out.println("new client attempting connection");
				
				this.input = this.webSocket.getInputStream();
				this.output = this.webSocket.getOutputStream(); 
				
				BufferedReader inboundBuffer= new BufferedReader(new InputStreamReader(this.input));
				DataOutputStream outboundBuffer = new DataOutputStream(this.output);
				
				String line;
				while( !(line=inboundBuffer.readLine()).isEmpty() ) {  
					 parseRequestLine(line);                 
				}  
				
				System.out.println("SERVER RESPONSE: " + this.serverResponse);
				outboundBuffer.writeBytes(this.serverResponse);
				outboundBuffer.flush();
				
				System.out.println("Handshake complete");
				
				if (!this.connectedWebSockets.containsKey(this.webSocket.getInetAddress())) {
					
					
					this.spawnedThreads.add(new Thread((Runnable)(new WebClientWorker(this, this.webSocket))));
					this.connectedWebSockets.put(this.webSocket.getInetAddress(), this.spawnedThreads.get(this.spawnedThreads.size() - 1).getId());
						
					for (Thread t : this.spawnedThreads) {
						if(t.getId() == this.connectedWebSockets.get(this.webSocket.getInetAddress()) && !t.isAlive()) t.start(); 
						System.out.println("Thread started and taking over client");
					}
				}
				
				
				

			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("UNGRACEFULLY SHUTDOWN SOCKET: " + e.getLocalizedMessage());
			}
		
       }
   }
	
	public void removeSocket(Socket _socket) {
		
		for (int i = 0; i < this.spawnedThreads.size(); i++) {
			if(this.spawnedThreads.get(i).getId() == this.connectedWebSockets.get(_socket.getInetAddress())) {
				this.spawnedThreads.remove(i);
				this.connectedWebSockets.remove(_socket.getInetAddress());
				break;
			}
		}
		
		
	}
	
	private void parseRequestLine(String _request)  {
		System.out.println("CLIENT REQUEST: " +_request);
		if (_request.contains("Sec-WebSocket-Key: ")) {
			this.serverResponse = WEB_SOCKET_SERVER_RESPONSE + generateSecurityKeyAccept(_request.replace("Sec-WebSocket-Key: ", "")) + "\r\n\r\n";
		} if (_request.contains("Host: ")) {
			registerClient(_request.replace("Host: ", ""));
		}
	}
	
	private void registerClient(String _client) {
		String ip4v = "";
		String port = "";
		
		for (int i = 0; i < _client.length();i++) {
			if(Character.toString(_client.charAt(i)).compareTo(":") == 0) {
				for (int j = i ; j < _client.length(); j++) {
					if (Character.isDigit(_client.charAt(j)) && !Character.isSpaceChar(_client.charAt(j))) {
						port += _client.charAt(j);
					}
				}
				break;
			} else {
				if (!Character.isSpaceChar(_client.charAt(i))) ip4v += _client.charAt(i);
			}
		}
		
		//ProfileService.getInstance().registerClient(new Client(ip4v, Integer.parseInt(port), StaticResources.CLIENT_PROTOCOL_TCP));
	}
	
	private String generateSecurityKeyAccept (String _secKey) {
		
		try {
			MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
			byte[] secKeyByte = (_secKey + StaticResources.MAGIC_STRING).getBytes();
			secKeyByte = sha1.digest(secKeyByte);
			_secKey = Base64.encodeBase64String(secKeyByte);
			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return _secKey;
	}
	
	
}
