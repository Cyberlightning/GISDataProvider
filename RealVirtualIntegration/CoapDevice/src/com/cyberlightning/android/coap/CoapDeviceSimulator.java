package com.cyberlightning.android.coap;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;






import org.json.JSONException;
import org.json.JSONObject;

import com.cyberlightning.android.coap.memory.RomMemory;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdManager.DiscoveryListener;
import android.net.nsd.NsdManager.ResolveListener;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CoapDeviceSimulator extends Activity implements Observer {

	
	protected static final String TAG = null;
	private ServiceListener serviceListener = new ServiceListener();
	private ServiceResolver serviceResolver = new ServiceResolver();
	private CoapSocket coapSocket;
	private SensorListener sensorListener;
	private HashMap<String,NetworkDevice> foundDevices = new HashMap<String,NetworkDevice>();
	
	public TextView receivedMessages;
	public TextView sendMessages;
	public Button showButton;
	private ArrayList<String> statusMessages = new ArrayList<String>();
	
	final Handler mHandler = new Handler() { 

	     public void handleMessage(Message msg) { 
	    	 showMessage(msg);
	     } 
	 }; 
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_coap_device_simulator);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		this.serviceListener.startDiscovery();
		this.receivedMessages = (TextView)findViewById(R.id.inboundMessagesDisplay);
		this.sendMessages = (TextView)findViewById(R.id.outboundMessagesDisplay);
	}
	
	private void showMessage(Message msg) { //shows messy, clean up needed
		
		switch (msg.what) {
		
			case RomMemory.INBOUND_MESSAGE:
			
				MessageEvent messageEvent = (MessageEvent) msg.obj;
				String content = messageEvent.getContent();
		
				if (content.contains("[GPS]")) {
					ISensorListener listener = this.sensorListener;		
					if (content.contains("high")) {
						listener.toggleGps(true);
					}else {
						listener.toggleGps(false);
					}
				}
				
				if (receivedMessages.getHeight() < 500) {
					receivedMessages.append(messageEvent.getSenderAddress() + " : " + content  + "\n");
				} else {
					receivedMessages.setText(messageEvent.getSenderAddress() + " : " + content  + "\n");
				}
				
			break;
			
			case RomMemory.OUTBOUND_MESSAGE:
			
				ICoapSocket socket = this.coapSocket;
				socket.broadCastMessage(msg,foundDevices); //throws networkonmainthread exception if not in st
				
				String deviceType = "";
				
				try {
					JSONObject jsonContent = new JSONObject(msg.obj.toString());
					deviceType = jsonContent.getJSONObject("device_properties").getString("type");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
				if (sendMessages.getHeight() > 500) this.sendMessages.setText("");
					Iterator<String> i = this.foundDevices.keySet().iterator();
					while (i.hasNext()) {
						NetworkDevice nd = this.foundDevices.get(i.next());
						this.sendMessages.append(deviceType + " -> " + nd.getAddress().getHostAddress() + "\n");
					}
			break;
		}
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.coap_device_simulator, menu);
		return true;
	}
	private void startSensorListener() {
		this.sensorListener = new SensorListener(this);
		this.sensorListener.addObserver(this);
		Thread t = new Thread(sensorListener);
		t.start();
	}
	
	private void openSocket() {
		this.coapSocket = new CoapSocket();
		this.coapSocket.addObserver(this);
		Thread thread = new Thread(coapSocket);
		thread.start();
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {
		
		mHandler.sendMessage((Message) arg1);
		
	}
	
	
	private void decodePacket (DatagramPacket _packet) {
		
		statusMessages.add("packet send to " + _packet.getAddress().toString());
	}
	
	private void showToast(String _message) {
	    	
	    	LayoutInflater inflater = getLayoutInflater();
	    	View layout = inflater.inflate(R.layout.toast_layout,(ViewGroup) findViewById(R.id.toast_layout_root));

	    	TextView text = (TextView) layout.findViewById(R.id.text);
	    	text.setText(_message);

	    	Toast toast = new Toast(getApplicationContext());
	    	toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
	    	toast.setDuration(Toast.LENGTH_LONG);
	    	toast.setView(layout);
	    	toast.show();
	    	
	}
	    
	
	private class ServiceResolver implements ResolveListener {

		@Override
		public void onResolveFailed(NsdServiceInfo arg0, int arg1) {
			System.out.print(arg0.getServiceName() + " cannot be resolved");
		}

		@Override
		public void onServiceResolved(NsdServiceInfo _nsdServiceInfo) {
			statusMessages.add("onServiceResolvedTriggered");
			if(!foundDevices.containsKey(_nsdServiceInfo.getServiceName())) {
        		foundDevices.put(_nsdServiceInfo.getServiceName(), new NetworkDevice(_nsdServiceInfo.getHost(),_nsdServiceInfo.getPort(),_nsdServiceInfo.getServiceName(),_nsdServiceInfo.getServiceType()));
        		if (coapSocket == null) openSocket();
        		startSensorListener();
        		
        		ISensorListener listener = sensorListener;		//TODO remove test only
				listener.toggleGps(true);
        		statusMessages.add("Service resolved \n");
        		
        	}
		}
	}
	
	private class ServiceListener implements DiscoveryListener {
		
		//http://www.iana.org/assignments/service-names-port-numbers/service-names-port-numbers.xml

		private String serviceName;
		private NsdManager _NsdManager;
		
		
		public ServiceListener() {
			this(RomMemory.DEFAULT_SERVICE_NAME);
		}
		
		public ServiceListener (String _serviceName ){
			this.serviceName = _serviceName;
		}
		
		public void startDiscovery() {
			this._NsdManager = ((NsdManager) getSystemService(NSD_SERVICE));
			this._NsdManager.discoverServices(RomMemory.DEFAULT_SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, this);

		}
		

		@Override
		public void onDiscoveryStarted(String serviceType) {
			Log.d(TAG, "Service discovery started");
			statusMessages.add("Service discovery started :" + serviceType );
		}

		@Override
		public void onDiscoveryStopped(String serviceType) {
			 Log.i(TAG, "Discovery stopped: " + serviceType);
			
		}

		@Override
		public void onServiceFound(NsdServiceInfo serviceInfo) {
			// A service was found!  Do something with it.
            Log.d(TAG, "Service discovery success" + serviceInfo);
            statusMessages.add("Service discovery success" + serviceInfo + "\n");
            if (!serviceInfo.getServiceType().equals(RomMemory.DEFAULT_SERVICE_TYPE)) {
                // Service type is the string containing the protocol and
                // transport layer for this service.
                Log.d(TAG, "Unknown Service Type: " + serviceInfo.getServiceType());
            } else if (serviceInfo.getServiceName().equals(this.serviceName)) {
                // The name of the service tells the user what they'd be
                // connecting to. It could be "Bob's Chat App".
                Log.d(TAG, "Same machine: " + this.serviceName);
//              
                this._NsdManager.resolveService(serviceInfo, serviceResolver);

            } else if (serviceInfo.getServiceName().contains(this.serviceName)){
               // mNsdManager.resolveService(service, mResolveListener);
            	this._NsdManager.resolveService(serviceInfo, serviceResolver);

            }
			
		}

		@Override
		public void onServiceLost(NsdServiceInfo serviceInfo) {
			// When the network service is no longer available.
            // Internal bookkeeping code goes here.
            Log.e(TAG, "service lost" + serviceInfo);
			
		}

		@Override
		public void onStartDiscoveryFailed(String serviceType, int errorCode) {
			Log.e(TAG, "Discovery failed: Error code:" + errorCode);
			_NsdManager.stopServiceDiscovery(this);
			
		}

		@Override
		public void onStopDiscoveryFailed(String serviceType, int errorCode) {
			Log.e(TAG, "Discovery failed: Error code:" + errorCode);
			_NsdManager.stopServiceDiscovery(this);
			
		}
		
	}

	

    

}
