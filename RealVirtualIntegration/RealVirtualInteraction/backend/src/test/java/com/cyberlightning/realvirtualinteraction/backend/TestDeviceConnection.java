package com.cyberlightning.realvirtualinteraction.backend;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.UUID;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.cyberlightning.realvirtualinteraction.backend.interfaces.IMessageEvent;
import com.cyberlightning.realvirtualinteraction.backend.services.MessageService;
import com.cyberlightning.realvirtualinteraction.backend.sockets.MessageObject;
import com.cyberlightning.realvirtualinteraction.backend.sockets.UdpSocket;

public class TestDeviceConnection {
	  
	
	  
	  @Mock
	  UdpSocket udpSocket = new UdpSocket();
	  
	  @Mock
	  Thread server = new Thread((Runnable) udpSocket);
	  
	  @Mock
	  Thread simulator = new Thread((Runnable) new TestRoutine());

	  @Before
	  public void setUp() throws Exception {
	    MockitoAnnotations.initMocks(this);
		MessageService.getInstance().startThread(); 
		String uuid = UUID.randomUUID().toString();
		//MessageService.getInstance().registerReceiver(this,uuid);
		ArrayList<String> devices = new ArrayList<String> ();
		devices.add(UdpSocket.uuid);
		MessageService.getInstance().subscribeByIds(devices, uuid);
	    server.start();
	  }

//	  @org.junit.Test
//	  public void testDeviceConnectionIsEstablished()  {
//		Mockito.verify(server).start();
//		boolean isStarted = udpSocket.serverSocket.isClosed();
//	    assertFalse(isStarted);
//	  }
//	  
//	  @org.junit.Test
//	  public void testDeviceConnectionIsReceivingData()  {
//		DatagramSocket socket = null;
//		try {
//			socket = new DatagramSocket(StaticResources.SERVER_PORT_COAP+ 1);
//		} catch (SocketException e2) {
//			// TODO Auto-generated catch block
//			e2.printStackTrace();
//		}
//		String sample = "test message";
//		byte[] byteBuffer = sample.getBytes();
//		DatagramPacket testPacket = null;
//		
//		try {
//			testPacket = new DatagramPacket(byteBuffer, byteBuffer.length,InetAddress.getLocalHost(),StaticResources.SERVER_PORT_COAP);
//			
//		} catch (UnknownHostException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//			System.out.println("Test Packet sending failed: " + e1.getMessage());
//		}
//		
//		try {
//			socket.send(testPacket);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			System.out.println("Test Packet sending failed: " + e.getMessage());
//		}
//		System.out.println("Test Packet send to " + testPacket.getAddress().getHostAddress() + ":" + testPacket.getPort() );
//		Mockito.verify(udpSocket).handleIncomingMessage(testPacket);
//		
//	   
//	  }
	  
	  

//	@Override
//	public void onMessageReceived(MessageObject _msg) {
//		System.out.println("Message Received: " + _msg.payload.toString() ); 
//		assertTrue(true);
//		
//	}
}
