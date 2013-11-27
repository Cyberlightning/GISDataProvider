package com.cyberlightning.webserver.tests;

import static org.junit.Assert.*;

import java.net.Socket;

import org.junit.Before;
import org.junit.Test;

import com.cyberlightning.webserver.sockets.HttpSocketWorker;

public class HttpTester {

	public HttpSocketWorker testWorker;
	public Socket testSocket;
	
	@Before
	public void setUp() {
		this.testSocket = new Socket();
		this.testWorker = new HttpSocketWorker(testSocket);
	}
	
	@Test
	public void testHandlePOSTMethod() {
		
		String query = "";
		boolean isFile = false;
		// check if multiply(10,5) returns 50
	   //assertEquals("10 x 5 must be 50", 50, this.testWorker.handlePOSTMethod(query, isFile));
	} 
}
