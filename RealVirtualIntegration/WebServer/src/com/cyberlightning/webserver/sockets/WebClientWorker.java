package com.cyberlightning.webserver.sockets;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;

import com.cyberlightning.webserver.StaticResources;
import com.cyberlightning.webserver.interfaces.IMessageEvent;
import com.cyberlightning.webserver.services.MessageService;

public class WebClientWorker implements Runnable, IMessageEvent {

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
		
		MessageService.getInstance().registerReceiver(this);
		System.out.println(this.clientSocket.getInetAddress().getAddress().toString() + StaticResources.CLIENT_CONNECTED);
		
		while(this.isConnected) {
			
			try {
				if(this.sendBuffer.listIterator().hasNext()) {
					this.send(this.getMessage());
				}
				
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
		
		MessageService.getInstance().unregisterReceiver(this);
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
    
	private String getMessage() {
		String msg = this.sendBuffer.get(this.sendBuffer.size() - 1);
		this.sendBuffer.remove(this.sendBuffer.size() - 1);
		return msg;
	}
	
	@Override
	public void coapMessageEvent(DatagramPacket _datagramPacket) {
		
		try {
			String _content = new String(_datagramPacket.getData(), "utf8");
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

	@Override
	public void webSocketMessageEvent(String _msg, String address) {
		//System.out.println("message from client: " + _msg);
		
	}

	@Override
	public void httpMessageEvent(String address, String msg) {
		// TODO Auto-generated method stub
		
	}

}
