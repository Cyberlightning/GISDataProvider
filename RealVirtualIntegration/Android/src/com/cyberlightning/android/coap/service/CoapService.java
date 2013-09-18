package com.cyberlightning.android.coap.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.UUID;

import com.cyberlightning.android.coap.Application;
import com.cyberlightning.android.coap.resources.StaticResources;
import com.cyberlightning.android.coapclient.R;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdManager.DiscoveryListener;
import android.net.nsd.NsdManager.RegistrationListener;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class CoapService extends Service {


    protected static final String TAG = null;
	protected static final String SERVICE_TYPE = "asd";
	private DatagramSocket webServerSocket;
    private DatagramSocket coapServerSocket;
    private CoapServiceBinder<CoapService> coapBinder;
    private WifiP2pManager wifiManager;
    private Channel wifiChannel;
    private WifiListener wifiListener;
    private WifiP2pDeviceList availableWifiDevices;
	protected String mServiceName;
	private RegistrationListener mRegistrationListener;
	private NsdManager mNsdManager;
	private DiscoveryListener mDiscoveryListener;
	
    private static int NOTIFICATION_ID;
   

    @Override
    public void onCreate() {    
        NOTIFICATION_ID = UUID.randomUUID().hashCode(); 
        this.openCoapSocket();
        this.initializeDiscoveryListener();
        mNsdManager.discoverServices("coap", NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener); //http://www.iana.org/assignments/service-names-port-numbers/service-names-port-numbers.xml
        //this.listenForLocalCoapDevices();
        this.showNotification(R.string.app_name,R.string.service_started_notification);
    }
  
	@Override
	public IBinder onBind(Intent arg0) {
		this.coapBinder = new CoapServiceBinder<CoapService>(this);
		return this.coapBinder;
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
					
					webServerSocket = new DatagramSocket(StaticResources.COAP_DEFAULT_PORT);
					webServerSocket.setReceiveBufferSize(1024);
					byte[] receiveByte = new byte[1024]; //512 for IPv6 networks?
					//webServerSocket.connect(InetAddress.getByName(StaticResources.LOCALHOST), StaticResources.SERVER_UDP_PORT);
					DatagramPacket receivedPacket = new DatagramPacket(receiveByte, receiveByte.length);
			
					
					while(true) {
						
						webServerSocket.receive(receivedPacket);
						if (receivedPacket.getSocketAddress() != null) {
							processReceivedPacket(receivedPacket);
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
    public void initializeDiscoveryListener() {

        // Instantiate a new DiscoveryListener
    	 mDiscoveryListener = new NsdManager.DiscoveryListener() {

            //  Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found!  Do something with it.
                Log.d(TAG, "Service discovery success" + service);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    // Service type is the string containing the protocol and
                    // transport layer for this service.
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(mServiceName)) {
                    // The name of the service tells the user what they'd be
                    // connecting to. It could be "Bob's Chat App".
                    Log.d(TAG, "Same machine: " + mServiceName);
                } else if (service.getServiceName().contains("NsdChat")){
                   // mNsdManager.resolveService(service, mResolveListener);
                	String s = "";
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e(TAG, "service lost" + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }
    public void registerService(int port) {
        NsdServiceInfo serviceInfo  = new NsdServiceInfo();
        serviceInfo.setServiceName("NsdChat");
        serviceInfo.setServiceType("_http._tcp.");
        serviceInfo.setPort(port);

        mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);

        mNsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
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
    
    private void listenForLocalCoapDevices () {
    	
    	this.wifiManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
    	this.wifiChannel = wifiManager.initialize(this, getMainLooper(), null);
    	
    	this.wifiListener = new WifiListener();
    	
    	IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        
        registerReceiver(wifiListener, mIntentFilter);
        this.wifiListener.discoverConnectedPeers();
    }
    
    
    private void processReceivedPacket (DatagramPacket _packet){
    	//TODO processReceivedPacket
    	
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
    
    
	public class CoapServiceBinder<T> extends Binder implements ICoapServiceBinder {
		 private WeakReference<T> coapService;
		 
		public CoapServiceBinder(T service) {
			coapService = new WeakReference<T>(service);
		}
		 
		public T getService() {
		 return coapService.get();
		}
		 
		@Override
		 public String getName() {
			return null;
		}
		 
		 
		@Override
		 public void sendMessage(Message _msg) {
			
			//if (webServerSocket.isConnected()) {
				try {
					byte[] byteBuffer = _msg.obj.toString().getBytes("UTF8");
					//DatagramPacket packet = new DatagramPacket(byteBuffer, byteBuffer.length);
					DatagramPacket packet = new DatagramPacket(byteBuffer,byteBuffer.length,InetAddress.getByName(StaticResources.REMOTEHOST),StaticResources.SERVER_UDP_PORT);
					packet.setData(byteBuffer);
					webServerSocket.send(packet);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			//}
		 }
	}
		 
		 /** Interface setting the name and getting the name.**/
		 
	public interface ICoapServiceBinder {
		 
		public String getName();
		public void sendMessage(Message _msg);
	
	}
	
	class WifiListener extends BroadcastReceiver implements PeerListListener  {

		  public WifiListener() {
		       super();
		   }
		   
		   public void discoverConnectedPeers() {
			   wifiManager.discoverPeers( wifiChannel, new WifiP2pManager.ActionListener() {
			   	    
			   		@Override
			   	    public void onSuccess() {
			   	       System.out.println("Discover Peers Success");
			   	    }

			   	    @Override
			   	    public void onFailure(int reasonCode) {
			   	    	System.out.println("Discover Peers Failed");
			   	    }
			   	});
		   }
		   
		   @Override
		   public void onReceive(Context context, Intent intent) {
		       String action = intent.getAction();

		       if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
		    	   
		    	   int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
		           if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
		               System.out.println("Wifi enabled");
		           } else {
		        	   System.out.println("Wifi disabled");
		           }

		       } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
		    	   wifiManager.requestPeers(wifiChannel, this);
		       } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
		           // Respond to new connection or disconnections
		       } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
		           // Respond to this device's wifi state changing
		       }
		   }

		   @Override
		   public void onPeersAvailable(WifiP2pDeviceList peers) {
			   availableWifiDevices = peers;
			
		   }
	}
	

    
   
}