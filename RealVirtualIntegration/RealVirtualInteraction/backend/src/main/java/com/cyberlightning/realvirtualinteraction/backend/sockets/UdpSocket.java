package com.cyberlightning.realvirtualinteraction.backend.sockets;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import com.cyberlightning.realvirtualinteraction.backend.StaticResources;
import com.cyberlightning.realvirtualinteraction.backend.interfaces.IMessageEvent;
import com.cyberlightning.realvirtualinteraction.backend.services.MessageService;



public class UdpSocket implements Runnable  {
	
	public DatagramSocket serverSocket;
	private int port;
	public static final String uuid = UUID.randomUUID().toString();
	public  final int type = StaticResources.UDP_RECEIVER;
	public SendWorker sendWorker;
	/**
	 * 
	 */
	public UdpSocket () {
		this(StaticResources.SERVER_PORT_COAP);
	}
	
	/**
	 * 
	 * @param _port
	 */
	public UdpSocket (int _port) {
		this.port = _port;
		Thread t = new Thread(this);
		t.start();
	}
	
	@Override
	public void run() {
		
		try {
			
		serverSocket = new DatagramSocket(this.port);
		this.serverSocket.setReceiveBufferSize(StaticResources.UDP_PACKET_SIZE);
		this.sendWorker = new SendWorker(uuid);
		
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.sendWorker.destroy();
		}
		
		while(true) {
        	
			if (!MessageService.isStarted()) continue;
			
			
			
        	byte[] receivedData = new byte[StaticResources.UDP_PACKET_SIZE];
    		DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
        	
    		try {
        		serverSocket.receive(receivedPacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		this.handleIncomingMessage(receivedPacket);
    		
		}
	}
	
	public void handleIncomingMessage(DatagramPacket _packet) {
		MessageService.getInstance().addToMessageBuffer(new MessageObject(uuid,StaticResources.UDP_RECEIVER, _packet));
		MessageService.getInstance().wakeThread();
	}

	
	public class SendWorker implements Runnable,IMessageEvent {
		
		public CopyOnWriteArrayList<MessageObject> sendBuffer = new CopyOnWriteArrayList<MessageObject>();
		public String uuid;
		private boolean suspendFlag = true;
		private boolean destroyFlag = false;
		private Thread thread;
		/**
		 * 
		 * @param _uuid
		 */
		public SendWorker (String _uuid) {
			this.uuid = _uuid;
			this.thread = new Thread(this);
			this.thread.start();
		}
		
		@Override
		public void run() {
			
			MessageService.getInstance().registerReceiver(this,this.uuid);
			
			while (true) {
				 
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
				if (!sendBuffer.isEmpty()) {
		        	   
		        	   try {
		        		
		        		   Iterator<MessageObject> i = this.sendBuffer.iterator();
				     		while(i.hasNext()) {
				     			
				     			MessageObject msg = i.next();
				     			
				     			if (msg.payload instanceof DatagramPacket) {
				     				serverSocket.send((DatagramPacket)msg.payload);
				     			} else if (msg.payload instanceof String) {
				     				
				     				byte[] b = ((String) msg.payload).getBytes();
				     				
				     				for (InetSocketAddress target : msg.targetAddresses) {
				     					DatagramPacket packet = new DatagramPacket(b,b.length,InetAddress.getByAddress(target.getAddress().getAddress()),target.getPort());
				     					serverSocket.send(packet);
				     				}
				     				
				     			}
				     			MessageObject response;
				     			if (msg.targetAddresses.size() == 0) {
				     				response = new MessageObject(msg.originUUID,StaticResources.UDP_RESPONSE,StaticResources.ERROR_CODE_NOT_FOUND);
					     			
				     			} else {
				     				response = new MessageObject(msg.originUUID,StaticResources.UDP_RESPONSE,StaticResources.HTTP_CODE_OK);
				     			}
				     			MessageService.getInstance().addToMessageBuffer(response);
				     			MessageService.getInstance().wakeThread();
				     			sendBuffer.remove(msg);
				     		}
				     		this.suspendThread();

		   				} catch (IOException e) {
		   					System.out.println(e.getMessage());
		   					if (serverSocket.isClosed()) break;
		   				} 
		       }
			}
			return; //Exit thread
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
		
		@Override
		public void onMessageReceived(MessageObject _msg) {
			this.sendBuffer.add(_msg);
			this.wakeThread();
		}
		
		
	}

}
