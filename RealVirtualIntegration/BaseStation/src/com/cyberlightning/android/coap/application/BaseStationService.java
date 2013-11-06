/** @author Tomi Sarni (tomi.sarni@cyberlightning.com)
 *  Copyright: Cyberlightning Ltd.
 */

package com.cyberlightning.android.coap.application;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cyberlightning.android.coap.w4ds.interfaces.*;
import com.cyberlightning.android.coap.w4ds.messages.*;
import com.cyberlightning.android.coapclient.R;


import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdManager.RegistrationListener;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class BaseStationService extends Service {

	private ArrayList<CoapDevice> devices = new ArrayList<CoapDevice>();
	private ArrayList<Client> clients  = new ArrayList<Client>();
	private DatagramSocket remoteServerSocket;
	private DatagramSocket localServerSocket;	
	private RegistrationListener serviceRegisterationListener;
	
	private static List<Messenger> connectedActivities = new ArrayList<Messenger>(); 
	private final Messenger messenger = new Messenger(new IncomingMessageHandler()); 
	
	public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_RECEIVED_FROM_WEBSERVICE = 3;
    public static final int MSG_RECEIVED_FROM_COAPDEVICE = 4;
    public static final int MSG_UI_EVENT = 5;
      
    private static int NOTIFICATION_ID;
    private static boolean isRunning = false;
   

    @Override
    public void onCreate() {    
        NOTIFICATION_ID = UUID.randomUUID().hashCode(); 
        isRunning = true;
        this.openServiceSocket();
        this.showNotification(R.string.app_name,R.string.service_started_notification);
        
        try {
			this.registerService();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	@Override
    public IBinder onBind(Intent intent) {
            return this.messenger.getBinder();
    }
   
    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
    	((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);
        Toast.makeText(this, R.string.service_stopped_notification, Toast.LENGTH_SHORT).show();
        isRunning = false;
        this.unregisterService();
        this.stopSelf();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
            return START_STICKY; // Run until explicitly stopped.
    }
    
    public static boolean isRunning() {
            return isRunning;
    }
    
    /** Initiates a pending call that will restart the service to avoid it being shutdown by the Android device **/
    private void initiateAwakeSelf() { //TODO whether to use this or not? 
    	Calendar cal = Calendar.getInstance();

    	Intent intent = new Intent(this, BaseStationService.class);
    	PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);

    	AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
    	// Start every 30 seconds
    	alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 30*1000, pintent); 
    }
    
    /** Register NSD-SD service and open port for handling inbound messages */
    public void registerService() throws UnknownHostException {
        
    	NsdServiceInfo serviceInfo  = new NsdServiceInfo();
        serviceInfo.setServiceName(Settings.SERVICE_NAME);
        serviceInfo.setServiceType(Settings.SERVICE_TYPE);
        serviceInfo.setPort(Settings.COAP_DEFAULT_PORT);
        this.openCoapSocket();
        
        this.initializeRegistrationListener();

        ((NsdManager) getSystemService(Context.NSD_SERVICE)).registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, this.serviceRegisterationListener);
    }
    
    private void unregisterService() {
    	 ((NsdManager) getSystemService(Context.NSD_SERVICE)).unregisterService(this.serviceRegisterationListener);
    }
    
    
    /** Forms a basic CoAP message
     * 
     * @param reliable set to true for confirmable message and false for non-confirmable message
     * @param requestCode HTTP POST/GET/PUT/DELETE
     * @return BasicCoapRequest
     */
    private BasicCoapRequest createRequest(boolean reliable, CoapRequestCode requestCode) {
	    BasicCoapRequest msg = new BasicCoapRequest(reliable ? CoapPacketType.CON : CoapPacketType.NON, requestCode,this.getNewMessageID());
	    return msg;
	}
	  
	/** Creates a new, global message id for a new COAP message */ 
	private synchronized int getNewMessageID() {
	    Random random = new Random();
	    return random.nextInt(Settings.MESSAGE_ID_MAX + 1);
	}
    
    public void initializeRegistrationListener() {
        this.serviceRegisterationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                // Save the service name.  Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.
            	System.out.print(NsdServiceInfo.getServiceName() + " registered");
             
            }
            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Registration failed!  Put debugging code here to determine why.
            	System.out.print("failed registeration");
            }
            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                // Service has been unregistered.  This only happens when you call
                // NsdManager.unregisterService() and pass in this listener.
            	System.out.print(" unregisteration");
            }
            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Unregistration failed.  Put debugging code here to determine why.
            	System.out.print("failed unregisteration");	
            }
        };
    }
     
    /** Display service notification
     * @param _title Title of the notification
     * @param _content Notification description
     */
    private void showNotification (int _title, int _content) {
    	
    	NotificationCompat.Builder mBuilder =new NotificationCompat.Builder(this).setSmallIcon(R.drawable.cyber_icon).setContentTitle(this.getString(_title)).setContentText(this.getString(_content));
    	   
    	Intent resultIntent = new Intent(this, BaseStationUI.class);
    	    
    	TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
    	stackBuilder.addParentStack(BaseStationUI.class); 
    	stackBuilder.addNextIntent(resultIntent);
    	    
    	PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
    	mBuilder.setContentIntent(resultPendingIntent);

    	((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, mBuilder.build());
    }
    
    /** Opens a new socket for connected devices in local area network**/
    private void openServiceSocket() {
		
		new Thread(new Runnable() { 
			
		    public void run()  {
				
				try {
					
					remoteServerSocket= new DatagramSocket(Settings.LOCAL_OUTBOUND_PORT);
					remoteServerSocket.setReceiveBufferSize(Settings.DEFAULT_BYTE_BUFFER_SIZE);
					remoteServerSocket.connect(InetAddress.getByName(Settings.REMOTEHOST), Settings.REMOTE_SERVER_PORT);
					
					byte[] receiveByte = new byte[Settings.DEFAULT_BYTE_BUFFER_SIZE];
					byte[] triggerByte = new byte[Settings.DEFAULT_BYTE_BUFFER_SIZE];
					DatagramPacket triggerPacket = new DatagramPacket(triggerByte,triggerByte.length);
					DatagramPacket receivedPacket = new DatagramPacket(receiveByte, receiveByte.length);
					String s = "TESTI";
					triggerPacket.setData(s.getBytes());
					remoteServerSocket.send(triggerPacket);
					
					while(true) {
						remoteServerSocket.receive(receivedPacket);
						handleInboundRemoteMessage(receivedPacket);
					}
					//TODO handle socket closed
					
				} catch(IOException e) {
					e.printStackTrace();
				} 
				
				return; 
				
			}}).start();	
		
	}

    /** Handles packets received from remote web services and transports them to targeted sensors if possible*/
    private void handleInboundRemoteMessage(DatagramPacket _packet) {
    	
    	JSONObject clientMessage = null;
    	String address = "";
    	String targetDevice = "";
    	String message = "";
    	try {
			
    		String payload = new String(_packet.getData(), "utf8");
			clientMessage = new JSONObject(payload);
			address = clientMessage.getString("notificationURI");
			targetDevice = clientMessage.getJSONObject("contextEntities").getString("DeviceID");
			message = clientMessage.getString("request");
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	boolean isNewClient = this.registerClient(address);
    	this.sendMessageToSpecificSensor(targetDevice, message, address, isNewClient);
    }
    	
    private void sendMessageToSpecificSensor(String _deviceId, String _message,String sender, boolean isNewClient) {
    	
    	CoapRequest coapRequest = this.createRequest(true, CoapRequestCode.POST);
	    coapRequest.setContentType(CoapMediaType.text_plain);
		//coapRequest.setUriPath("/devices");
	    
		coapRequest.setPayload(_message);
		CoapMessage coapMessage = (CoapMessage)coapRequest;
		//coapRequest.setUriQuery(jsonSensorsList.toString());
		ByteBuffer buf = ByteBuffer.wrap(coapMessage.serialize());
		
		Iterator<CoapDevice> i = this.devices.iterator();
		int foundCount = 0;
		while(i.hasNext()) {
			CoapDevice device = i.next();
			if (device.getIpAdress().getHostAddress().contentEquals(_deviceId) || _deviceId.contentEquals("*")) { // if * -> send to all
				DatagramPacket packet = new DatagramPacket(buf.array(), buf.array().length, device.getIpAdress(), device.getPort());
				foundCount++;
				MessageEvent messageEvent = new MessageEvent(_message,sender,isNewClient,device.getIpAdress().getHostAddress());
		    	messageEvent.setSenderAddress(sender);
		    	this.sendMessageToUI(Message.obtain(null, MSG_RECEIVED_FROM_WEBSERVICE,messageEvent ));
				
		    	try {
					this.localServerSocket.send(packet);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		
		if (foundCount == 0) { //handle if message received and was not able to find a target device
			MessageEvent messageEvent = new MessageEvent(_message,sender,isNewClient, "None Connected");
	    	messageEvent.setSenderAddress(sender);
	    	this.sendMessageToUI(Message.obtain(null, MSG_RECEIVED_FROM_WEBSERVICE,messageEvent ));
		}
    }
    
    /***/
    private boolean registerClient(String _address ) {
    	
    	Iterator<Client> i = this.clients.iterator();
    	boolean isRegistered = false;
    	while (i.hasNext()) {
    		if (i.next().getAddress().contentEquals(_address)) {
    			isRegistered = true;
    		}
    	}
    	if (!isRegistered) {
    		this.clients.add(new Client(_address));
    	}
    	return isRegistered;
    }
    
    /** Opens a new socket for connected devices in local area network**/
    private void openCoapSocket() {
		
		new Thread(new Runnable() { 
			
		    public void run()  {
				
				try {
					
					localServerSocket = new DatagramSocket(Settings.COAP_DEFAULT_PORT);
					localServerSocket.setReceiveBufferSize(Settings.DEFAULT_BYTE_BUFFER_SIZE);
					byte[] receiveByte = new byte[Settings.DEFAULT_BYTE_BUFFER_SIZE]; 
					DatagramPacket receivedPacket = new DatagramPacket(receiveByte, receiveByte.length);	

					while(true) {
						
						localServerSocket.receive(receivedPacket);
						handleInboundLocalMessages(receivedPacket);
					}
					//TODO handle socket closed
				} catch(IOException e) {
					e.printStackTrace();
				} 
				return; 
			}}).start();	
	}
    
    /** */
    private void handleInboundLocalMessages(DatagramPacket _packet) {
    	
    	InetAddress address = _packet.getAddress();
    	boolean isNewDevice = this.registerDevice(address,_packet.getPort());
    	
    	ByteBuffer buffer = ByteBuffer.wrap(_packet.getData());
    	
		CoapMessage msg = null;
		String payload = null;

		try {
			msg = AbstractCoapMessage.parseMessage(buffer.array(), buffer.array().length);
			payload = new String(msg.getPayload(), "utf8");
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		this.sendMessageToUI(Message.obtain(null,BaseStationService.MSG_RECEIVED_FROM_COAPDEVICE , new MessageEvent(payload,address.getHostAddress(),isNewDevice, Settings.REMOTEHOST)));
		
		//TODO input logic to handle ACK,NON,RST, .. 
		
		DatagramPacket udp = new DatagramPacket(msg.getPayload(), msg.getPayload().length);
		try {
			this.remoteServerSocket.send(udp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
    /** Checks whether a device with InetAddress has been registered already and registers it if not. 
     * @return true if already registered*/
    private boolean registerDevice(InetAddress _address, int _port ) {
    	
    	Iterator<CoapDevice> i = this.devices.iterator();
    	boolean isNewDevice = true;
    	while (i.hasNext()) {
    		if (i.next().getIpAdress().getHostAddress().contains(_address.getHostAddress())) {
    			isNewDevice = false;
    		}
    	}
    	if (isNewDevice) {
    		this.devices.add(new CoapDevice(_address,_port));
    	}
    	return isNewDevice;
    }
    
    /**
     * Send the data to all clients.
     * @param _msg payload to send 
     * @param _origin original sender
     */
    private void sendMessageToUI(Message _message) {
    	Iterator<Messenger> messengerIterator = connectedActivities.iterator();            
        while(messengerIterator.hasNext()) {
        	try {                              
                messengerIterator.next().send(_message);
            } catch (RemoteException e) {
            	connectedActivities.remove(messenger);
            }
        }
    }

	/** Handle incoming messages from MainActivity*/
    static class IncomingMessageHandler extends Handler { // Handler of incoming messages from clients.
            @Override
            public void handleMessage(Message msg) {
                   
                    switch (msg.what) {
                    case BaseStationService.MSG_REGISTER_CLIENT:
                    	connectedActivities.add(msg.replyTo);
                            break;
                    case BaseStationService.MSG_UNREGISTER_CLIENT:
                    	connectedActivities.remove(msg.replyTo);
                            break;
                    case BaseStationService.MSG_UI_EVENT: //TODO handle UI events
                    	break;
                    default:
                            super.handleMessage(msg);
                    }
            }
    }
}