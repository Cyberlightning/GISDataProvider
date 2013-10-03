/** @author Tomi Sarni (tomi.sarni@cyberlightning.com)
 *  Copyright: Cyberlightning Ltd.
 *  
 */


package com.cyberlightning.android.coap.application;

/**
 * @author Tomi sarni <tomi.sarni@cyberlightning.com>
 */

public final class Settings {
   
    public final static int COAP_MESSAGE_SIZE_MAX = 1152;
    public final static int COAP_DEFAULT_PORT = 5683;
    public final static int COAP_DEFAULT_MAX_AGE_S = 60;
    public final static int COAP_DEFAULT_MAX_AGE_MS = COAP_DEFAULT_MAX_AGE_S * 1000;
    public final static int DEFAULT_BYTE_BUFFER_SIZE = 1024;
    public final static int LOCAL_OUTBOUND_PORT = 45454;
    public final static int MESSAGE_ID_MIN = 0;
    public final static int MESSAGE_ID_MAX = 65535;
    public final static int REMOTE_SERVER_PORT = 61616;
    public final static int SERVICE_STARTED_NOTIFICATION = 1;
	public static final int SENSOR_EVENT = 1;
	
	
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";
	public final static String LOCALHOST_EMULATOR = "10.0.2.2";
	 public final static String REMOTEHOST = "dev.cyberlightning.com";
	public static final String SERVICE_TYPE = "_coap._udp"; 
	public static final String SERVICE_NAME = "BaseStation"; 
    

}
