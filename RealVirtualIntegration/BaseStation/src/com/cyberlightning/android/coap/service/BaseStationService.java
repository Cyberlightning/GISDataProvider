package com.cyberlightning.android.coap.service;

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

import com.cyberlightning.android.coap.w4ds.interfaces.*;
import com.cyberlightning.android.coap.w4ds.messages.*;
import com.cyberlightning.android.coap.CoapBaseStation;
import com.cyberlightning.android.coap.CoapDevice;
import com.cyberlightning.android.coap.resources.Settings;
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
	private DatagramSocket remoteServerSocket;
	private DatagramSocket localServerSocket;	
	private NsdManager mNsdManager;
	private RegistrationListener serviceRegisterationListener;
	private String mServiceName;
	
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
        this.isRunning = true;
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
    	((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancel(this.NOTIFICATION_ID);
        Toast.makeText(this, R.string.service_stopped_notification, Toast.LENGTH_SHORT).show();
        this.isRunning = false;
        
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
						if (receivedPacket.getSocketAddress() != null) {
							handleIncommingMessage(receivedPacket);
						}
					}
					//TODO handle socket closed
				} catch(IOException e) {
					e.printStackTrace();
				} 
				return; 
				
			}}).start();	
		
	}
    
    /** Register NSD-SD service and open port for handling inbound messages */
    public void registerService() throws UnknownHostException {
        
    	NsdServiceInfo serviceInfo  = new NsdServiceInfo();
        serviceInfo.setServiceName(Settings.SERVICE_NAME);
        serviceInfo.setServiceType(Settings.SERVICE_TYPE);
        serviceInfo.setPort(Settings.COAP_DEFAULT_PORT);
        this.openCoapSocket();
       
        mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
        this.initializeRegistrationListener();

        mNsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, this.serviceRegisterationListener);
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
					DatagramPacket receivedPacket = new DatagramPacket(receiveByte, receiveByte.length);
					
					while(true) {
						remoteServerSocket.receive(receivedPacket);
						broadCastToAllCoapDevices(Message.obtain(null, BaseStationService.MSG_RECEIVED_FROM_WEBSERVICE,  new String(receivedPacket.getData(),"utf8")));
						registerDevice(receivedPacket);
					}
					//TODO handle socket closed
					
				} catch(IOException e) {
					e.printStackTrace();
				} 
				
				return; 
				
			}}).start();	
		
	}
    
    /** Serializes message in to a CoAP message and sends it to all registered devices on local area network */
    private void broadCastToAllCoapDevices (Message _msg) {
    	
    	CoapRequest coapRequest = this.createRequest(true, CoapRequestCode.POST);
	    coapRequest.setContentType(CoapMediaType.text_plain);
		//coapRequest.setUriPath("/devices");
	    
		coapRequest.setPayload(_msg.obj.toString());
		CoapMessage coapMessage = (CoapMessage)coapRequest;
		//coapRequest.setUriQuery(jsonSensorsList.toString());
		ByteBuffer buf = ByteBuffer.wrap(coapMessage.serialize());
		
		Iterator<CoapDevice> i = this.devices.iterator();
		while(i.hasNext()) {
			CoapDevice device= i.next();
			DatagramPacket packet = new DatagramPacket(buf.array(), buf.array().length, device.getIpAdress(), device.getPort());
			try {
				this.localServerSocket.send(packet);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
                mServiceName = NsdServiceInfo.getServiceName();
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
    
    /***/
    private void registerDevice(DatagramPacket _packet ) {
    	
    	Iterator<CoapDevice> i = this.devices.iterator();
    	boolean isRegistered = false;
    	while (i.hasNext()) {
    		if (i.next().getIpAdress().getHostAddress().contains(_packet.getAddress().getHostAddress())) {
    			isRegistered = true;
    		}
    	}
    	if (!isRegistered) {
    		this.devices.add(new CoapDevice(_packet.getAddress(), _packet.getPort()));
    	}
    }
    
    /***/
    private void handleIncommingMessage(DatagramPacket _packet) {
    	
    	ByteBuffer buffer = ByteBuffer.wrap(_packet.getData());
    	
		CoapMessage msg = null;
		try {
			msg = AbstractCoapMessage.parseMessage(buffer.array(), buffer.array().length);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String payload = null;
		try {
			payload = new String(msg.getPayload(), "utf8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.print(payload);
		DatagramPacket udp = new DatagramPacket(msg.getPayload(), msg.getPayload().length);
		try {
			this.remoteServerSocket.send(udp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
    /** Display service notification
     * @param _title Title of the notification
     * @param _content Notification description
     */
    private void showNotification (int _title, int _content) {
    	
    	NotificationCompat.Builder mBuilder =new NotificationCompat.Builder(this).setSmallIcon(R.drawable.cyber_icon).setContentTitle(this.getString(_title)).setContentText(this.getString(_content));
    	   
    	Intent resultIntent = new Intent(this, CoapBaseStation.class);
    	    
    	TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
    	stackBuilder.addParentStack(CoapBaseStation.class); 
    	stackBuilder.addNextIntent(resultIntent);
    	    
    	PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
    	mBuilder.setContentIntent(resultPendingIntent);

    	((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, mBuilder.build());
    }
    
    /**
     * Send the data to all clients.
     * @param _msg payload to send 
     * @param _origin original sender
     */
    private void sendMessageToUI(Object _msg, int _origin) {
            Iterator<Messenger> messengerIterator = connectedActivities.iterator();            
            while(messengerIterator.hasNext()) {
                    Messenger messenger = messengerIterator.next();
                    try {                              
                            messenger.send(Message.obtain(null, _origin, _msg));

                    } catch (RemoteException e) {
                            // The client is dead. Remove it from the list.
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
                    case BaseStationService.MSG_RECEIVED_FROM_COAPDEVICE:
                            //incrementBy = msg.arg1;
                            break;
                    default:
                            super.handleMessage(msg);
                    }
            }
    }
}