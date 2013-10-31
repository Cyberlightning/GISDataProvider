package com.cyberlightning.webserver.sockets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.cyberlightning.webserver.StaticResources;
import com.cyberlightning.webserver.interfaces.IMessageEvent;
import com.cyberlightning.webserver.services.MessageService;

public class WebSocketWorker implements Runnable {

	private Socket clientSocket;
	private InputStream input;
	private OutputStream output;
	private Thread sendWorker;
	private WebSocket parent;
	
	private volatile boolean isConnected = true;

	protected final String uuid = UUID.randomUUID().toString();

	public WebSocketWorker (WebSocket _parent, Socket _client) {
		this.clientSocket = _client;
		this.parent = _parent;
		this.initialize();
	}
	
	private void initialize() {
		try {
			this.input = this.clientSocket.getInputStream();
			this.output = this.clientSocket.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
	
		System.out.println(this.clientSocket.getInetAddress().getAddress().toString() + StaticResources.CLIENT_CONNECTED);
		
		Runnable runnable = new SendWorker(this.uuid);
		this.sendWorker = new Thread(runnable);
		this.sendWorker.run();
		
		while(this.isConnected) {
			
			try {
				
				
				if(this.input.available() > 0) {
					
					int opcode = this.input.read();  
				    @SuppressWarnings("unused")
					boolean whole = (opcode & 0b10000000) !=0;  
				    opcode = opcode & 0xF;
				    
				    if (opcode != 8) { 
				    	 
				    	MessageService.getInstance().messageBuffer.put(this.uuid, read()); 
				    	 
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
				}
				
			} catch (Exception e) {
				
				e.printStackTrace();
				System.out.println("Connecttion interrupted: " + e.getLocalizedMessage());
				this.closeSocketGracefully();
			}
		}
		
		
		this.parent.removeSocket(this.clientSocket);
		System.out.println(this.clientSocket.getInetAddress().getAddress().toString() + StaticResources.CLIENT_DISCONNECTED);
	
		return;
		
	}
	
	private void closeSocketGracefully() {
		try {
			
			this.input.close();
			this.clientSocket.close();
			this.isConnected = false;
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	private class SendWorker implements Runnable,IMessageEvent {
		
	
		@SuppressWarnings("unused")
		public final int type = StaticResources.TCP_CLIENT;
		@SuppressWarnings("unused")
		public String uuid;
		
		private  Map<Integer, Object> sendBuffer = new ConcurrentHashMap<Integer, Object>(); 
		
		public SendWorker(String _uuid) {
			this.uuid = _uuid;
		}
		
		@Override
		public void run() {
			
			MessageService.getInstance().registerReceiver(this,this.uuid);
			
			while (true) {
				if (sendBuffer.isEmpty()) continue;
				
				try {
					
					Iterator<Integer> i = this.sendBuffer.keySet().iterator();
		     		while(i.hasNext()) {
		     			
		     			int key = i.next();
		     			if (sendBuffer.get(key) instanceof DatagramPacket) {
		     					String _content = new String(((DatagramPacket)sendBuffer.get(key)).getData(), "utf8");
		     					this.send(_content);
		     			} else if (sendBuffer.get(key) instanceof String) {
		     					this.send((String)sendBuffer.get(key));
		     			}
		     			sendBuffer.remove(key);
			        	
		     		}
		     		
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					break;
				}      
			}
			MessageService.getInstance().unregisterReceiver(this.uuid);
		}
		

		private void send(String message) throws Exception {  
	          
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

	    }  
		
		@Override
		public void onMessageReceived(int _type, Object _msg) {
			((ConcurrentHashMap<Integer, Object>) this.sendBuffer).putIfAbsent(_type, _msg);
		}

	}
	

}
