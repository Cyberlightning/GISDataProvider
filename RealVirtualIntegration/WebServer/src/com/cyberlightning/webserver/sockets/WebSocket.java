package com.cyberlightning.webserver.sockets;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import javax.xml.bind.DatatypeConverter;

import org.json.simple.JSONObject;

import com.cyberlightning.webserver.StaticResources;
import com.cyberlightning.webserver.interfaces.IMessageEvent;
import com.cyberlightning.webserver.services.MessageService;
import com.cyberlightning.webserver.services.TranslationService;

public class WebSocket extends Thread implements IMessageEvent {
  
  private int port;
  private ByteBuffer byteBuffer;

  private InputStream inputStream;
  private OutputStream outputStream;
  private ServerSocket serverSocket;
  private Socket websocket;
  private ArrayList<String> sendBuffer = new ArrayList<String>();
  
  public static final int DEFAULT_BUFFER_SIZE = 1024;
  public static final int MAX_CLIENT_CONNECTIONS = 50;
  public static final String SAMPLE_JSON = "{\"number\":1, \"value\":8},{\"number\":2, \"value\":16},{\"number\":3, \"value\":32},{\"number\":4, \"value\":64}";
  public WebSocket() {
	  this(StaticResources.WEB_SOCKET_PORT);
  }
  
  public WebSocket( int _port ) {
    this.port = _port;
    this.initialize();
    MessageService.getInstance().registerReceiver(this);
  }
  
  private void initialize () {
	  
	  this.byteBuffer =  ByteBuffer.allocate( DEFAULT_BUFFER_SIZE );
	  try {
		this.serverSocket = new ServerSocket(this.port);
		this.websocket = this.serverSocket.accept();

		this.inputStream = this.websocket.getInputStream();
		this.outputStream = this.websocket.getOutputStream();
	
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

  } 
  
  @Override
  public void run () {
	  
	  
	  while(!this.isInterrupted()) {
		  
		  	
		  	try {

			  	BufferedReader inboundBuffer= new BufferedReader(new InputStreamReader(this.inputStream));
			  	while (inboundBuffer.ready()) {
					readSocket();
				}
			  	inboundBuffer = null;
			  	this.sendBuffer.add(SAMPLE_JSON);
		  		if( this.sendBuffer.size() > 0) {
		  			try {
						send(this.sendBuffer.get(this.sendBuffer.size() - 1));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		  		}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	  }
    }
  
  private void send(String message) throws Exception {  
      
      byte[] utf = message.getBytes("UTF8");  
        
      this.outputStream.write(129);  
        
      if(utf.length > 65535) {  
    	  this.outputStream.write(127);  
    	  this.outputStream.write(utf.length >> 16);  
    	  this.outputStream.write(utf.length >> 8);  
    	  this.outputStream.write(utf.length);  
      }  
      else if(utf.length>125) {  
    	  this.outputStream.write(126);  
    	  this.outputStream.write(utf.length >> 8);  
    	  this.outputStream.write(utf.length);  
      }  
      else {  
    	  this.outputStream.write(utf.length);  
      }  
        
      this.outputStream.write(utf);  
  }  
  private void readSocket() throws IOException {
	
	  
	  String msg = null;
		try {
			msg = readMessage();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  if (msg.toLowerCase().contains("sec-websocket-key")) {

			try {
				this.handshake(msg);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	  } else {
		  if (msg != null )MessageService.getInstance().broadcastWebSocketMessageEvent(msg);
	  }
	    
  }
  
  private String readMessage() throws Exception {  
    
      
      int opcode = this.inputStream.read();  
      boolean whole = (opcode & 0b10000000) !=0;  
      opcode = opcode & 0xF;  
        
      //if(opcode!=1)  
        //  throw new IOException("Wrong opcode: " + opcode);  
        
      int len = this.inputStream.read();  
      boolean encoded = (len >= 128);  
        
      if(encoded)  
          len -= 128;  
        
      if(len == 127) {  
          len = (this.inputStream.read() << 16) | (this.inputStream.read() << 8) | this.inputStream.read();  
      }  
      else if(len == 126) {  
          len = (this.inputStream.read() << 8) | this.inputStream.read();  
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
     // this.inputStream = null;  
      return new String(frame, "UTF8");  
  }  
  
  private void readFully(byte[] b) throws IOException {  
      
      int readen = 0;  
      while(readen<b.length)  
      {  
          int r = this.inputStream.read(b, readen, b.length-readen);  
          if(r==-1)  
              break;  
          readen+=r;  
      }  
  }  
  
  private void handshake(String _key) throws IOException, NoSuchAlgorithmException {  

	  PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(this.outputStream, "UTF8"));
	  String response = _key.substring(_key.indexOf(":") + 1).trim() + StaticResources.MAGIC_STRING;  

      byte[] digest = MessageDigest.getInstance("SHA-1").digest(response.getBytes("UTF8"));
     
      response = DatatypeConverter.printBase64Binary(digest);  

      printWriter.println("HTTP/1.1 101 Switching Protocols");  
      printWriter.println("Upgrade: websocket");  
      printWriter.println("Connection: Upgrade");  
      printWriter.println("Sec-WebSocket-Accept: " + response);  
      printWriter.println();  
      printWriter.flush();  
        
  }

  @Override
  public void httpMessageEvent(String msg) {
	// TODO Auto-generated method stub
	
  }	

  @Override
  public void coapMessageEvent(DatagramPacket _datagramPacket) {
	// TODO Auto-generated method stub
	
  }

  @Override
  public void webSocketMessageEvent(String msg) {
	// TODO Auto-generated method stub
	
  }
}