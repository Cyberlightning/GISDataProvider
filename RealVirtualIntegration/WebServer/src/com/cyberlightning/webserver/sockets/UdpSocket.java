package com.cyberlightning.webserver.sockets;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.cyberlightning.webserver.StaticResources;
import com.cyberlightning.webserver.interfaces.IMessageEvent;
import com.cyberlightning.webserver.services.MessageService;

public class UdpSocket implements Runnable  {
	
	private DatagramSocket serverSocket;
	private int port;
	public static final String uuid = UUID.randomUUID().toString();
	public  final int type = StaticResources.UDP_RECEIVER;
	private SendWorker sendWorker;
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
			//Thread t = new Thread((Runnable)(new TestRoutine()));
			
			//t.start();
        	byte[] receivedData = new byte[StaticResources.UDP_PACKET_SIZE];
    		DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
        	
    		try {
        		
				serverSocket.receive(receivedPacket);
				System.out.println("Basestation packet received from " + new String(receivedPacket.getData(),"utf8"));
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		MessageService.getInstance().addToMessageBuffer(new MessageObject(uuid,StaticResources.UDP_RECEIVER, receivedPacket));
			MessageService.getInstance().wakeThread();
    		
		}
	}
	
	private class TestRoutine implements Runnable {

		@Override
		public void run() {
			int j = 0;
			while (j<50) {
			double ds = Math.random()*10;
			double ds2;
			double ds3;
			String uid = UUID.randomUUID().toString();
			Random fff = new Random();
			if (fff.nextBoolean() == true) {
				ds2 = -Math.random()*10;
				ds3 = Math.random()*10;
			} else {
				ds2 = Math.random()*10;
				ds3 = -Math.random()*10;
			}
			
			
				try {
					Thread.sleep(4000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				String times = StaticResources.getTimeStamp();
				
				//String s = "{\"5de5f289-c7f3-4994-8dfb-3639d3f7c8d0\": {\""+uid+"\": {\"attributes\": { \"name\": \"Texas CL2541 Sensor\",\"gps\": ["+(65.03+ds)+","+(ds+25.28)+"]},\"sensors\": [{\"value\": {\"unit\": \"m/s2\",\"primitive\": \"3DPoint\", \"time\": \""+times+"\",\"values\": [ "+ds2+",-2.15625,"+ds3+"]},\"uuid\": \"f000aa10-0451-4000-b000-000000000000\",\"parameters\": {\"toggleable\": \"true\",\"options\": \"boolean\"},\"attributes\": {\"type\": \"accelerometer\",\"vendor\": \"Texas Instruments\"}},{\"value\": { \"unit\": \"Celsius\",\"primitive\": \"double\",\"time\": \""+times+"\",\"values\": "+(27.21875 + ds2)+"},\"uuid\": \"f000aa00-0451-4000-b000-000000000000\",\"parameters\": {\"toggleable\": \"true\",\"options\": \"boolean\"},\"attributes\": {\"type\": \"temperature\",\"vendor\": \"Texas Instruments\"}}]}}}";
				//String s = "{\"5de5f289-c7f3-4994-8dfb-3639d3f7c8d0\": {\"SENSOR2\": {\"attributes\": { \"name\": \"Texas CL2541 Sensor\",\"gps\": ["+(65.03+ds)+","+(ds+25.28)+"]},\"sensors\": [{\"value\": {\"unit\": \"m/s2\",\"primitive\": \"3DPoint\", \"time\": \""+times+"\",\"values\": [ "+ds2+",-2.15625,"+ds3+"]},\"uuid\": \"f000aa10-0451-4000-b000-000000000000\",\"parameters\": {\"toggleable\": \"true\",\"options\": \"boolean\"},\"attributes\": {\"type\": \"accelerometer\",\"vendor\": \"Texas Instruments\"}},{\"value\": { \"unit\": \"Celsius\",\"primitive\": \"double\",\"time\": \""+times+"\",\"values\": "+(27.21875 + ds2)+"},\"uuid\": \"f000aa00-0451-4000-b000-000000000000\",\"parameters\": {\"toggleable\": \"true\",\"options\": \"boolean\"},\"attributes\": {\"type\": \"temperature\",\"vendor\": \"Texas Instruments\"}}]}}}";
				//String dds = "{\"5de5f289-c7f3-4994-8dfb-3639d3f7c8d0\": {\"5de5f200-c7f3-4994-8dfb-3639d3f7c8d0\": {\"attributes\": { \"name\": \"Texas CL2541 Sensor\",\"gps\": [65.061049,25.4680896]},\"sensors\": [{\"value\": {\"unit\": \"m/s2\",\"primitive\": \"3DPoint\", \"time\": \""+times+"\",\"values\": [ "+ds2+",-2.15625,"+ds3+"]},\"uuid\": \"f000aa10-0451-4000-b000-000000000000\",\"parameters\": {\"toggleable\": \"true\",\"options\": \"boolean\"},\"attributes\": {\"type\": \"accelerometer\",\"vendor\": \"Texas Instruments\"}},{\"value\": { \"unit\": \"Celsius\",\"primitive\": \"double\",\"time\": \""+times+"\",\"values\": "+(27.21875 + ds2)+"},\"uuid\": \"f000aa00-0451-4000-b000-000000000000\",\"parameters\": {\"toggleable\": \"true\",\"options\": \"boolean\"},\"attributes\": {\"type\": \"temperature\",\"vendor\": \"Texas Instruments\"}}]}}}";
				//String dds = "{\"d23c058698435eff\":{\"d23c058698435eff\":{\"sensors\":[{\"value\":{\"primitive\":\"3DPoint\",\"time\":\"2749-05-17 16:50\",\"values\":[2.0045621395111084,-0.19144247472286224,-1.7531667947769165]},\"parameters\":{\"interval\":\"ms\",\"toggleable\":\"boolean\"},\"attributes\":{\"type\":\"rotation\",\"vendor\":\"Google Inc.\",\"name\":\"Rotation Vector Sensor\"}}]}}}";
				//String dds = "{\"d23c058698435eff\":{\"d23c058698435eff\":{\"sensors\":[{\"value\":{\"unit\":\"μT\",\"primitive\":\"3DPoint\",\"time\":\"2777-03-12 15:19\",\"values\":[0.017108917236328125,-0.0018523354083299637,-0.5829010009765625]},\"parameters\":{\"interval\":\"ms\",\"toggleable\":\"boolean\"},\"attributes\":{\"type\":\"magneticfield\",\"power\":0.5,\"vendor\":\"Invensense\",\"name\":\"MPL magnetic field\"}}]}}}";
				String dds = "{\"d23c058698435eff\":{\"d23c058698435eff\":{\"sensors\":[{\"value\":{\"unit\":\"lx\",\"primitive\":\"3DPoint\",\"time\":\"2869-02-12 08:39\",\"values\":[25,0,0]},\"parameters\":{\"interval\":\"ms\",\"toggleable\":\"boolean\"},\"attributes\":{\"type\":\"light\",\"power\":0.75,\"vendor\":\"Capella\",\"name\":\"CM3663 Light sensor\"}}],\"attributes\":{\"gps\":[65.5,25.3],\"name\":\"Android device\"}}}}";
				byte[]  b = dds.getBytes();
			

				DatagramPacket d = null;
				byte[] address = {22,22,22,22};
				try {
					d = new DatagramPacket(b, b.length,InetAddress.getByAddress(address), 23233);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				MessageService.getInstance().addToMessageBuffer(new MessageObject(uuid,type,d));
				MessageService.getInstance().wakeThread();
			}	
			return;
		}
		
	}
	
	private class SendWorker implements Runnable,IMessageEvent {
		
		public List<MessageObject> sendBuffer = Collections.synchronizedList(new ArrayList<MessageObject>());
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
				     					DatagramPacket packet = new DatagramPacket(b,b.length,target.getAddress(),target.getPort());
				     					serverSocket.send(packet);
				     				}
				     			}
				     			sendBuffer.remove(msg);
				     		}
				     		this.suspendThread();

		   				} catch (IOException e) {
		   					// TODO Auto-generated catch block
		   					e.printStackTrace();
		   					break;
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
