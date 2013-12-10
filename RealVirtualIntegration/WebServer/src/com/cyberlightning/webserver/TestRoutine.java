package com.cyberlightning.webserver;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


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
			String sample = "{\"440cd2d8c18d7d3a\":{\"440cd2d8c18d7d3a\":{\"sensors\":[{\"value\":{\"unit\":\"rads\",\"primitive\":\"3DPoint\",\"time\":\"2013-12-10 10:07:30\",\"values\":[21.117462158203125,-0.9801873564720154,-0.6045787930488586]},\"configuration\":[{\"interval\":\"ms\",\"toggleable\":\"boolean\"}],\"attributes\":{\"type\":\"gyroscope\",\"power\":0.5,\"vendor\":\"Invensense\",\"name\":\"MPL Gyro\"}},{\"value\":{\"unit\":\"ms2\",\"primitive\":\"3DPoint\",\"time\":\"2013-12-10 10:07:30\",\"values\":[149.10000610351563,420.20001220703125,-1463.9000244140625]},\"configuration\":[{\"interval\":\"ms\",\"toggleable\":\"boolean\"}],\"attributes\":{\"type\":\"accelerometer\",\"power\":0.5,\"vendor\":\"Invensense\",\"name\":\"MPL accel\"}},{\"value\":{\"unit\":\"uT\",\"primitive\":\"3DPoint\",\"time\":\"2013-12-10 10:07:30\",\"values\":[-0.08577163517475128,0.16211289167404175,9.922416687011719]},\"configuration\":[{\"interval\":\"ms\",\"toggleable\":\"boolean\"}],\"attributes\":{\"type\":\"magneticfield\",\"power\":0.5,\"vendor\":\"Invensense\",\"name\":\"MPL magnetic field\"}},{\"value\":{\"unit\":\"orientation\",\"primitive\":\"3DPoint\",\"time\":\"2013-12-10 10:07:30\",\"values\":[-0.004261057823896408,-0.017044231295585632,0.019174760207533836]},\"configuration\":[{\"interval\":\"ms\",\"toggleable\":\"boolean\"}],\"attributes\":{\"type\":\"orientation\",\"power\":9.699999809265137,\"vendor\":\"Invensense\",\"name\":\"MPL Orientation (android deprecated format)\"}}],\"actuators\":[{\"configuration\":[{\"value\":\"100\",\"unit\":\"percent\",\"name\":\"viewsize\"}],\"actions\":[{\"value\":\"[marker1,marker2,marker3,marker4,marker6,marker7,marker8,marker9,marker10,marker11,marker12,marker13,marker14,marker15,marker15,marker16,marker17,marker18,marker19]\",\"primitive\":\"array\",\"unit\":\"string\",\"parameter\":\"viewstate\"}],\"callbacks\":[{\"target\":\"viewstate\",\"return_type\":\"boolean\"}],\"attributes\":{\"dimensions\":\"[480,800]\"}}]},\"attributes\":{\"name\":\"Android device\"}}}";
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