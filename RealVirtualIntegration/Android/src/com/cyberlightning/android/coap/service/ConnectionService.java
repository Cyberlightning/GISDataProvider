package com.cyberlightning.android.coap.service;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.cyberlightning.android.coap.StaticResources;
import com.cyberlightning.android.coap.entities.CoapMessageObject;
import com.cyberlightning.android.coapclient.R;






import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.widget.Toast;


public class ConnectionService extends Service {

	public InetAddress serverAddress;
	public int portNumber;
	

	private NotificationManager notificationManager;
	private DatagramSocket socket;
	


	private boolean messageReceivedFromServer = false;

	private ArrayList<Messenger> registeredReceivers = new ArrayList<Messenger>();
	private Vector<CoapMessageObject> receiveBuffer = new Vector<CoapMessageObject>();
	private ArrayList<String> sendBuffer = new ArrayList<String>();
	

    private final Messenger messageHandler = new Messenger(new IncomingMessagesHandler());
	
    private final long CONNECTION_TIME_OUT = 65536;
    
    private static boolean isRunning = false;
    
    public static int NOTIFICATION;
    
    public static final int DISCOVER_SERVICE = 1;
    public static final int SENSOR_EVENT = 2;
    public static final int EXCEPTION_EVENT = 3;
    public static final int ACTUATOR_EVENT = 4;
    
    public static final int MSG_UNREGISTER_CLIENT = 5;
    public static final int MSG_REGISTER_CLIENT = 5;

    
	@Override
	public void onCreate() {
		NOTIFICATION = this.generateId();
		this.notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		this.registerNotification();

		//SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        //this.serverAddress = preferences.getString(getString(R.string.settings_preferences_ip), getString(R.string.settings_default_ip));    
        //this.portNumber = Integer.parseInt(preferences.getString(getString(R.string.settings_preferences_port), getString(R.string.settings_default_port)));
       
		
		isRunning = true;
      
        this.openConnection();
       
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		this.openConnection();
		isRunning = true;
		return START_STICKY;
	}

	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
       
	}
	@Override
    public IBinder onBind(Intent intent) {
        return this.messageHandler.getBinder();
    }
	
	
	@Override
    public void onDestroy() {
        this.notificationManager.cancelAll();
        Toast.makeText(this, getText(R.string.connection_service_stopped_notification), Toast.LENGTH_LONG).show();
    }

	public static boolean isRunning() {
        return isRunning;
    }
	
	public static int getNotificationId() {
        return NOTIFICATION;
    }
	
	private void stopConnectionService() {
		this.stopSelf();
	}
	
	private int generateId() {
		return UUID.randomUUID().hashCode();
	}
	  
	private void registerNotification() {
   
        Notification notification = new Notification(R.drawable.ic_launcher, getText(R.string.connection_service_started_notification),System.currentTimeMillis());
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, Application.class), 0);
	    notification.setLatestEventInfo(this, getText(R.string.connection_service_label), getText(R.string.connection_service_started_notification), contentIntent);
        this.notificationManager.notify(NOTIFICATION, notification);
    }
	
	private void openConnection() {
		startDeviceListener();
	}
	
	private void startDeviceListener() {
		
		new Thread(new Runnable() { 
			
		    public void run()  {
				
				try {
					
					socket = new DatagramSocket(StaticResources.COAP_DEFAULT_PORT);
					byte[] byteBuffer = new byte[256]; //512 for IPv6 networks?
					socket.connect(InetAddress.getByName(StaticResources.LOCALHOST), StaticResources.SERVER_UDP_PORT);
					DatagramPacket receivedPacket = new DatagramPacket(byteBuffer, byteBuffer.length);
					String testi = "testi";
					byteBuffer = testi.getBytes();
					boolean isEmpty = false;
					
					while(socket.isConnected()) {
						
						if (!isEmpty) {
							serverAddress = InetAddress.getByName(StaticResources.LOCALHOST);
							DatagramPacket packet = new DatagramPacket(byteBuffer, byteBuffer.length,serverAddress, StaticResources.SERVER_UDP_PORT);
							packet.setData(byteBuffer);
							socket.send(packet);
							isEmpty = true;
						}
						
						socket.receive(receivedPacket);
						if (receivedPacket.getSocketAddress() != null) {
							//process received package
							receivedPacket = null; //clear packet holder
						}
					}
					
				} catch(IOException e) {
					e.printStackTrace();
				} 
				
			}}).start();	
		
	}

	
	private void messageReceivedNotification () {
		
		
		if (!messageReceivedFromServer) {
			Notification notification = new Notification(R.drawable.ic_launcher, getText(R.string.connection_service_message_received),System.currentTimeMillis());
			this.notificationManager.cancelAll();
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, Application.class), 0);
			notification.setLatestEventInfo(this, getText(R.string.connection_service_label), getText(R.string.connection_service_message_received), contentIntent);    
			this.notificationManager.notify(NOTIFICATION, notification);
			messageReceivedFromServer = true; 
		}

	}
	
	private void sendMessageToReceiver(int type, String content) {
        
		for (Messenger messenger : this.registeredReceivers) {
            
		try {
					
			Message msg = Message.obtain(null, type, content);
				
//			if(type != FREQUENCY_CHANGE && type != CONNECTION_STATUS) {
//					Bundle bundle = new Bundle();
//			        bundle.putSerializable("receive_buffer", this.receiveBuffer);
//			        msg.setData(bundle);
//				}
					
			messenger.send(msg);
				
            } catch (RemoteException e) {
         
            	this.registeredReceivers.remove(messenger);
            }
        }
    }
	
	
	
	
	
	
	private void closeConnection() throws IOException {
		
		if(this.socket != null ) {
			this.socket.close();
		}
		else {
			this.sendMessageToReceiver(ConnectionService.EXCEPTION_EVENT, "Socket already closed, stopping service");
			this.stopConnectionService();
		} 
	}
	
	public static void handleDiscovery(Message msg ){
		
	}

	static class IncomingMessagesHandler extends Handler { 
	        
		 @Override
	     public void handleMessage(Message msg) {
			 
			 switch (msg.what) {
	            case ConnectionService.DISCOVER_SERVICE:
	            	ConnectionService.handleDiscovery(msg);
	                break;
	            case ConnectionService.ACTUATOR_EVENT:
	            	//TODO handleMessage ConnectionService.ACTUATOR_EVENT
	                break;
	            case ConnectionService.EXCEPTION_EVENT:
	            	//TODO handleMessage ConnectionService.EXCEPTION_EVENT
		           break;
	            case ConnectionService.SENSOR_EVENT:
	            	//TODO handleMessage ConnectionService.SENSOR_EVENT
	               break;
	            default:
	                super.handleMessage(msg);
	            }
	           
	        }
	}
	
	
	
	
}
