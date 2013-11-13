package com.cyberlightning.webserver.sockets;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.codec.binary.Base64;

import com.cyberlightning.webserver.StaticResources;
import com.cyberlightning.webserver.interfaces.IMessageEvent;
import com.cyberlightning.webserver.services.DataStorageService;
import com.cyberlightning.webserver.services.MessageService;

public class WebSocketWorker implements Runnable {

	private Socket clientSocket;
	private String serverResponse = new String();
	private InputStream input;
	private OutputStream output;
	private SendWorker sendWorker;
	
	private volatile boolean isConnected = true;
	public final String uuid = UUID.randomUUID().toString();
	public final int type =  StaticResources.TCP_CLIENT;
	
	public static final String WEB_SOCKET_SERVER_RESPONSE = 
			"HTTP/1.1 101 Switching Protocols\r\n"	+
			"Upgrade: websocket\r\n"	+
			"Connection: Upgrade\r\n" +
			"Sec-WebSocket-Accept: ";

	/**
	 * 	
	 * @param _parent
	 * @param _client
	 */
	public WebSocketWorker (Socket _client) {
		this.clientSocket = _client;
		
	}
	
	/**
	 * Handles handshaking between connecting client and server
	 */
	private void doHandShake() {
		try {
			this.input = this.clientSocket.getInputStream();
			this.output = this.clientSocket.getOutputStream();
			System.out.println("new client attempting connection");
			BufferedReader inboundBuffer= new BufferedReader(new InputStreamReader(this.input));
			DataOutputStream outboundBuffer = new DataOutputStream(this.output);
			
			String line;
			while( !(line=inboundBuffer.readLine()).isEmpty() ) {  
				 parseRequestLine(line);                 
			}  
			
			outboundBuffer.writeBytes(this.serverResponse);
			outboundBuffer.flush();
			
			System.out.println("Handshake complete");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Parses the client request for security key and generates header for server response to complete the handshake
	 * @param _request
	 */
	private void parseRequestLine(String _request)  {
		System.out.println("CLIENT REQUEST: " +_request);
		if (_request.contains("Sec-WebSocket-Key: ")) {
			this.serverResponse = WEB_SOCKET_SERVER_RESPONSE + generateSecurityKeyAccept(_request.replace("Sec-WebSocket-Key: ", "")) + "\r\n\r\n";
		} 
	}
	
	/**
	 * Generates security key for the session using magic string and generated key.
	 * @param _secKey Client security key
	 * @return Server security key
	 */
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
	
	@Override
	public void run() {
	
		System.out.println(this.clientSocket.getInetAddress().getAddress().toString() + StaticResources.CLIENT_CONNECTED);
		this.doHandShake();
		
		this.sendWorker = new SendWorker();
		
		while(this.isConnected) {
		
			try {
					int opcode = this.input.read();  
				    @SuppressWarnings("unused")
					boolean whole = (opcode & 0b10000000) !=0;  
				    opcode = opcode & 0xF;
				    
				    if (opcode != 8) { 
				    	 
				    	//handleClientMessage(read());  //TODO implement how to subscribe by basestation id not socket class uuid.
				    	testHandle(read());
				    	
				    	 
				    } else {
				    	 
					    /*|Opcode  | Meaning                             | Reference |
					     -+--------+-------------------------------------+-----------|
					      | 0      | Continuation Frame                  | RFC 6455  |
					     -+--------+-------------------------------------+-----------|
					      | 1      | Text Frame                          | RFC 6455  |
					     -+--------+-------------------------------------+-----------|
					      | 2      | Binary Frame                        | RFC 6455  |
					     -+--------+-------------------------------------+-----------|
					      | 8      | Connection Close Frame              | RFC 6455  |
				     	 -+--------+-------------------------------------+-----------|
					      | 9      | Ping Frame                          | RFC 6455  |
					     -+--------+-------------------------------------+-----------|
					      | 10     | Pong Frame                          | RFC 6455  |*/
				    	 
				    	System.out.println("Client message type: " + opcode);
				    	this.closeSocketGracefully();
				    }
				
				
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Connecttion interrupted: " + e.getLocalizedMessage());
				this.closeSocketGracefully();
			}
		}
		System.out.println(this.clientSocket.getInetAddress().getAddress().toString() + StaticResources.CLIENT_DISCONNECTED);
		return;	//Exits thread
	}
	
	private void testHandle(String _request) { 
		ArrayList<String> devices = new ArrayList<String> ();
		devices.add(UdpSocket.uuid);
		MessageService.getInstance().subscribeByIds(devices, this.uuid);
		
	}
	
	private void handleClientMessage(String _request) {
		String[] result = _request.split("\n");
		int fromIndex =  result[0].indexOf("?");
		int toIndex = result[0].indexOf("HTTP");
		
		/* Passes the urlencoded query string to appropriate http method handlers*/
		if (result[0].trim().toUpperCase().contains("GET")) {
			this.handleGETMethod(result[0].substring(fromIndex + 1, toIndex).trim());
		
		}
		else if (result[0].trim().toUpperCase().contains("POST")) {
			
			fromIndex =  result[0].indexOf("/");
			String content = result[0].substring(fromIndex, toIndex);
			if (content.trim().contentEquals("/")) {
				this.handlePOSTMessage(result[result.length-1].toString());
			} else {
				send(StaticResources.ERROR_CODE_METHOD_NOT_ALLOWED);
			}
			
		}
		else if (result[0].trim().toUpperCase().contains("PUT")) {
			send(StaticResources.ERROR_CODE_METHOD_NOT_ALLOWED);
		}
		else if (result[0].trim().toUpperCase().contains("DELETE")) {
			send(StaticResources.ERROR_CODE_METHOD_NOT_ALLOWED);
		}
	}
	
	/**
	 * 
	 * @param _msg
	 */
	private void handlePOSTMessage(String _msg) { //TODO design post method options
		
		String[] queries = _msg.split("&");
		String[] targetUUIDs = null;
			
		for (int i = 0; i < queries.length; i++) {
				
			if(queries[i].contains("action")) {
				String[] action = queries[i].split("=");
					
				if (action[1].contentEquals("update")) {
					
					for (int j = 0; j < queries.length; j++) {
					
						if (queries[j].contains("device_id")) {
							String[] s = queries.clone()[j].trim().split("=");
							targetUUIDs = s[1].split(","); //check correct regex
						} 
					}
				}
			}
				
		} if (targetUUIDs == null) {
			try {
				send(StaticResources.ERROR_CODE_BAD_REQUEST);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			MessageService.getInstance().addToMessageBuffer(new MessageObject(this.uuid,StaticResources.TCP_CLIENT,DataStorageService.getInstance().resolveBaseStationAddresses(targetUUIDs),_msg));
		}
	}
	
	/**
	 * 
	 * @param _content
	 */
	private void handleGETMethod(String _content) {
		
		String[] queries = _content.split("&");
		
		for (int i = 0; i < queries.length; i++) {
			if(queries[i].contains("action")) {
				String[] action = queries[i].split("=");
				if (action[1].contentEquals("subscribeById")) {
					for (int j = 0; j < queries.length;j++) {
						if (queries[j].contains("device_id")) {
							String[] device = queries[j].split("=");
							String[] targetUUIDs = device[1].split(",");
							MessageService.getInstance().subscribeByIds(DataStorageService.getInstance().resolveBaseStationUuids(targetUUIDs),uuid);
						}
					}
					
				} else if (action[1].contentEquals("unsubscribeById")) {
					
					//TODO not implemented yet
				} else if (action[1].contentEquals("unsubscribeAll")) {
					MessageService.getInstance().unregisterReceiver(uuid);
				}
			}
		}
	}
	
	/**
	 * 
	 */
	private void closeSocketGracefully() {
		try {	
			MessageService.getInstance().unsubscribeAllById(this.uuid);
			this.sendWorker.destroy();
			this.isConnected = false;
			this.input.close();
			this.clientSocket.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 
	 * @param b
	 * @throws IOException
	 */
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
	
    /**
     *  
     * @return
     * @throws Exception
     */
    private String read() throws Exception {  
          
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
    /**
	 * 
	 * @param message
	 * @throws Exception
	 */
	private void send(String message)  {  
          
        try {
        	byte[] utf = message.getBytes("UTF8");  
            
            output.write(129);  
              
            if(utf.length > 65535) {  
            output.write(127);  
            output.write(utf.length >> 16);  
            output.write(utf.length >> 8);  
            output.write(utf.length);  
            }  
            else if(utf.length>125) {  
            output.write(126);  
            output.write(utf.length >> 8);  
            output.write(utf.length);  
            }  
            else {  
            output.write(utf.length);  
            }  
              
            output.write(utf);
            
        } catch(Exception e) {
        	
        	e.printStackTrace();
        }
		

    } 
  
	private class SendWorker implements Runnable,IMessageEvent {
		
		
		public CopyOnWriteArrayList<MessageObject> sendBuffer = new CopyOnWriteArrayList<MessageObject>();
		private boolean suspendFlag = true;
		private boolean destroyFlag = false;
		private Thread thread;
		
		public SendWorker() {
			this.thread = new Thread(this);
			this.thread.start();
		}
		@Override
		public void run() {
			
			MessageService.getInstance().registerReceiver(this,uuid);
			
			while (!destroyFlag) {
				
				synchronized(this) {
		            
					while(suspendFlag && !destroyFlag) {
						try {
							wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						return;
						}
		            }
		        }
				
				if (sendBuffer.isEmpty()) continue;
				
				try {
					
					Iterator<MessageObject> i = this.sendBuffer.iterator();
		     		while(i.hasNext()) {
		     			
		     			MessageObject msg = i.next();
		     			
		     			if (msg.payload instanceof DatagramPacket) {
		     					String _content = new String(((DatagramPacket)msg.payload).getData(), "utf8");
		     					send(_content);
		     			} else if (msg.payload instanceof String) {
		     						send((String)msg.payload);
		     			}
		     			sendBuffer.remove(msg);
			        	
		     		}
		     		this.suspendThread();
		     		
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					break;
				}      
			}
			MessageService.getInstance().unregisterReceiver(uuid);
			return;
		}
		
		public void suspendThread() {
		      suspendFlag = true;
		}

		private synchronized void wakeThread() {
		      suspendFlag = false;
		      notify();
		}
		
		private synchronized void destroy() {
		      this.destroyFlag = true;
		      notify();
		}
		
		/**
		 * 		
		 */
		@Override
		public void onMessageReceived(MessageObject _msg) {
			this.sendBuffer.add(_msg);
			this.wakeThread();
		}

	}
	

}
