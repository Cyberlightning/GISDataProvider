package com.cyberlightning.webserver;

import java.text.SimpleDateFormat;
import java.util.Date;

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
	
	
	
	public static final int CLIENT_PROTOCOL_COAP= 1;
	
	public static final int CLIENT_PROTOCOL_UDP= 2;
	
	public static final int CLIENT_PROTOCOL_TCP= 3;
	
	public static final int CLIENT_PROTOCOL_HTTP= 4;
	
	public static final int UDP_PACKET_SIZE = 1024;
	
	public static final int SERVER_PORT_COAP = 61616; //default compressed UDP port space defined in [RFC4944]
	public static final int DEFAULT_COAP_PORT = 5683;
	public static final int SERVER_PORT = 44446;
	public static final int WEB_SOCKET_PORT = 44445; 
	public static final int DEFAULT_BASESTATION_PORT = 45454;
//	public static final int SERVER_PORT_COAP = 61615; //default compressed UDP port space defined in [RFC4944]
//	public static final int DEFAULT_COAP_PORT = 5683;
//	public static final int SERVER_PORT = 44447;
//	public static final int WEB_SOCKET_PORT = 44444; 
//	public static final int DEFAULT_BASESTATION_PORT = 45457;
	public static final String LOCAL_HOST = "127.0.0.1";
	public static final String SERVER_DETAILS = "Cyberlightning Web Server";
	public static final int ACTIVITY_DURATION = 300; // in seconds

	
	public static final int MAX_CONNECTED_CLIENTS = 50;
	
	public static final int MAX_NUM_OF_THREADS = 7;
	
	public static final String MAGIC_STRING = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
	
	public static final String CLIENT_DISCONNECTED = " disconnected, socket shutdown gracefully";
	public static final String CLIENT_CONNECTED = " connected, connection established";
	
	public static final String DATABASE_FILE_NAME = "deviceDB.ser";
	public static final String REFERENCE_TABLE_FILE_NAME = "BaseStationRefernceDB.ser";
	
	
	public static final int DEFAULT_NUM_OF_ENTRIES = 10000;
	
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";
	
	public static String getTimeStamp() {
		return  new SimpleDateFormat(DATE_FORMAT).format(new Date(System.currentTimeMillis()));
	}
	
	public static final int HTTP_CLIENT = 0;
	public static final int TCP_CLIENT = 1;
	public static final int UDP_RECEIVER = 2;
	public static final int UDP_RESPONSE = 3;
	
	public static final int QUERY_SPATIAL_CIRCLE = 0;
	public static final int QUERY_SPATIA_BOUNDING_BOX = 1;
	public static final int QUERY_SPATIA_SHAPE = 2;
	public static final int QUERY_TYPE = 3;
	
	public static final int HTTP_GET = 0;
	public static final int HTTP_POST_FILE = 1;
	public static final int HTTP_POST_NON_FILE = 2;
	
	public static final String HTTP_CODE_OK = "200 OK";
	public static final String ERROR_CODE_BAD_REQUEST = "ERROR 400 BAD REQUEST";
	public static final String ERROR_CODE_METHOD_NOT_ALLOWED = "ERROR 405 METHOD NOT ALLOWED";
	public static final String ERROR_CODE_NOT_FOUND = "ERROR 404 NOT FOUND";

	
	public static final long SAVE_TO_HD_INTERVAL = 10000;
	public static final int MAX_HISTORY_VALUES_FOR_SENSOR = 5;


	
}
