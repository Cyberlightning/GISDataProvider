
package com.cyberlightning.webserver.sockets;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.cyberlightning.webserver.StaticResources;

public class WebSocket implements Runnable  {
	

	private ServerSocket tcpSocket;
	private int port;

	
	public WebSocket () {
		this(StaticResources.WEB_SOCKET_PORT);
	}
	
	/**
	 * 
	 * @param _port
	 */
	public WebSocket(int _port) {
		this.port = _port;
		this.intialize();
	}
	
	private void intialize() {
		
		try {
			this.tcpSocket = new ServerSocket (this.port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {

		while(true) {
			try {
				Socket newClient = this.tcpSocket.accept();
				Thread worker = new Thread((Runnable)(new WebSocketWorker(newClient)));
				worker.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
       }
	}
}
