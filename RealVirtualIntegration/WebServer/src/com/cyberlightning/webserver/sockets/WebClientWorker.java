package com.cyberlightning.webserver.sockets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.Socket;
import java.util.ArrayList;

import com.cyberlightning.webserver.interfaces.IMessageEvent;
import com.cyberlightning.webserver.services.MessageService;

public class WebClientWorker implements Runnable, IMessageEvent {

	private Socket clientSocket;
	private InputStream input;
	private OutputStream output;
	private ArrayList<String> sendBuffer = new ArrayList<String>();
	
	public WebClientWorker (Socket _client) {
		this.clientSocket = _client;
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
		
		while(this.clientSocket.isConnected()) {
			
			try {
				if(this.sendBuffer.size() > 0) {
					this.send(this.sendBuffer.get(this.sendBuffer.size() -1));
					this.sendBuffer.remove(this.sendBuffer.size() - 1);
				}
				if(this.input.available() > 0) MessageService.getInstance().broadcastWebSocketMessageEvent(read());
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
	
	@Override
	public void httpMessageEvent(String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void coapMessageEvent(DatagramPacket _datagramPacket) {
		byte[] buffer = new byte[_datagramPacket.getData().length];
		buffer = _datagramPacket.getData();
		try {
			this.sendBuffer.add(new String(buffer, "UTF8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		
	}

	@Override
	public void webSocketMessageEvent(String msg) {
		this.sendBuffer.add(msg);
		
	}

}
