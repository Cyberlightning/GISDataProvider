package com.cyberlightning.android.coap.memory;



/** @author Tomi Sarni 
 *  email: tomi.sarni@cyberlightning.com
 */

/** This class simulates ROM memory of devices, and can be accessed in a static way**/
public class RomMemory {
	public static final String DEFAULT_SERVICE_TYPE = "_coap._udp.";
	public static final String DEFAULT_SERVICE_NAME = "BaseStation";
	public static final int DEFAULT_PORT = 5683;
	public static final int DEFAULT_BUFFER_SIZE = 1024;
	public static final int MAX_MESSAGE_ID = 65535;
	public static final int INBOUND_MESSAGE = 1;
	public static final int OUTBOUND_MESSAGE = 2;
	private static final long DEFAULT_BROADCAST_INTERVAL = 6000;
	
	
	
	/* 0                   1                   2                   3
	    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	   |Ver| T |  TKL  |      Code     |          Message ID           |
	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	   |   Token (if any, TKL bytes) ...
	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	   |   Options (if any) ...
	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	   |1 1 1 1 1 1 1 1|    Payload (if any) ...
	   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/
	
	
	/** Version (Ver):  2-bit unsigned integer.  Indicates the CoAP version
      number.  Implementations of this specification MUST set this field
      to 1 (01 binary).  Other values are reserved for future versions.
      Messages with unknown version numbers MUST be silently ignored.**/
	private static final String VER = "01";
	
	/**Type (T):  2-bit unsigned integer.  Indicates if this message is of type Confirmable (0), Non-confirmable (1), Acknowledgement (2) or Reset (3). **/
	private static final String T_CONFIRMABLE = "00";
	private static final String T_NON_CONFIRMABLE = "01"; 
	private static final String T_ACKNOWLEDGEMENT =  "10";
	private static final String T_RESET =  "11";
	
	/* +------+----------------------------+-----------+
     | Code | Description                | Reference |
     +------+----------------------------+-----------+
     | 2.01 | Created                    | [RFCXXXX] |
     | 2.02 | Deleted                    | [RFCXXXX] |
     | 2.03 | Valid                      | [RFCXXXX] |
     | 2.04 | Changed                    | [RFCXXXX] |
     | 2.05 | Content                    | [RFCXXXX] |
     | 4.00 | Bad Request                | [RFCXXXX] |
     | 4.01 | Unauthorized               | [RFCXXXX] |
     | 4.02 | Bad Option                 | [RFCXXXX] |
     | 4.03 | Forbidden                  | [RFCXXXX] |
     | 4.04 | Not Found                  | [RFCXXXX] |
     | 4.05 | Method Not Allowed         | [RFCXXXX] |
     | 4.06 | Not Acceptable             | [RFCXXXX] |
     | 4.12 | Precondition Failed        | [RFCXXXX] |
     | 4.13 | Request Entity Too Large   | [RFCXXXX] |
     | 4.15 | Unsupported Content-Format | [RFCXXXX] |
     | 5.00 | Internal Server Error      | [RFCXXXX] |
     | 5.01 | Not Implemented            | [RFCXXXX] |
     | 5.02 | Bad Gateway                | [RFCXXXX] |
     | 5.03 | Service Unavailable        | [RFCXXXX] |
     | 5.04 | Gateway Timeout            | [RFCXXXX] |
     | 5.05 | Proxying Not Supported     | [RFCXXXX] |
     +------+----------------------------+-----------+*/
	
	/**8-bit unsigned integer, split into a 3-bit class (most
      significant bits) and a 5-bit detail (least significant bits),
      documented as c.dd where c is a digit from 0 to 7 for the 3-bit
      subfield and dd are two digits from 00 to 31 for the 5-bit
      subfield. **/
	private static final String CODE_CREATED = "010" + "00001";
	private static final String CODE_DELETED= "010" + "00010";
	private static final String CODE_VALID = "010" + "00011";
	private static final String CODE_CHANGED = "010" + "00100";
	private static final String CODE_CONTENT = "010" + "00101";
	private static final String CODE_BAD_REQUEST = "100" + "00000";
	private static final String CODE_UNAUTHORIZED= "100" + "00001";
	private static final String CODE_BAD_OPTION = "100" + "00010";
	private static final String CODE_FORBIDDEN = "100" + "00011";
	private static final String CODE_NOT_FOUND = "100" + "00100";
	private static final String CODE_METHOD_NOT_ALLOWED = "100" + "00101";
	private static final String CODE_NOT_ACCEPTABLE = "100" + "00110";
	private static final String CODE_PRECONDITION_FAILED = "100" + "01100";
	private static final String CODE_REQUEST_ENTITY_TOO_LARGE = "100" + "01101";
	private static final String CODE_UNSUPPORTED_CONTENT_INFORMATION = "100" + "01111";
	private static final String CODE_INTERNAL_SERVER_ERROR = "101" + "0000+";
	private static final String CODE_NOT_IMPLEMENTED = "101" + "00001";
	private static final String CODE_BAD_GATEWAY = "101" + "00010";
	private static final String CODE_SERVICE_UNAVAILABLE = "101" + "00011";
	private static final String CODE_GATEWAY_TIMEOUT = "101" + "00100";
	private static final String CODE_PROXYING_NOT_SUPPORTED = "101" + "00101";
	
	


}
