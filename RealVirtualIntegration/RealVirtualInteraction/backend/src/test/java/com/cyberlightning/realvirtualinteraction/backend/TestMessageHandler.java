package com.cyberlightning.realvirtualinteraction.backend;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.cyberlightning.realvirtualinteraction.backend.services.Gzip;
import com.cyberlightning.realvirtualinteraction.backend.sockets.MessageObject;

public class TestMessageHandler {
	public DatagramPacket testCompressedPacket;
	public DatagramPacket testUnCompressedPacket;
	public final byte[] testClientAddress = {127,0,0,1};
	public final int type = StaticResources.UDP_RECEIVER;
	public final int testClientPort = 61616;
	public final String testUdpSocketUUID = UUID.randomUUID().toString();

	@Before
	public void prepareEmptyCompressedMessageObject() {
		try {
			byte[] byteBuffer = null;
			try {
				byteBuffer = Gzip.compress("");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.testCompressedPacket = new DatagramPacket(byteBuffer, byteBuffer.length,InetAddress.getByAddress(this.testClientAddress), this.testClientPort);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	  @Test 
		public void testCreatingMessageObjectFromCompressedPacket(){
			assertNotNull("MessageObject should not be null", new MessageObject(this.testUdpSocketUUID,this.type,this.testCompressedPacket));
			System.out.println("testCreatingMessageObjectFromCompressedPacket(): assertNotNull(MessageObject should not be null" + this.testUdpSocketUUID+ "," + this.type +"," +this.testCompressedPacket+")");
		}
		
		@Test 
		public void testCreatingMessageObjectFromUnCompressedPacket(){
			assertNotNull("MessageObject should not be null", new MessageObject(this.testUdpSocketUUID,this.type,this.testUnCompressedPacket));
			System.out.println("testCreatingMessageObjectFromCompressedPacket(): assertNotNull(essageObject should not be null" + this.testUdpSocketUUID+ "," + this.type +"," +this.testUnCompressedPacket+")");
			
		}

}
