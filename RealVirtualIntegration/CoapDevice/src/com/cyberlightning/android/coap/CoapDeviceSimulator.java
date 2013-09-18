package com.cyberlightning.android.coap;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdManager.DiscoveryListener;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;

public class CoapDeviceSimulator extends Activity implements Observer {

	
	protected static final String TAG = null;
	private ServiceListener serviceListener = new ServiceListener();
	private CoapSocket coapSocket;
	private ArrayList<NetworkDevice> devices = new ArrayList<NetworkDevice>();
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_coap_device_simulator);
		this.serviceListener.startDiscovery();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.coap_device_simulator, menu);
		return true;
	}
	
	private void openSocket() {
		this.coapSocket = new CoapSocket();
		coapSocket.addObserver(this);
		Thread thread = new Thread(coapSocket);
		thread.start();
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {
		if (arg1 instanceof DatagramPacket) {
			this.decodePacket((DatagramPacket) arg1);
           //TODO
        }
	}
	
	private void decodePacket (DatagramPacket _packet) {
		//TODO parse
	}
	
	private void encodePacket(String _payload, int _receiver) {
	
		
	}
	
	private class ServiceListener implements DiscoveryListener {

		
		//http://www.iana.org/assignments/service-names-port-numbers/service-names-port-numbers.xml
	
		private static final String SERVICE_NAME = "coap";
		private String serviceName;
		private NsdManager _NsdManager;
		
		
		public ServiceListener() {
			this(SERVICE_NAME);
		}
		
		public ServiceListener (String _serviceName ){
			this.serviceName = _serviceName;
		}
		
		public void startDiscovery() {
			this._NsdManager = ((NsdManager) getSystemService(NSD_SERVICE));
			_NsdManager.discoverServices(this.serviceName, NsdManager.PROTOCOL_DNS_SD, this);
		}
		

		@Override
		public void onDiscoveryStarted(String serviceType) {
			Log.d(TAG, "Service discovery started");
			
		}

		@Override
		public void onDiscoveryStopped(String serviceType) {
			 Log.i(TAG, "Discovery stopped: " + serviceType);
			
		}

		@Override
		public void onServiceFound(NsdServiceInfo serviceInfo) {
			// A service was found!  Do something with it.
            Log.d(TAG, "Service discovery success" + serviceInfo);
            if (!serviceInfo.getServiceType().equals(PersistentMemory.getInstance().SERVICE_TYPE)) {
                // Service type is the string containing the protocol and
                // transport layer for this service.
                Log.d(TAG, "Unknown Service Type: " + serviceInfo.getServiceType());
            } else if (serviceInfo.getServiceName().equals(SERVICE_NAME)) {
                // The name of the service tells the user what they'd be
                // connecting to. It could be "Bob's Chat App".
                Log.d(TAG, "Same machine: " + SERVICE_NAME);
            } else if (serviceInfo.getServiceName().contains(SERVICE_NAME)){
               // mNsdManager.resolveService(service, mResolveListener);
            	devices.add(new NetworkDevice(serviceInfo.getHost(),serviceInfo.getPort(),serviceInfo.getServiceName(),serviceInfo.getServiceType()));
            	openSocket();
            	
            
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
