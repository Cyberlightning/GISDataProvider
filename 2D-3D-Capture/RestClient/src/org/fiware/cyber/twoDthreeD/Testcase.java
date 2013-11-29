package org.fiware.cyber.twoDthreeD;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Ignore;
import org.junit.Test;

public class Testcase {

	@Test
	@Ignore
	public void test() {
		fail("Not yet implemented");
	}
	
    ImageTaggingServlet servlet = new ImageTaggingServlet();
    
    
//  @Test
//	public void testTagImage() {
//		String keyValue = servlet.tagImage("image_2013.11.12_13.39.42",".png");
//		assertEquals(keyValue,"Test Text");
//	}

	@Ignore
	@Test
	public void testReadImage(){
		File f = new File("/home/tharanga/git/CyberWexdemo1/2D-3D-Capture/public_html/images/image_2013.11.12_13.18.52.png");
		BufferedImage in = servlet.readImage(f);
		assertEquals(BufferedImage.TYPE_4BYTE_ABGR,in.getType());
	}
	
	@Test
	public void TestServlet() throws Exception{
		HttpServletRequest request = mock(HttpServletRequest.class);       
	    HttpServletResponse response = mock(HttpServletResponse.class); 
		when(request.getParameter("data")).thenReturn("{\"type\":\"image\",\"time\":\"2013.11.13_13.39.57\",\"ext\":\"png\",\"devicetype\":\"Android\",\"browsertype\":\"Firefox\",\"position\" : {\"lon\":25.4738811,\"lat\":65.01239927,\"alt\":0,\"acc\":37},\"motion\":{\"heading\":0,\"speed\":0},\"device\":{\"ax\":0,\"ay\":-0.39,\"az\":-0.49,\"gx\":0,\"gy\":5.02,\"gz\":7.71,\"ra\":191.2802,\"rb\":-33.4154,\"rg\":0.776,\"orientation\":\"potrait\"},\"dTime\":1384342396022,\"vwidth\":480,\"vheight\":800}\"");
	      PrintWriter writer = new PrintWriter("somefile.txt");
	      when(response.getWriter()).thenReturn(writer);	      
	      servlet.doPost(request, response);
	      writer.flush();
	      
	}

}
