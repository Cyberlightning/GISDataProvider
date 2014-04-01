package org.FIware.cyber.d2d3.web.desktop;

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
 * Servlet implementation class DesktopClientMultiplexer
 */
@WebServlet("/DesktopClientMultiplexer")
public class DesktopClientMultiplexer extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String serverURL = "dev.cyberlightning.com";
	private static final String serverPORT = "17321";
//    private static String serverURL = "localhost";
//    private static final String serverPORT = "17322";    
    private final String USER_AGENT = "Mozilla/5.0";
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DesktopClientMultiplexer() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String  data = request.getParameter("command");
		String server = request.getParameter("url");
		System.out.println("Post request arrived "+data+	":"+server);
		if(!server.equals(""))
			serverURL = server;		
		PrintWriter out = response.getWriter();
		if(data != null)  {
			server = server.trim();
			data = data.trim();		
			if(data.equals("get")){		
				try {
		            URL getURL = new URL("http://"+serverURL);
		            HttpURLConnection con = (HttpURLConnection) getURL.openConnection();            
		            con.setRequestMethod("GET");
		    		con.setRequestProperty("User-Agent", USER_AGENT);
		    		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		    		con.setRequestProperty("Content-Type", "binary/octet-stream");
		    		con.setDoOutput(true);
		            BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()));	
		            String outString="";
		            String inputLine;
		            while (( inputLine = in.readLine()) != null)
		            	outString = outString+inputLine;
		            in.close();
		            System.out.println("Server response arrived "+outString);
		            if(outString.equalsIgnoreCase("READY")){
		            	out.println("SERVER_READY");
		            	out.flush();
		            	out.close();
		            } else if(outString.equalsIgnoreCase("CLOSSED") || outString.equalsIgnoreCase("ALREADY_CLOSSED"))
		            	out.println("CLOSSED");
		            else {
		            	out.println("ERROR");
		            }
		        } 
		        catch (MalformedURLException e) {
		        	e.printStackTrace();
		        	out.println(e.getMessage());
		        } catch (Exception c) {
		        	c.printStackTrace();
		        	out.println(c.getMessage());
		        }
		        
				
			} else if (data.equals("post")){
				
			}
		}
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
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
