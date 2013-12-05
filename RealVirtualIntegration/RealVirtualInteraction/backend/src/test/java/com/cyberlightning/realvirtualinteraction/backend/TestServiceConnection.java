package com.cyberlightning.realvirtualinteraction.backend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

import org.junit.Before;
import org.junit.Test;

import com.cyberlightning.realvirtualinteraction.backend.entities.SpatialQuery;
import com.cyberlightning.realvirtualinteraction.backend.services.DataStorageService;
import com.cyberlightning.realvirtualinteraction.backend.services.Gzip;


public class TestServiceConnection {
	
	private DatagramPacket testCompressedPacket;
	private final String testJsonString ="{\"d23c0586984d35eff\":{\"d23c058698435eff\":{\"sensors\":[{\"value\":{\"unit\":\"orientation\",\"primitive\":\"3DPoint\",\"time\":\"2013-11-15 14:56\",\"values\":[-0.003545045852661133,0.05859129875898361,-0.5206212997436523]},\"parameters\":{\"interval\":\"ms\",\"toggleable\":\"boolean\"},\"attributes\":{\"type\":\"orientation\",\"power\":1.5,\"vendor\":\"Samsung Inc.\",\"name\":\"Orientation Sensor\"}},{\"value\":{\"unit\":\"rads\",\"primitive\":\"3DPoint\",\"time\":\"2013-11-15 14:56\",\"values\":[9.9683837890625,0.23239292204380035,-1.8811875581741333]},\"parameters\":{\"interval\":\"ms\",\"toggleable\":\"boolean\"},\"attributes\":{\"type\":\"gyroscope\",\"power\":0.5,\"vendor\":\"Invensense\",\"name\":\"MPL Gyro\"}},{\"value\":{\"unit\":\"lx\",\"primitive\":\"double\",\"time\":\"2013-11-15 14:56\",\"values\":357.77637},\"parameters\":{\"interval\":\"ms\",\"toggleable\":\"boolean\"},\"attributes\":{\"type\":\"light\",\"power\":0.75,\"vendor\":\"Capella\",\"name\":\"CM3663 Light sensor\"}},{\"value\":{\"unit\":\"uT\",\"primitive\":\"3DPoint\",\"time\":\"2013-11-15 14:56\",\"values\":[357.81671142578125,0.5156025290489197,-1.8891750574111938]},\"parameters\":{\"interval\":\"ms\",\"toggleable\":\"boolean\"},\"attributes\":{\"type\":\"magneticfield\",\"power\":0.5,\"vendor\":\"Invensense\",\"name\":\"MPL magnetic field\"}},{\"value\":{\"unit\":\"ms2\",\"primitive\":\"3DPoint\",\"time\":\"2013-11-15 14:56\",\"values\":[-0.3239738643169403,-0.09122344106435776,9.800872802734375]},\"parameters\":{\"interval\":\"ms\",\"toggleable\":\"boolean\"},\"attributes\":{\"type\":\"linearacceleration\",\"power\":0.5,\"vendor\":\"Invensense\",\"name\":\"MPL linear accel\"}},{\"value\":{\"unit\":\"ms2\",\"primitive\":\"3DPoint\",\"time\":\"2013-11-15 14:56\",\"values\":[-0.32213011384010315,-0.0398171991109848,9.804611206054688]},\"parameters\":{\"interval\":\"ms\",\"toggleable\":\"boolean\"},\"attributes\":{\"type\":\"accelerometer\",\"power\":0.5,\"vendor\":\"Invensense\",\"name\":\"MPL accel\"}},{\"value\":{\"unit\":\"quaternion\",\"primitive\":\"array\",\"time\":\"2013-11-15 14:56\",\"values\":[357.8206787109375,0.5172339677810669,-1.8906971216201782]},\"parameters\":{\"interval\":\"ms\",\"toggleable\":\"boolean\"},\"attributes\":{\"type\":\"rotationvector\",\"power\":1.5,\"vendor\":\"Google Inc.\",\"name\":\"Rotation Vector Sensor\"}},{\"value\":{\"unit\":\"ms2\",\"primitive\":\"3DPoint\",\"time\":\"2013-11-15 14:56\",\"values\":[-0.0030828863382339478,-8.415747433900833E-4,0.003661018330603838]},\"parameters\":{\"interval\":\"ms\",\"toggleable\":\"boolean\"},\"attributes\":{\"type\":\"gravity\",\"power\":0.5,\"vendor\":\"Invensense\",\"name\":\"MPL gravity\"}}],\"attributes\":{\"gps\":[65.5,25.3],\"name\":\"Android device\"}}}}";
	private final byte[] testClientAddress = {127,0,0,1};
	private final int testClientPort = 61616;
	
	@Before
	public void setUp() {
	
		try {
			byte[] byteBuffer = Gzip.compress(this.testJsonString);
			this.testCompressedPacket = new DatagramPacket(byteBuffer, byteBuffer.length,InetAddress.getByAddress(this.testClientAddress), this.testClientPort);
			DataStorageService.getInstance().addEntry(this.testCompressedPacket);
		} catch (IOException e) {
				// TODO Auto-generated catch block
			e.printStackTrace();
		
		}
		DataStorageService.getInstance().saveData(DataStorageService.getInstance().entityTable,StaticResources.DATABASE_FILE_NAME);
	       
		
	}
	
	@Test
	public void testGetEntryByUUID () {
		String test = DataStorageService.getInstance().getEntryById("d23c058698435eff", 1);
		assertNotNull(test);
		System.out.println("testGetEntryByUUID(): assertNotNull(DataStorageService.getInstance().getEntryById(\"d23c058698435eff\", 1))");
		System.out.println("Expected not null: Actual result:" + test.length());
	}
	  
	@Test
	public void testGetEntrBySpatialQueryWithExpectedCoordinates() {
		String test = DataStorageService.getInstance().getEntriesByParameter(new SpatialQuery(Float.parseFloat("65.5"),Float.parseFloat("25.3"),1600,1));
		assertNotNull(test);
		System.out.println("testGetEntrBySpatialQueryWithExpectedCoordinates(): assertNotNull(DataStorageService.getInstance().getEntriesByParameter(new SpatialQuery(Float.parseFloat(\"65.5\"),Float.parseFloat(\"25.3\"),1600,1)))");
		System.out.println("Expected not null: Actual result:" + test.length());
	}
	
	@Test
	public void testGetEntrBySpatialQueryWithUnExpectedCoordinates() {
		String test = DataStorageService.getInstance().getEntriesByParameter(new SpatialQuery(Float.parseFloat("90.5"),Float.parseFloat("10.3"),1600,1));
		assertEquals("Expecting error code not found with wront GPS coordinates : ", StaticResources.ERROR_CODE_NOT_FOUND, test);
		System.out.println(" testGetEntrBySpatialQueryWithUnExpectedCoordinates(): assertEuals(\"Expecting error code not found with wront GPS coordinates : \", StaticResources.ERROR_CODE_NOT_FOUND, DataStorageService.getInstance().getEntriesByParameter(new SpatialQuery(Float.parseFloat(\"90.5\"),Float.parseFloat(\"10.3\"),1600,1)))");
		System.out.println("Expected boolean are equal strings in comparison: Actual result: true");
	}
	
	
	  

}
