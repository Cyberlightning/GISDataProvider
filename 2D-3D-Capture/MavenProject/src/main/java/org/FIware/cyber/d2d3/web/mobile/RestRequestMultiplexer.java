package org.FIware.cyber.d2d3.web.mobile;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class RestRequestMultiplexer
 */
@WebServlet("/RestRequestMultiplexer")
public class RestRequestMultiplexer extends HttpServlet {
	private static final long serialVersionUID = 1L;
//    private static final String serverPORT = "17322";  
    private final String USER_AGENT = "Mozilla/5.0";
    private static final String serverURL = "dev.cyberlightning.com";
//    private static final String serverPORT = "17323";
//    private static final String serverURL = "localhost";
    private static final String serverPORT = "17321"; 
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RestRequestMultiplexer() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		String  data = request.getParameter("command");
		String server = request.getParameter("server");
		server = server.trim();
		data = data.trim();
		System.out.println(data);
		if(data.equals("getAll")){
	        try {
	            URL getPhotoList = new URL("http://"+serverURL+":"+serverPORT+"/getAllImageData");
	            BufferedReader in = new BufferedReader( new InputStreamReader(getPhotoList.openStream()));	
	            String outString="";
	            String inputLine;
	            while (( inputLine = in.readLine()) != null)
	            	outString = outString+inputLine;
	            in.close();
	            out.println(outString);
	        } 
	        catch (MalformedURLException e) {
	        }
		} else if (data.equals("getLocationBasedData")){
			try {
				String  lng = request.getParameter("Longitude");
				String  lat = request.getParameter("Latitude");
				float flng = Float.parseFloat(lng);
				float flat = Float.parseFloat(lat);
				String payload = "{\"lon\":"+lng.trim()+",\"lat\":"+lat.trim()+"}";
	            URL getPhotoList = new URL("http://"+serverURL+":"+serverPORT+"/getLocationImageData?lat="+flat+"&lon="+flng+"&json="+payload);	           
	            BufferedReader in = new BufferedReader( new InputStreamReader(getPhotoList.openStream()));
	            System.out.println(data);
	            String outString="";
	            String inputLine;
	            while (( inputLine = in.readLine()) != null)
	            	outString = outString+inputLine;
	            System.out.println(outString);
	            in.close();
	            out.println(outString);
	        } 
	        catch (MalformedURLException e) {
	        }
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String  data = request.getParameter("command");
		String  imagedata = request.getParameter("imagedata");
		String server = request.getParameter("server");		
		System.out.println("Post request arrived "+data+server);		
		if(data == null) {
			OutputStream out = response.getOutputStream();
			System.out.println("-->" +request.getContentLength());
			try {
				InputStream inputStream = request.getInputStream();
			    byte[] buffer = new byte[8192];
			    ByteArrayOutputStream output = new ByteArrayOutputStream();
			    int bytesRead = 0;
			    int read;
			    while ((read = inputStream.read(buffer)) != -1)
			    {
			    	//int read = inputStream.read(buffer);
			    	output.write(buffer, 0, read);
			    	bytesRead = bytesRead + read;
			    }
			    System.out.println("-->" + bytesRead);			    
			    //Creating the post
			    URL postURL = new URL("http://"+serverURL+":"+serverPORT+"/postBinaryImage");
	            HttpURLConnection con = (HttpURLConnection) postURL.openConnection();            
	            con.setRequestMethod("POST");
	    		con.setRequestProperty("User-Agent", USER_AGENT);
	    		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
	    		con.setRequestProperty("Content-Type", "binary/octet-stream");
	    		con.setDoOutput(true);
	    		String urlParameters = "data="+data;
	    		OutputStream bout =con.getOutputStream();
	    		bout.write(output.toByteArray());
	    		bout.flush();
	    		bout.close();
	    		BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()));
	            String outString="";
	            String inputLine;
	            while (( inputLine = in.readLine()) != null)
	            	outString = outString+inputLine;
	            in.close();
	            if(outString.equals("")) {
	            	response.setStatus(500);
	            	out.write("ERROR".getBytes());
	            }
	            else {
	            	outString = outString.trim();
	            	if(outString.equalsIgnoreCase("READY")){
	            		response.setStatus(200);
	            		out.write("SUCCESS".getBytes());
	            	} else {
		            	out.write("ERROR".getBytes());
		            	response.setStatus(500);
		            }
	            }			    
			} catch (Exception ex) {
				out.write("ERROR".getBytes());
				ex.printStackTrace();
			}
		} else {
			PrintWriter out = response.getWriter();
			server = server.trim();
			data = data.trim();		
			if(data.equals("wsstart")){		
				try {
		            URL postURL = new URL("http://"+serverURL+":"+serverPORT+"/postImage");
		            HttpURLConnection con = (HttpURLConnection) postURL.openConnection();            
		            con.setRequestMethod("POST");
		    		con.setRequestProperty("User-Agent", USER_AGENT);
		    		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		    		con.setDoOutput(true);
		    		String urlParameters = "data="+data;
		    		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		    		wr.writeBytes(urlParameters);
		    		wr.flush();
		    		wr.close();		     
		    		int responseCode = con.getResponseCode();
		    		BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()));
		            String outString="";
		            String inputLine;
		            while (( inputLine = in.readLine()) != null)
		            	outString = outString+inputLine;
		            in.close();
		            if(outString.equalsIgnoreCase("READY"))
		            	out.println("SERVER_READY");
		            else
		            	out.println("ERROR");
		            
		        } 
		        catch (MalformedURLException e) {
		        }
			} else if (data.equals("post")){
				
			}
		}
	}

	/**
	 * @see HttpServlet#doPut(HttpServletRequest, HttpServletResponse)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
