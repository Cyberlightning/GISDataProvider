package com.cyberlightning.webserver.sockets;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.StringTokenizer;

import com.cyberlightning.webserver.StaticResources;
import com.cyberlightning.webserver.interfaces.IMessageEvent;
import com.cyberlightning.webserver.services.MessageService;

public class HttpSocket implements Runnable,IMessageEvent{

private Socket clientSocket;
private ServerSocket serverSocket;
private DataOutputStream output;
private BufferedReader input;
private int port;

public HttpSocket() {
	this(StaticResources.SERVER_PORT);
}
public HttpSocket(int _port) {
	this.port = _port;
	this.initialize();
}

public void initialize() {
	try {
		this.serverSocket = new ServerSocket (this.port, StaticResources.MAX_CONNECTED_CLIENTS, InetAddress.getByName(StaticResources.LOCAL_HOST));
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	MessageService.getInstance().registerReceiver(this);
}

@Override
public void run() {

	while(true) {
		
		try {
			this.clientSocket = this.serverSocket.accept();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			
			this.input =  new BufferedReader(new InputStreamReader (clientSocket.getInputStream()));
			this.output = new DataOutputStream(clientSocket.getOutputStream());
			String request = "";
			
			while(input.ready()){
				request += input.readLine(); 
		}
			
			StringTokenizer tokenizer = new StringTokenizer(request);
			String httpMethod = tokenizer.nextToken();
			String httpQueryString = tokenizer.nextToken();
			
			if (httpMethod.equals("GET")) 			{
				this.handGETMethod(httpQueryString);
			}
			else if (httpMethod.equals("POST"))  	{
				this.handPOSTMethod(httpQueryString.replaceFirst("/", ""));
			}
			else if (httpMethod.equals("PUT")) 		{
				this.handPUTMethod(httpQueryString);
			}
			else if (httpMethod.equals("DELETE")) 	{
				this.handDELETEMethod(httpQueryString);
			}
			else System.out.println(httpMethod.toString());
	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}

private void handGETMethod(String _query) throws Exception {
	
	if (_query.equals("/")) {
		sendResponse(200, "dev.cyberlightning.com/~tsarni/index.html", true); //absolute path for local dev
		
	} else {
		//This is interpreted as a file name
		String fileName = _query.replaceFirst("/", "");
		fileName = URLDecoder.decode(fileName);

		if (new File(fileName).isFile()){ //this is relative path
			sendResponse(200, fileName, true);	
		}
		else {
			sendResponse(404, "404.html", false);
		}
	}
}

private void handPOSTMethod(String _action) {
	MessageService.getInstance().broadcastHttpMessageEvent(_action);	

}

private void handPUTMethod(String _request) {
	//TODO handPUTMethod
}

private void handDELETEMethod(String _request) {
	//TODO handDELETEMethod
}

public void sendResponse (int statusCode, String responseString, boolean isFile) throws Exception {

	String statusLine = null;
	String serverdetails = StaticResources.SERVER_DETAILS;
	String contentLengthLine = null;
	String fileName = null;
	String contentTypeLine = "Content-Type: text/html" + "\r\n";
	FileInputStream fin = null;

	if (statusCode == 200)
		statusLine = "HTTP/1.1 200 OK" + "\r\n";
	else
		statusLine = "HTTP/1.1 404 Not Found" + "\r\n";

	if (isFile) {
		fileName = responseString;
		fin = new FileInputStream(fileName);
		contentLengthLine = "Content-Length: " + Integer.toString(fin.available()) + "\r\n";
		if (!fileName.endsWith(".htm") && !fileName.endsWith(".html"))
			contentTypeLine = "Content-Type: \r\n";
	} else {
		contentLengthLine = "Content-Length: " + responseString.length() + "\r\n";
	}

	this.output.writeBytes(statusLine);
	this.output.writeBytes(serverdetails);
	this.output.writeBytes(contentTypeLine);
	this.output.writeBytes(contentLengthLine);
	this.output.writeBytes("Connection: close\r\n");
	this.output.writeBytes("\r\n");

	if (isFile) {
		sendFile(fin, this.output);
	}
	else {
		this.output.writeBytes(responseString);
	}
	
	this.output.close();
}

public void sendFile (FileInputStream fin, DataOutputStream out) throws Exception {
	
	byte[] buffer = new byte[1024] ;
	int bytesRead;

	while ((bytesRead = fin.read(buffer)) != -1 ) {
		out.write(buffer, 0, bytesRead);
	}

	fin.close();
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

