package com.cyberlightning.android.coap.service;



import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.UUID;

import com.cyberlightning.android.coap.Application;
import com.cyberlightning.android.coap.StaticResources;
import com.cyberlightning.android.coapclient.R;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class CoapService extends Service {


  
    /** Keeps track of all current registered clients. */
    ArrayList<Messenger> registeredClients = new ArrayList<Messenger>();
    ArrayList<String> sendBuffer = new ArrayList<String>();
    /** Holds last value set by a client. */
    int mValue = 0;
    private DatagramSocket socket;
  
    static int NOTIFICATION_ID;

    /**
     * Command to the service to register a client, receiving callbacks
     * from the service.  The Message's replyTo field must be a Messenger of
     * the client where callbacks should be sent.
     */
    public static final int MSG_REGISTER_CLIENT = 1;

    /**
     * Command to the service to unregister a client, ot stop receiving callbacks
     * from the service.  The Message's replyTo field must be a Messenger of
     * the client as previously given with MSG_REGISTER_CLIENT.
     */
    public static final int MSG_UNREGISTER_CLIENT = 2;

    /**
     * Command to service to set a new value.  This can be sent to the
     * service to supply a new value, and will be sent by the service to
     * any registered clients with the new value.
     */
    public static final int MSG_BROADCAST = 3;
    public static final int SEND_TO_WEBSERVER = 4;
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger messenger = new Messenger(new IncomingHandler());

    @Override
    public void onCreate() {    
        NOTIFICATION_ID = UUID.randomUUID().hashCode(); 
        openConnectionToWebServer();
        showNotification(R.string.app_name,R.string.connection_service_started_notification);
 
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
    	((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancel(R.string.connection_service_started_notification);

        // Tell the user we stopped.
        Toast.makeText(this, R.string.connection_service_started_notification, Toast.LENGTH_SHORT).show();
     
    }

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
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
    	
    	Message message = new Message();
    	message.obj = (Object) _packet;
    	
    	try {
			messenger.send(message);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    private void showNotification (int _title, int _content) {
    	
    	NotificationCompat.Builder mBuilder =new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_launcher).setContentTitle(this.getString(_title)).setContentText(this.getString(_content));
    	   
    	//TODO Creates an explicit intent for an Activity in your app
    	Intent resultIntent = new Intent(this, Application.class);
    	    
    	TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
    	stackBuilder.addParentStack(Application.class); 
    	stackBuilder.addNextIntent(resultIntent);
    	    
    	PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
    	mBuilder.setContentIntent(resultPendingIntent);

    	((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, mBuilder.build());
    }
    
    
    /**
     * Handler of incoming messages from clients.
     */ 
    class IncomingHandler extends Handler {
        
    	@Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                	registeredClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                	registeredClients.remove(msg.replyTo);
                    break;
                case MSG_BROADCAST:
                	for (int i = 0 ; i < registeredClients.size(); i++) {
                		try {
							registeredClients.get(i).send(msg);
						} catch (RemoteException e) {
							registeredClients.remove(i);
						}
                	}
                	break;
                case SEND_TO_WEBSERVER:
                	sendBuffer.add(msg.obj.toString());
                	break;
    
                default:
                    super.handleMessage(msg);
            }
        }
    }
   
}