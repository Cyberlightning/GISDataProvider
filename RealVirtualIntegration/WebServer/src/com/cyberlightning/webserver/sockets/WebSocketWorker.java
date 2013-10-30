package com.cyberlightning.webserver.sockets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.cyberlightning.webserver.StaticResources;
import com.cyberlightning.webserver.interfaces.IMessageEvent;
import com.cyberlightning.webserver.services.MessageService;

public class WebClientWorker implements Runnable {

	private Socket clientSocket;
	private InputStream input;
	private OutputStream output;
	private ArrayList<String> sendBuffer = new ArrayList<String>();
	private WebSocket parent;
	private boolean isConnected = true;

	
	public WebClientWorker (WebSocket _parent, Socket _client) {
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
		
		while(this.isConnected) {
			
			try {
				
				
				if(this.input.available() > 0) {
					
					int opcode = this.input.read();  
				    @SuppressWarnings("unused")
					boolean whole = (opcode & 0b10000000) !=0;  
				    opcode = opcode & 0xF;
				    System.out.println("Client message type: " + opcode);
				    
				    if (opcode != 8) { 
				    	 
				    	MessageService.getInstance().broadcastWebSocketMessageEvent(read(), this.clientSocket.getInetAddress().getHostAddress()); 
				    	 
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
				    	 
				    	this.closeSocketGracefully();
				    }
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
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
      
    
    
	private String getMessage() {
		String msg = this.sendBuffer.get(this.sendBuffer.size() - 1);
		this.sendBuffer.remove(this.sendBuffer.size() - 1);
		return msg;
	}
	

	private class SendWorker implements Runnable,IMessageEvent {
		
		public  Map<Integer, Object> sendBuffer = new ConcurrentHashMap<Integer, Object>(100); 
		
		public final String uuid = UUID.randomUUID().toString();
		public final int type = StaticResources.TCP_CLIENT;

		@Override
		public void run() {
			
			MessageService.getInstance().registerReceiver(this);
			
			while (true) {
				 
				if (sendBuffer.isEmpty()) continue;
		        	   
		        	   try {
		        		   if (_msg instanceof DatagramPacket) {
		       				try {
		       					String _content = new String(((DatagramPacket) _msg).getData(), "utf8");
		       					System.out.println(_content);
		       					this.send(_content);
		       				} catch (UnsupportedEncodingException e1) {
		       					// TODO Auto-generated catch block
		       					e1.printStackTrace();
		       				} catch (Exception e) {
		       					// TODO Auto-generated catch block
		       					e.printStackTrace();
		       				}
		       			}
		        		   Iterator<String> i = sendBuffer.keySet().iterator();
		        		
		        		   while(i.hasNext()) {
		        			   String key = i.next();
		        			   //byte[] b = this.formatJSON(sendBuffer.get(key));
		        			   //DatagramPacket packet = new DatagramPacket(b, b.length, "ds", 23);
		        			
			        			serverSocket.send(sendBuffer.get(key));
			        			sendBuffer.remove(key);
		        		   }
		   				
		   				} catch (IOException e) {
		   					// TODO Auto-generated catch block
		   					e.printStackTrace();
		   					break;
		   				} 
		           
			}
			MessageService.getInstance().unregisterReceiver(this);
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
			this.sendBuffer.putIfAbsent(_type, _msg);
		}
	}
	

}
