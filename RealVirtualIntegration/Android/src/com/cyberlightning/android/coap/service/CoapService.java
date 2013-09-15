package com.cyberlightning.android.coap.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import com.cyberlightning.android.coap.Application;
import com.cyberlightning.android.coap.StaticResources;
import com.cyberlightning.android.coapclient.R;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

public class CoapService extends Service {


  
    
    ArrayList<String> sendBuffer = new ArrayList<String>();
    /** Holds last value set by a client. */
    int mValue = 0;
    private DatagramSocket socket;
    private static int NOTIFICATION_ID;
  
    

    
    public static final String MY_CUSTOM_ACTION = "com.ramesh.myservice.ACTION";
    private MyBinder<CoapService> binder;
    private String name = "Hi";

    @Override
    public void onCreate() {    
        NOTIFICATION_ID = UUID.randomUUID().hashCode(); 
        openConnectionToWebServer();
        //listenLocalCoapDevices();
        showNotification(R.string.app_name,R.string.service_started_notification);
    }
    
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//    return START_STICKY;
//    }


	@Override
	public IBinder onBind(Intent arg0) {
		binder = new MyBinder<CoapService>(this);
		return binder;
	}
   
    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
    	((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancel(R.string.service_started_notification);
        Toast.makeText(this, R.string.service_stopped_notification, Toast.LENGTH_SHORT).show();
    }

    
   
    private void listenLocalCoapDevices() {
    	
    	if (BluetoothAdapter.getDefaultAdapter() != null) {
    		
    	}
    }
    
    private void openConnectionToWebServer() {
		
		new Thread(new Runnable() { 
			
		    public void run()  {
				
				try {
					
					socket = new DatagramSocket(StaticResources.COAP_DEFAULT_PORT);
					byte[] receiveByte = new byte[512]; //512 for IPv6 networks?
					socket.connect(InetAddress.getByName(StaticResources.LOCALHOST), StaticResources.SERVER_UDP_PORT);
					DatagramPacket receivedPacket = new DatagramPacket(receiveByte, receiveByte.length);
			
					
					while(socket.isConnected()) {
						
						
						if (sendBuffer.size() > 0) {
		
							
							byte[] byteBuffer = new byte[sendBuffer.get(sendBuffer.size() - 1).getBytes().length]; //512 for IPv6 networks?
							DatagramPacket packet = new DatagramPacket(byteBuffer, byteBuffer.length,InetAddress.getByName(StaticResources.LOCALHOST), StaticResources.SERVER_UDP_PORT);
							packet.setData(byteBuffer);
							socket.send(packet);
							
							packet = null;
							byteBuffer = null;
							sendBuffer.remove(sendBuffer.size() - 1);

						}
						
						socket.receive(receivedPacket);
						if (receivedPacket.getSocketAddress() != null) {
							processReceivedPacket(receivedPacket);
							receivedPacket = null; //clear packet holder
						}
					}
					//TODO handle socket closed
					
				} catch(IOException e) {
					e.printStackTrace();
				} 
				
			}}).start();	
		
	}
    
    private void processReceivedPacket (DatagramPacket _packet){
    	
    	
    }

    private void showNotification (int _title, int _content) {
    	
    	NotificationCompat.Builder mBuilder =new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_launcher).setContentTitle(this.getString(_title)).setContentText(this.getString(_content));
    	   
    	Intent resultIntent = new Intent(this, Application.class);
    	    
    	TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
    	stackBuilder.addParentStack(Application.class); 
    	stackBuilder.addNextIntent(resultIntent);
    	    
    	PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
    	mBuilder.setContentIntent(resultPendingIntent);

    	((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, mBuilder.build());
    }
   
    
	public class MyBinder<T> extends Binder implements IMybinder {
		 private WeakReference<T> mService;
		 
		public MyBinder(T service) {
		 mService = new WeakReference<T>(service);
		 }
		 
		public T getService() {
		 return mService.get();
		 }
		 
		@Override
		 public String getName() {
		 
		Random r = new Random();
		 return "=> your lucky number is:" + r.nextInt(100);
		 }
		 
		@Override
		 public void sendMessage(Message _msg) {

		
			try {
				byte[] byteBuffer = _msg.obj.toString().getBytes("UTF8");
				DatagramPacket packet = new DatagramPacket(byteBuffer, byteBuffer.length);
				packet.setData(byteBuffer);
				socket.send(packet);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

		 }
		 }
		
		 
		 /** Interface setting the name and getting the name.**/
		 
		 public interface IMybinder
		 {
		 public String getName();
		 
		 public void sendMessage(Message _msg);
		 }

    
   
}