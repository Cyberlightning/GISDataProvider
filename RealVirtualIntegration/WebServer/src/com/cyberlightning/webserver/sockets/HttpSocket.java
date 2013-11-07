package com.cyberlightning.webserver.sockets;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.cyberlightning.webserver.StaticResources;

public class HttpSocket implements Runnable {


	private ServerSocket serverSocket;
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
			this.serverSocket = new ServerSocket (this.port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void run() {
	
		while(true) {
	
				try {
					Socket newClient = this.serverSocket.accept();
					Thread worker = new Thread((Runnable)(new HttpSocketWorker(newClient)));
					worker.start();
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
		}
	}

}

