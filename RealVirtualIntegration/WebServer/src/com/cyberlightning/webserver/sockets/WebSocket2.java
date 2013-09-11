
package com.cyberlightning.webserver.sockets;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;



//import java.util.Base64;
import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;

import com.cyberlightning.webserver.StaticResources;
import com.cyberlightning.webserver.entities.Client;
import com.cyberlightning.webserver.interfaces.IMessageEvent;
import com.cyberlightning.webserver.services.MessageService;
import com.cyberlightning.webserver.services.ProfileService;
import com.cyberlightning.webserver.services.TranslationService;

public class WebSocket2 extends Thread implements IMessageEvent {
	
	private ArrayList<String> sendBuffer = new ArrayList<String>();
	private Socket webSocket;
	private ServerSocket tcpSocket;
	private InputStream input;
	private OutputStream output;
	
	private boolean isHandshake = false;
	private String serverResponse;

	public static final String WEB_SOCKET_SERVER_RESPONSE = 
			"HTTP/1.1 101 Switching Protocols\r\n"	+
			"Upgrade: websocket\r\n"	+
			"Connection: Upgrade\r\n" +
			"Sec-WebSocket-Accept: ";

			
	public WebSocket2 () {
		MessageService.getInstance().registerReceiver(this);
	}

	@Override
	public void run() {
		
		//BufferedReader inboundBuffer = null;
		//DataOutputStream outboundBuffer = null;
		try {
			tcpSocket = new ServerSocket (StaticResources.WEB_SOCKET_PORT);
			webSocket = tcpSocket.accept();
			this.input = webSocket.getInputStream();
			this.output = webSocket.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      
		while(true) {

			
      	   try {
      		   BufferedReader inboundBuffer= new BufferedReader(new InputStreamReader(this.input));
      		   DataOutputStream outboundBuffer = new DataOutputStream(this.output);
				
				while (inboundBuffer.ready()) {
					parseRequestLine(inboundBuffer.readLine());
				}
				
				if (isHandshake) {
					outboundBuffer.writeBytes(this.serverResponse);
					outboundBuffer.flush();
					this.isHandshake = false;
					
				}
				
				if (this.sendBuffer.size() > 0) {
					
					
					this.send(this.sendBuffer.get(this.sendBuffer.size() - 1));
					this.sendBuffer.remove(this.sendBuffer.size() - 1);
					
				}
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

       }
   }
	
	private void readFully(byte[] b) throws IOException {  
        
        int readen = 0;  
        while(readen<b.length)  
        {  
            int r = this.input.read(b, readen, b.length-readen);  
            if(r==-1)  
                break;  
            readen+=r;  
        }  
    }  
      
    private String read() throws Exception {  
  
        int opcode = this.input.read();  
        boolean whole = (opcode & 0b10000000) !=0;  
        opcode = opcode & 0xF;  
          
        if(opcode!=1)  
            throw new IOException("Wrong opcode: " + opcode);  
          
        int len = this.input.read();  
        boolean encoded = (len >= 128);  
          
        if(encoded)  
            len -= 128;  
          
        if(len == 127) {  
            len = (this.input.read() << 16) | (this.input.read() << 8) | this.input.read();  
        }  
        else if(len == 126) {  
            len = (this.input.read() << 8) | this.input.read();  
        }  
          
        byte[] key = null;  
          
        if(encoded) {  
            key = new byte[4];  
            readFully(key);  
        }  
          
        byte[] frame = new byte[len];  
          
        readFully(frame);  
          
        if(encoded) {  
            for(int i=0; i<frame.length; i++) {  
                frame[i] = (byte) (frame[i] ^ key[i%4]);  
            }  
        }  
          
        return new String(frame, "UTF8");  
    }  
      
    private void send(String message) throws Exception {  
          
        byte[] utf = message.getBytes("UTF8");  
          
        this.output.write(129);  
          
        if(utf.length > 65535) {  
        	this.output.write(127);  
        	this.output.write(utf.length >> 16);  
        	this.output.write(utf.length >> 8);  
        	this.output.write(utf.length);  
        }  
        else if(utf.length>125) {  
        	this.output.write(126);  
        	this.output.write(utf.length >> 8);  
        	this.output.write(utf.length);  
        }  
        else {  
        	this.output.write(utf.length);  
        }  
          
        this.output.write(utf);  
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
		
		ProfileService.getInstance().registerClient(new Client(ip4v, Integer.parseInt(port), StaticResources.CLIENT_PROTOCOL_TCP));
	}
	
	private String generateSecurityKeyAccept (String _secKey) {
		
		try {
			MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
			byte[] secKeyByte = (_secKey + StaticResources.MAGIC_STRING).getBytes();
			secKeyByte = sha1.digest(secKeyByte);
			//_secKey = Base64.getEncoder().encodeToString(secKeyByte); //java.util.base64
			_secKey = Base64.encodeBase64String(secKeyByte);
			
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
