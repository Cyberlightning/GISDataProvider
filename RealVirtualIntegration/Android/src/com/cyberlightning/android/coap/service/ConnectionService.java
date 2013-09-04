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

import com.cyberlightning.android.coap.entities.CoapMessageObject;
import com.cyberlightning.android.coap.entities.StaticResources;
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

	private boolean isHandShakeReceived = false;
	private boolean isReadFinished = true;

	

	private int totalConnectionTimeElapsed = 0;
	private long connectionStarted;


	private Handler connTimerHandler;
	private Runnable connTimer;
	
	private ArrayList<Messenger> registeredReceivers = new ArrayList<Messenger>();
	private Vector<CoapMessageObject> receiveBuffer = new Vector<CoapMessageObject>();
	private ArrayList<String> sendBuffer = new ArrayList<String>();
	

    private final Messenger messageHandler = new Messenger(new IncomingMessagesHandler());
	
    private final long CONNECTION_TIME_OUT = 65536;
    
    private static boolean isRunning = false;
    
    public static int NOTIFICATION;
    
    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MESSAGE_RECEIVED = 3;
    public static final int FREQUENCY_CHANGE = 4;
    public static final int ACTIVITY_STATUS = 5;
    public static final int CONNECTION_STATUS = 6;
    public static final int MESSAGE_SEND = 7;
    
    public static final int STATE_UP = -1;
    public static final int STATE_DOWN = -2;
    public static final int LONG_STATE_UP = -3;
    public static final int CONNECTION_CLOSED = -4;
    public static final int CONNECTION_OPEN = -5;
    public static final int CONNECTION_TIMEOUT = -6;
    public static final int NO_REPLY_FROM_SERVER = - 7;
    public static final int RETRY_CONNECTION = -8;
    

    
    
	@Override
	public void onCreate() {
		NOTIFICATION = this.generateId();
		this.notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		this.registerNotification();

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        //this.serverAddress = preferences.getString(getString(R.string.settings_preferences_ip), getString(R.string.settings_default_ip));    
        //this.portNumber = Integer.parseInt(preferences.getString(getString(R.string.settings_preferences_port), getString(R.string.settings_default_port)));
       
		
		isRunning = true;
       
        this.initConnectionTimer();
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
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
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
							DatagramPacket packet = new DatagramPacket(byteBuffer, byteBuffer.length,serverAddress, StaticResources.SERVER_UDP_PORT);
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
		
		if(!isHandShakeReceived) isHandShakeReceived = true; //Server alive
		
		if (!messageReceivedFromServer) {
			Notification notification = new Notification(R.drawable.ic_launcher, getText(R.string.connection_service_message_received),System.currentTimeMillis());
			this.notificationManager.cancelAll();
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
			notification.setLatestEventInfo(this, getText(R.string.connection_service_label), getText(R.string.connection_service_message_received), contentIntent);    
			this.notificationManager.notify(NOTIFICATION, notification);
			messageReceivedFromServer = true; 
		}

	}
	
	private void sendMessageToReceiver(int type, int state, int duration , int value1, int value2) {
        
		
		if(type != FREQUENCY_CHANGE && type != CONNECTION_STATUS) {
			CoapMessageObject messageObject = new CoapMessageObject(state,duration);
			this.receiveBuffer.addElement(messageObject);
		} 
		
		for (int i=0; i < this.registeredReceivers.size(); i++) {
            
			try {
					
				Message msg = Message.obtain(null, type, value1, value2);
				
				if(type != FREQUENCY_CHANGE && type != CONNECTION_STATUS) {
					Bundle bundle = new Bundle();
			        bundle.putSerializable("receive_buffer", this.receiveBuffer);
			        msg.setData(bundle);
				}
					
			    this.registeredReceivers.get(i).send(msg);
				
            } catch (RemoteException e) {
         
            	this.registeredReceivers.remove(i);
            }
        }
    }
	
	private void initConnectionTimer () {
		 
		this.connTimerHandler = new Handler(); 
		this.connTimer  = new Runnable() {
			   public void run() {
			       
			       //long currentTime = System.currentTimeMillis();
			       if (!isReadFinished) {
			    	   handleConnectionException();
			    	   connTimerHandler.removeCallbacks(this);
			       }
				   //connTimerHandler.postAtTime(this, currentTime + CONNECTION_TIME_OUT);
			   }
			};
	 }
	
	private void handleConnectionException() {
		
		this.connTimerHandler.removeCallbacks(this.connTimer);
		//this.closeConnection();
		
		if(!isHandShakeReceived) { //No hand-shake from server, server down. Thus no retry for connection needed.
			
			this.sendMessageToReceiver(CONNECTION_STATUS, 0, 0, NO_REPLY_FROM_SERVER, this.totalConnectionTimeElapsed);
			this.stopConnectionService();
			
		} 
		if (isHandShakeReceived) { //Connection timed out, retry with different channel 
			if(this.registeredReceivers.size() > 0) {
			this.sendMessageToReceiver(CONNECTION_STATUS, 0, 0, CONNECTION_TIMEOUT, 0);
			this.isHandShakeReceived = false; //reset hand-shake in case server not reachable anymore
			} else {
				this.stopConnectionService();
			}
		}
		
	}
	
	
	private void closeConnection() throws IOException {
		
		if(this.socket != null ) {
			this.socket.close();
		}
		else {
			this.sendMessageToReceiver(CONNECTION_STATUS, 0, 0, NO_REPLY_FROM_SERVER, this.totalConnectionTimeElapsed);
			this.stopConnectionService();
		} 
	}

	class IncomingMessagesHandler extends Handler { 
	        
		 @Override
	     public void handleMessage(Message msg) {
			 
			 switch (msg.what) {
	            case MSG_REGISTER_CLIENT:
	            	registeredReceivers.add(msg.replyTo);
	                break;
	            case MSG_UNREGISTER_CLIENT:
	            	registeredReceivers.remove(msg.replyTo);
	                break;
	            case MESSAGE_SEND:
	            	handleSendMessage(msg.arg1,msg.arg2);
		           break;
	            case FREQUENCY_CHANGE:
	            	handleSendFrequency(msg.arg1);
	               break;
	            case ACTIVITY_STATUS:
	            	if(msg.arg1 == RETRY_CONNECTION) {
	            		openConnection();
	            		handleSendFrequency(msg.arg2);
	            		sendMessageToReceiver(FREQUENCY_CHANGE, 0, 0,msg.arg2 , 0); //check if better way
	            	} 
	               break;
	         
	            default:
	                super.handleMessage(msg);
	            }
	           
	        }
	
		 public void handleSendMessage(int duration, int channel) {
			
				
				int stateUp = (int)(System.currentTimeMillis() - connectionStarted);
				sendBuffer.add("[STATE_UP] " + Integer.toString(stateUp));
				short stateDown = (short)duration;
				sendBuffer.add("[STATE_DOWN] " + Short.toString(stateDown));
				
			
			
		}
			
		public void handleSendFrequency(int freq){
			sendBuffer.add("[CHANGE_FREQUENCY] " + Integer.toString(-freq));	
		}
	}
	
	 class SimpleHttpRequestTask extends AsyncTask<String[], String, String> { // might not be needed


			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				//Do some prepartations over here, before the task starts to execute
				//Like freeze the button and/or show a progress bar
			}

			
			@Override
			protected String doInBackground(String[]... params) {
				// Task starts executing.
							String url = params[0][0].toString();

							// Execute HTTP requests here, with one url(urls[0]),
							// or many urls using the urls table
							// Save result in myresult
							
							return simpleHttp(url);
			}
			
			private String simpleHttp(String url) {
				 // Creating HTTP client
		        HttpClient httpClient = new DefaultHttpClient();
		        // Creating HTTP Post
		        HttpPost httpPost = new HttpPost(url);
		 
		        // Building post parameters
		        // key and value pair
		        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
		        nameValuePair.add(new BasicNameValuePair("email", "user@gmail.com"));
		        nameValuePair.add(new BasicNameValuePair("message",
		                "Hi, trying Android HTTP post!"));
		        
		        String r = "no response";
		 
		        // Url Encoding the POST parameters
		        try {
		            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
		        } catch (UnsupportedEncodingException e) {
		            // writing error to Log
		            e.printStackTrace();
		        }
		        
		        // Making HTTP Request
		        try {
		            HttpResponse response = httpClient.execute(httpPost);
		 
		            // writing response to log
		            //Log.d("Http Response:", response.toString());
		            r = "Https Response:" + response.toString();
		        } catch (ClientProtocolException e) {
		            // writing exception to log
		            e.printStackTrace();
		            r = e.getMessage();
		        } catch (IOException e) {
		            // writing exception to log
		            e.printStackTrace();
		            r = e.getMessage();
		 
		        }
		        return r;
			}
	 }
	
	
}
