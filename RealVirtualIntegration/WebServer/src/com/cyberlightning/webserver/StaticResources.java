package com.cyberlightning.webserver;

public abstract class StaticResources {
	
	public static final String HTML_START =
			"<html>" +
			"<head>"+
			"<LINK href=\"styles.css\" rel=\"stylesheet\" type=\"text/css\">" +
			"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\"/>"	+	
			"<title>Cyberlightning FI-WARE webserver</title>" +
		    "</head>" +
			"<body style=\"background-color:#1F3D5C;color:#ffffff\">" +
		    "<div align=\"center\" id=\"holder\">";
		    
	
	public static final String HTML_END =
			
			"</div><div align=\"center\" id=\"timer\"/></body>" +
			"</html>";
	
	public static final String RESPONSE_WELCOME_MESSAGE = "<b>Cyberlightning web server </b><BR>";
	
	public static final String RESPONSE_CLIENT_REQUEST_MESSAGE = "The HTTP Client request is ....<BR>";
	
	public static final String ERROR_404_MESSAGE = "<b>ERROR 404: The requested resource not found</b>";
	
	public static final int CLIENT_PROTOCOL_COAP= 1;
	
	public static final int CLIENT_PROTOCOL_UDP= 2;
	
	public static final int CLIENT_PROTOCOL_TCP= 3;
	
	public static final int CLIENT_PROTOCOL_HTTP= 4;
	
	public static final int UDP_PACKET_SIZE = 512;
	
	public static final int SERVER_PORT_COAP = 61616; //default compressed UDP port space defined in [RFC4944]
	
	public static final int SERVER_PORT = 44444;
	public static final String LOCAL_HOST = "127.0.0.1";
	public static final String SERVER_DETAILS = "Cyberlightning Web Server";
	public static final int ACTIVITY_DURATION = 300; // in seconds
	public static final int WEB_SOCKET_PORT = 44445; 
	
	public static final int MAX_CONNECTED_CLIENTS = 10;
	
	
	
}
