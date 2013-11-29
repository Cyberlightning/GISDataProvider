package com.cyberlightning.webserver;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.cyberlightning.webserver.services.Gzip;

public class TestRoutine implements Runnable {

	
	private DatagramSocket testSocket;
	@Override
	public void run() {
		
		try {
			this.testSocket = new DatagramSocket(StaticResources.SERVER_PORT_COAP+ 1);
			this.testSocket.setReceiveBufferSize(StaticResources.UDP_PACKET_SIZE);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Unable to create test socket: " + e.getMessage());
			return;
		}
		
		Runnable receiveRoutine = new TestReceiver();
		Thread t = new Thread(receiveRoutine);
		t.start();
		
		while (true) {
		
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			
			}
			String sample = "{\"d23c0586984d35eff\":{\"d23c058698435eff\":{\"sensors\":[{\"value\":{\"unit\":\"lx\",\"primitive\":\"double\",\"time\":\"2013-11-15 14:56\",\"values\":357.77637},\"parameters\":{\"interval\":\"ms\",\"toggleable\":\"boolean\"},\"attributes\":{\"type\":\"light\",\"power\":0.75,\"vendor\":\"Capella\",\"name\":\"CM3663 Light sensor\"}},{\"value\":{\"unit\":\"quaternion\",\"primitive\":\"array\",\"time\":\"2013-11-15 14:56\",\"values\":[357.8206787109375,0.5172339677810669,-1.8906971216201782]},\"parameters\":{\"interval\":\"ms\",\"toggleable\":\"boolean\"},\"attributes\":{\"type\":\"rotationvector\",\"power\":1.5,\"vendor\":\"Google Inc.\",\"name\":\"Rotation Vector Sensor\"}},{\"value\":{\"unit\":\"m/s2\",\"primitive\":\"3DPoint\",\"time\":\"2013-11-15 14:56\",\"values\":[-0.0030828863382339478,-8.415747433900833E-4,0.003661018330603838]},\"parameters\":{\"interval\":\"ms\",\"toggleable\":\"boolean\"},\"attributes\":{\"type\":\"gravity\",\"power\":0.5,\"vendor\":\"Invensense\",\"name\":\"MPL gravity\"}}],\"attributes\":{\"gps\":[65.5,25.3],\"name\":\"Android device\"}}}}";
			byte[] byteBuffer = sample.getBytes();
			
			DatagramPacket testPacket = null;
			
			try {
				testPacket = new DatagramPacket(byteBuffer, byteBuffer.length,InetAddress.getLocalHost(),StaticResources.SERVER_PORT_COAP);
				
			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.out.println("Test Packet sending failed: " + e1.getMessage());
			}
			
			try {
				this.testSocket.send(testPacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("Test Packet sending failed: " + e.getMessage());
			}
			System.out.println("Test Packet send to " + testPacket.getAddress().getHostAddress() + ":" + testPacket.getPort() );
		}
	}
	
	public class TestReceiver implements Runnable {
		
		
		@Override
		public void run() {
				while (true) {
				
			
				
				byte[] receivedData = new byte[StaticResources.UDP_PACKET_SIZE];
	    		DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
	        	
	    		try {
	        		testSocket.receive(receivedPacket);
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("Error in receiving a packet:" + e.getMessage());
					break;
				}
	    		String payload = "";
	    		try {
					payload  = new String(receivedPacket.getData(),"utf8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("Error unpacking packet received from server:" + e.getMessage()); 
				}
	    		System.out.println("Packet received from server:" + payload);
			}
			return;
		}
	
	}
	
}