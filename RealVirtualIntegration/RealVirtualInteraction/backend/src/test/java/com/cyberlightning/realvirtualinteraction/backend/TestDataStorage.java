package com.cyberlightning.realvirtualinteraction.backend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.junit.Before;
import org.junit.Test;

import com.cyberlightning.realvirtualinteraction.backend.services.DataStorageService;
import com.cyberlightning.realvirtualinteraction.backend.services.Gzip;



public class TestDataStorage {
	
	private DatagramPacket testCompressedPacket;
	private DatagramPacket testUnCompressedPacket;
	private final String testJsonString ="{\"d23c0586984d35eff\":{\"d23c058698435eff\":{\"sensors\":[{\"value\":{\"unit\":\"orientation\",\"primitive\":\"3DPoint\",\"time\":\"2013-11-15 14:56\",\"values\":[-0.003545045852661133,0.05859129875898361,-0.5206212997436523]},\"parameters\":{\"interval\":\"ms\",\"toggleable\":\"boolean\"},\"attributes\":{\"type\":\"orientation\",\"power\":1.5,\"vendor\":\"Samsung Inc.\",\"name\":\"Orientation Sensor\"}},{\"value\":{\"unit\":\"rads\",\"primitive\":\"3DPoint\",\"time\":\"2013-11-15 14:56\",\"values\":[9.9683837890625,0.23239292204380035,-1.8811875581741333]},\"parameters\":{\"interval\":\"ms\",\"toggleable\":\"boolean\"},\"attributes\":{\"type\":\"gyroscope\",\"power\":0.5,\"vendor\":\"Invensense\",\"name\":\"MPL Gyro\"}},{\"value\":{\"unit\":\"lx\",\"primitive\":\"double\",\"time\":\"2013-11-15 14:56\",\"values\":357.77637},\"parameters\":{\"interval\":\"ms\",\"toggleable\":\"boolean\"},\"attributes\":{\"type\":\"light\",\"power\":0.75,\"vendor\":\"Capella\",\"name\":\"CM3663 Light sensor\"}},{\"value\":{\"unit\":\"uT\",\"primitive\":\"3DPoint\",\"time\":\"2013-11-15 14:56\",\"values\":[357.81671142578125,0.5156025290489197,-1.8891750574111938]},\"parameters\":{\"interval\":\"ms\",\"toggleable\":\"boolean\"},\"attributes\":{\"type\":\"magneticfield\",\"power\":0.5,\"vendor\":\"Invensense\",\"name\":\"MPL magnetic field\"}},{\"value\":{\"unit\":\"ms2\",\"primitive\":\"3DPoint\",\"time\":\"2013-11-15 14:56\",\"values\":[-0.3239738643169403,-0.09122344106435776,9.800872802734375]},\"parameters\":{\"interval\":\"ms\",\"toggleable\":\"boolean\"},\"attributes\":{\"type\":\"linearacceleration\",\"power\":0.5,\"vendor\":\"Invensense\",\"name\":\"MPL linear accel\"}},{\"value\":{\"unit\":\"ms2\",\"primitive\":\"3DPoint\",\"time\":\"2013-11-15 14:56\",\"values\":[-0.32213011384010315,-0.0398171991109848,9.804611206054688]},\"parameters\":{\"interval\":\"ms\",\"toggleable\":\"boolean\"},\"attributes\":{\"type\":\"accelerometer\",\"power\":0.5,\"vendor\":\"Invensense\",\"name\":\"MPL accel\"}},{\"value\":{\"unit\":\"quaternion\",\"primitive\":\"array\",\"time\":\"2013-11-15 14:56\",\"values\":[357.8206787109375,0.5172339677810669,-1.8906971216201782]},\"parameters\":{\"interval\":\"ms\",\"toggleable\":\"boolean\"},\"attributes\":{\"type\":\"rotationvector\",\"power\":1.5,\"vendor\":\"Google Inc.\",\"name\":\"Rotation Vector Sensor\"}},{\"value\":{\"unit\":\"ms2\",\"primitive\":\"3DPoint\",\"time\":\"2013-11-15 14:56\",\"values\":[-0.0030828863382339478,-8.415747433900833E-4,0.003661018330603838]},\"parameters\":{\"interval\":\"ms\",\"toggleable\":\"boolean\"},\"attributes\":{\"type\":\"gravity\",\"power\":0.5,\"vendor\":\"Invensense\",\"name\":\"MPL gravity\"}}],\"attributes\":{\"gps\":[65.5,25.3],\"name\":\"Android device\"}}}}";
	private final byte[] testClientAddress = {127,0,0,1};
	private final int testClientPort = 61616;
	
	@Before
	public void prepareCompressedMessageObject() {
		try {
			byte[] byteBuffer = Gzip.compress(this.testJsonString);
			this.testCompressedPacket = new DatagramPacket(byteBuffer, byteBuffer.length,InetAddress.getByAddress(this.testClientAddress), this.testClientPort);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Before
	public void prepareUnCompressedMessageObject() {
		try {
			byte[] byteBuffer = this.testJsonString.getBytes();
			this.testUnCompressedPacket = new DatagramPacket(byteBuffer, byteBuffer.length,InetAddress.getByAddress(this.testClientAddress), this.testClientPort);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
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
	public void testDataStorageServiceSaveDatagramPacketWithCompressedPayload() {
		
		boolean isSuccess = true;
		this.prepareCompressedMessageObject();
		
		try {
			DataStorageService.getInstance().addEntry(this.testCompressedPacket);
		} catch (IOException e) {
			isSuccess = false;
		}
		assertEquals("DataStorageServiceError not being able to store a testCompressedPacket: ",true, isSuccess );
		System.out.println(" testDataStorageServiceSaveDatagramPacketWithCompressedPayload(): assertEquals(\"DataStorageServiceError not being able to store a Compressed Packet: ,true, isSuccess )");
		
		
	}
	
	@Test
	public void testDataStorageServiceSaveDatagramPacketWithUnCompressedPayload() {
		
		boolean isSuccess = true;
		this.prepareUnCompressedMessageObject();
		
		try {
			DataStorageService.getInstance().addEntry(this.testUnCompressedPacket);
		} catch (IOException e) {
			isSuccess = false;
		}
		assertEquals("DataStorageServiceError not being able to store a UncompressedCompressedPacket: ",true, isSuccess );
		System.out.println(" testDataStorageServiceSaveDatagramPacketWithUnCompressedPayload(): assertEquals(\"DataStorageServiceError not being able to store a Uncompressed Packet: ,true, isSuccess )");

	}
	
	@Test 
	public void testAddNewEntityToDataBaseAndSaveFileToHardDrive() {
	     
		boolean isSaved = false;
		try {
			DataStorageService.getInstance().addEntry(this.testUnCompressedPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	
		}
        DataStorageService.getInstance().saveData(DataStorageService.getInstance().entityTable,StaticResources.DATABASE_FILE_NAME);
        isSaved = true;
        assertTrue("Saving new entity successfully completed on hard drive: Expected : true, Actual: true",isSaved);
	}
	
	
}
