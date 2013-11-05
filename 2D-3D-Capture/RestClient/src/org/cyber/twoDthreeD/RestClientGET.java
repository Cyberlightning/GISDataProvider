package org.cyber.twoDthreeD;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class RestClientGET
 */
@WebServlet("/RestClientGET")
public class RestClientGET extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
    public RestClientGET() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
        
        try {
            URL getPhotoList = new URL("http://dev.cyberlightning.com:17000/googlemap");
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
       
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
