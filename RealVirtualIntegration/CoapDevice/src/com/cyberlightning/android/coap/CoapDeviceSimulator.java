package com.cyberlightning.android.coap;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import com.cyberlightning.android.coap.memory.RomMemory;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdManager.DiscoveryListener;
import android.net.nsd.NsdManager.ResolveListener;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Message;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class CoapDeviceSimulator extends Activity implements Observer {

	
	protected static final String TAG = null;
	private ServiceListener serviceListener = new ServiceListener();
	private ServiceResolver serviceResolver = new ServiceResolver();
	private CoapSocket coapSocket;
	private SensorListener sensorListener;
	private HashMap<String,NetworkDevice> foundDevices = new HashMap<String,NetworkDevice>();
	
	public TextView textDisplay;
	public Button showButton;
	private ArrayList<String> statusMessages = new ArrayList<String>();

	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_coap_device_simulator);
		this.serviceListener.startDiscovery();
		this.textDisplay = (TextView)findViewById(R.id.statusText);
		this.textDisplay.setText("TADAA!");
		this.showButton = (Button)findViewById(R.id.showButton);
		showButton.setOnClickListener(new OnClickListener()
		{
		public void onClick(View v)
		{
		showMessages();
		}
		                                });
		     
		
		
	}

	private void showMessages() {
		Iterator<String> i = statusMessages.iterator();
		  while (i.hasNext()) {
			  textDisplay.append(i.next());
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
		if (arg1 instanceof Message) {
				this.coapSocket.broadCastMessage((Message) arg1,foundDevices);
			}
		if (arg1 instanceof DatagramPacket) {
			this.decodePacket((DatagramPacket) arg1);
           //TODO
        }
	}
	
	private void decodePacket (DatagramPacket _packet) {
		
		statusMessages.add("packet send to " + _packet.getAddress().toString());
	}
	
	private class ServiceResolver implements ResolveListener {

		@Override
		public void onResolveFailed(NsdServiceInfo arg0, int arg1) {
			System.out.print(arg0.getServiceName() + " cannot be resolved");
		}

		@Override
		public void onServiceResolved(NsdServiceInfo _nsdServiceInfo) {
        	if(!foundDevices.containsKey(_nsdServiceInfo.getServiceName())) {
        		foundDevices.put(_nsdServiceInfo.getServiceName(), new NetworkDevice(_nsdServiceInfo.getHost(),_nsdServiceInfo.getPort(),_nsdServiceInfo.getServiceName(),_nsdServiceInfo.getServiceType()));
        		if (coapSocket == null) openSocket();
        		startSensorListener();
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
