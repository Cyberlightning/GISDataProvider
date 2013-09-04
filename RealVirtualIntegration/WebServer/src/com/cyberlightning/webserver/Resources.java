package com.cyberlightning.webserver;

public abstract class Resources {
	
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
	
	
	
}
