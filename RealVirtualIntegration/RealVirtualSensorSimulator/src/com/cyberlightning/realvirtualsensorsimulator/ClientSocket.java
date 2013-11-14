package com.cyberlightning.realvirtualsensorsimulator;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Observable;




import java.util.concurrent.ConcurrentLinkedQueue;

import android.os.Message;

public class ClientSocket extends Observable implements Runnable, IClientSocket {
	
	private ConcurrentLinkedQueue <Message> outboundBuffer = new ConcurrentLinkedQueue <Message>();
	private DatagramSocket serverSocket;
	private InetAddress serverAddress;
	private SocketSender senderWorker;
	private Thread thread;
	private int port;
	
	
	private volatile boolean isConnected;
	
	public static final int DEFAULT_BUFFER_SIZE = 1024;
	public static final int DEFAULT_INBOUND_SOCKET = 55555;
	public static final int MESSAGE_TYPE_INBOUND = 1;
	public static final int MESSAGE_TYPE_OUTBOUND = 2;
	public static final int MESSAGE_TYPE_UNKNOWNHOST_ERROR= 3;
	
	public static final String SERVER_DEFAULT_ADDRESS = "dev.cyberlightning.com";
	public static final int SERVER_DEFAULT_PORT =61616;
	
	public ClientSocket() {
		this(DEFAULT_INBOUND_SOCKET);
	}
	
	public ClientSocket(int _port) {
		this.port = _port;
		this.thread = new Thread(this);
		this.thread.start();
		
	}
	
	@Override
	public void run() {
		
		try {
			
			this.serverSocket = new DatagramSocket(this.port);
			this.serverSocket.setReceiveBufferSize(DEFAULT_BUFFER_SIZE);
			
			byte[] receiveByte = new byte[DEFAULT_BUFFER_SIZE]; 
			DatagramPacket receivedPacket = new DatagramPacket(receiveByte, receiveByte.length);
			
			this.senderWorker = new SocketSender();
			
			
			while(this.isConnected) {
				serverSocket.receive(receivedPacket);
				handleInboundMessage(receivedPacket);
			}

			
		} catch(IOException e) {
			e.printStackTrace();
		} 	
		return; //Exits thread

	}

    private void handleInboundMessage(DatagramPacket _packet) {
    	
    	String payload = null;
		try {
			payload = new String (_packet.getData(), "utf8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    	MessageObject msg = new MessageObject(_packet.getAddress(), _packet.getPort(), payload);
		setChanged();
		notifyObservers(Message.obtain(null, MESSAGE_TYPE_INBOUND, msg));
	}
    
	public class SocketSender implements Runnable {
		private boolean suspendFlag = true;
		private boolean destroyFlag = false;
		private Thread thread;
		
		public SocketSender() {
			this.thread = new Thread(this);
			this.thread.start();
		}
		
		@Override
		public void run() {
			
			if(!this.resolveServerAddress()) return;
			
			while(true) {
				
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
		            if (destroyFlag) break; 
		        }
				
				if(outboundBuffer.isEmpty()) continue;
				
				Message msg = outboundBuffer.poll();
				String payload = (String)msg.obj;
				byte[] payloadBuffer = null;
				try {
					payloadBuffer = Gzip.compress(payload);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				DatagramPacket packet = new DatagramPacket(payloadBuffer, payloadBuffer.length,serverAddress,SERVER_DEFAULT_PORT);
				try {
					serverSocket.send(packet);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				this.suspendThread();
			}
			
			return;
			
		}
		
		private boolean resolveServerAddress() {
	    	
			try {
				serverAddress = InetAddress.getByName(SERVER_DEFAULT_ADDRESS);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				setChanged();
				notifyObservers(Message.obtain(null, MESSAGE_TYPE_UNKNOWNHOST_ERROR, e.getCause()));
				this.destroy();
				return false;
			}
	    	return true;
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
	}
	
	
	
	@Override
	public void sendMessage(Message _msg) {
		this.outboundBuffer.offer(_msg);
		this.senderWorker.wakeThread();
	}

	@Override
	public void pause() {
		this.senderWorker.suspendThread();
		
	}

	@Override
	public void resume() {
		this.senderWorker.wakeThread();
		
	}

	@Override
	public void end() {
		this.senderWorker.destroy();
		this.isConnected = false;
		
	}
	
	

}
