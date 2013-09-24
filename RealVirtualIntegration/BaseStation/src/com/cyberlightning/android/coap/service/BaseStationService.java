package com.cyberlightning.android.coap.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.UUID;

import com.cyberlightning.android.coap.w4ds.interfaces.*;
import com.cyberlightning.android.coap.w4ds.messages.*;
import com.cyberlightning.android.coap.CoapBaseStation;
import com.cyberlightning.android.coap.resources.Settings;
import com.cyberlightning.android.coapclient.R;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdManager.RegistrationListener;
import android.net.nsd.NsdServiceInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class BaseStationService extends Service {


    protected static final String TAG = "BaseStationService";
    
	
	private DatagramSocket remoteServerSocket;
	private DatagramSocket localServerSocket;
    private BaseStationServiceBinder<BaseStationService> serviceBinder;
	
	private RegistrationListener mRegistrationListener;
	private NsdManager mNsdManager;
	private String mServiceName;

	
    private static int NOTIFICATION_ID;
   

    @Override
    public void onCreate() {    
        NOTIFICATION_ID = UUID.randomUUID().hashCode(); 
        //this.openCoapSocket();
    
          //this.listenForLocalCoapDevices();
        this.showNotification(R.string.app_name,R.string.service_started_notification);
        try {
			this.registerService(5683);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
  
	@Override
	public IBinder onBind(Intent arg0) {
		this.serviceBinder = new BaseStationServiceBinder<BaseStationService>(this);
		return this.serviceBinder;
	}
   
    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
    	((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancel(R.string.service_started_notification);
        Toast.makeText(this, R.string.service_stopped_notification, Toast.LENGTH_SHORT).show();
    }

    private void openCoapSocket() {
		
		new Thread(new Runnable() { 
			
		    public void run()  {
				
				try {
					
					remoteServerSocket = new DatagramSocket(Settings.COAP_DEFAULT_PORT);
					remoteServerSocket.setReceiveBufferSize(1024);
					byte[] receiveByte = new byte[1024]; //512 for IPv6 networks?
					remoteServerSocket.connect(InetAddress.getByName(Settings.REMOTEHOST), Settings.REMOTE_SERVER_PORT);
					DatagramPacket receivedPacket = new DatagramPacket(receiveByte, receiveByte.length);
			
					
					while(true) {
						
						remoteServerSocket.receive(receivedPacket);
						if (receivedPacket.getSocketAddress() != null) {
							handleIncommingMessage(receivedPacket);
							receivedPacket = null; //clear packet holder
						}
					}
					//TODO handle socket closed
					
				} catch(IOException e) {
					e.printStackTrace();
				} 
				
				return; 
				
			}}).start();	
		
	}
    
    public void registerService(int _port) throws UnknownHostException {
        NsdServiceInfo serviceInfo  = new NsdServiceInfo();
        serviceInfo.setServiceName("BaseStation");
        serviceInfo.setServiceType("_coap._udp");
        serviceInfo.setPort(_port);
        //this.openServiceSocket();
        this.openCoapSocket();
       
        mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
        this.initializeRegistrationListener();

        mNsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
    }
    
    private void openServiceSocket() {
		
		new Thread(new Runnable() { 
			
		    public void run()  {
				
				try {
					
					localServerSocket= new DatagramSocket(Settings.COAP_DEFAULT_PORT);
					localServerSocket.setReceiveBufferSize(1024);
					byte[] receiveByte = new byte[1024]; //512 for IPv6 networks?
					localServerSocket.connect(InetAddress.getLocalHost(), Settings.COAP_DEFAULT_PORT);
					DatagramPacket receivedPacket = new DatagramPacket(receiveByte, receiveByte.length);
			
					
					while(true) {
						
						localServerSocket.receive(receivedPacket);
						if (receivedPacket.getSocketAddress() != null) {
							handleIncommingMessage(receivedPacket);
							receivedPacket = null; //clear packet holder
						}
					}
					//TODO handle socket closed
					
				} catch(IOException e) {
					e.printStackTrace();
				} 
				
				return; 
				
			}}).start();	
		
	}
    public void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {

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
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                // Service has been unregistered.  This only happens when you call
                // NsdManager.unregisterService() and pass in this listener.
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Unregistration failed.  Put debugging code here to determine why.
            	
            }
        };
    }
    
    private void handleIncommingMessage(DatagramPacket _packet) {
    	
    	ByteBuffer buffer = ByteBuffer.wrap(_packet.getData());
    	
		CoapMessage msg;
		try {
			 msg = AbstractCoapMessage.parseMessage(buffer.array(), buffer.position());
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}
    
    private void showNotification (int _title, int _content) {
    	
    	NotificationCompat.Builder mBuilder =new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_launcher).setContentTitle(this.getString(_title)).setContentText(this.getString(_content));
    	   
    	Intent resultIntent = new Intent(this, CoapBaseStation.class);
    	    
    	TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
    	stackBuilder.addParentStack(CoapBaseStation.class); 
    	stackBuilder.addNextIntent(resultIntent);
    	    
    	PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
    	mBuilder.setContentIntent(resultPendingIntent);

    	((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, mBuilder.build());
    }
    
    /** Binder class for binding activities to base station server**/
	public class BaseStationServiceBinder<T> extends Binder implements IBaseStationServiceBinder {
		 private WeakReference<T> coapService;
		 
		public BaseStationServiceBinder(T service) {
			coapService = new WeakReference<T>(service);
		}
		 
		public T getService() {
		 return coapService.get();
		}
		 
		 
		 /** Message is being captured by base station service**/
		@Override
		 public void sendMessage(Message _msg) {
			
			if (remoteServerSocket.isConnected()) {
				
				try {
					byte[] byteBuffer = _msg.obj.toString().getBytes("UTF8");
					DatagramPacket packet = new DatagramPacket(byteBuffer,byteBuffer.length,InetAddress.getByName(Settings.REMOTEHOST),Settings.REMOTE_SERVER_PORT);
					packet.setData(byteBuffer);
					remoteServerSocket.send(packet);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		 }
	}
		 
	/** Interface for sending messages through the base station **/
	public interface IBaseStationServiceBinder {
		public void sendMessage(Message _msg);
	}	 
}