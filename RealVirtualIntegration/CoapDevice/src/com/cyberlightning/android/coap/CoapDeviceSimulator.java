package com.cyberlightning.android.coap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.text.method.ScrollingMovementMethod;
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
	// Requests to other activities
	private static final int REQ_ENABLE_BT = 0;
	private static final int REQ_DEVICE_ACT = 1;
	private static final int NO_DEVICE = -1;
	
	
	
	private boolean isBleSupported = true;
	private boolean isInitialized = false;
	private boolean isScanning = false;
	
	private int numDevs = 0;
	private int mConnIndex = NO_DEVICE;
	
	private BluetoothAdapter btAdapter = null;
	private BluetoothDevice btDevice = null;
	private BluetoothLeService btLeService = null;
	private CoapSocket coapSocket;
	
	private HashMap<String,NetworkDevice> foundDevices = new HashMap<String,NetworkDevice>();
	private IntentFilter intentFilter;
	private List<BleDeviceInfo> deviceInfoList;
	private static CoapDeviceSimulator mThis = null;
	
	private SensorListener sensorListener;
	private ServiceListener serviceListener = new ServiceListener();
	private ServiceResolver serviceResolver = new ServiceResolver();
	private String [] deviceFilter = null;
	
	public Button showButton;
	public TextView receivedMessages;
	public TextView sendMessages;
	
	// Tags to store saved instance state of this activity
	private static final String STATE_RECEIVED_MESSAGES = "StateReceivedMessages";
	private static final String STATE_SEND_MESSAGES = "StateSendMessages"; 
	
	final Handler mHandler = new Handler() { 

	     public void handleMessage(Message msg) { 
	    	 showMessage(msg);
	     } 
	 }; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_coap_device_simulator);
		this.receivedMessages = (TextView)findViewById(R.id.inboundMessagesDisplay);
		this.sendMessages = (TextView)findViewById(R.id.outboundMessagesDisplay);
		
		// Use this check to determine whether BLE is supported on the device. Then
	    // you can selectively disable BLE-related features.
	    if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
	      Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_LONG).show();
	      this.isBleSupported = false;
	    }
	    
	    // Initialize device list container and device filter
	    this.deviceInfoList = new ArrayList<BleDeviceInfo>();
	    this.deviceFilter = this.getResources().getStringArray(R.array.device_filter);
	    
	    // Register the BroadcastReceiver
	    this.intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
	    this.intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
	    this.intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		
		if (savedInstanceState != null) {
			// TODO restore states for relevant items
			this.sendMessages.setText(savedInstanceState.getString(STATE_SEND_MESSAGES));
			this.receivedMessages.setText(savedInstanceState.getString(STATE_RECEIVED_MESSAGES));
			
		}
		else {
			// TODO Or initialize UI here
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
			this.serviceListener.startDiscovery();
		}
		this.receivedMessages.setMovementMethod(new ScrollingMovementMethod());
		this.sendMessages.setMovementMethod(new ScrollingMovementMethod());
	}
	
	@Override
	public void onSaveInstanceState(Bundle saveState) {
		// TODO save states of relevant objects here
		saveState.putString(STATE_RECEIVED_MESSAGES, this.receivedMessages.getText().toString());
		saveState.putString(STATE_SEND_MESSAGES, this.sendMessages.getText().toString());
		
		super.onSaveInstanceState(saveState);
	}
	
	private void showMessage(Message msg) { //shows messy, clean up needed
		
		switch (msg.what) {
		
			case RomMemory.INBOUND_MESSAGE:
			
				//MessageEvent messageEvent = (MessageEvent) msg.obj;
				String content = msg.obj.toString();
				
				if (content.contains("[GPS]")) {
					PackageManager pm = this.getPackageManager();
					boolean hasGps = pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
					this.showToast("Starting location manager - System has GPS: " + hasGps);
					
					ISensorListener listener = this.sensorListener;		
					
					if (content.contains("high")) {
						listener.toggleGps(true, hasGps);
						
					}else {
						listener.toggleGps(false, hasGps);
					}
				}
				
				if (receivedMessages.getLineCount() < 20) {
					receivedMessages.append("Message Received : " + content  + "\n");
				} else {
					receivedMessages.setText("Message Received : " + content  + "\n");
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
			
				if (sendMessages.getLineCount() > 20) this.sendMessages.setText("");
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
			
			if(!foundDevices.containsKey(_nsdServiceInfo.getServiceName())) {
        		foundDevices.put(_nsdServiceInfo.getServiceName(), new NetworkDevice(_nsdServiceInfo.getHost(),_nsdServiceInfo.getPort(),_nsdServiceInfo.getServiceName(),_nsdServiceInfo.getServiceType()));
        		if (coapSocket == null) openSocket();
        		startSensorListener();
        		
        			//TODO remove test only
				
        		
        		
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
			
		}

		@Override
		public void onDiscoveryStopped(String serviceType) {
			 Log.i(TAG, "Discovery stopped: " + serviceType);
			
		}

		@Override
		public void onServiceFound(NsdServiceInfo serviceInfo) {
			// A service was found!  Do something with it.
            Log.d(TAG, "Service discovery success" + serviceInfo);
            
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
	
	private void startScan() {
	    // Start device discovery
	    if (this.isBleSupported) {
	      this.numDevs = 0;
	      this.deviceInfoList.clear();
	      //mScanView.notifyDataSetChanged(); //TODO need ScanView?
	      this.scanLeDevice(true);
	      /*mScanView.updateGui(this.isScanning); //TODO need ScanView?
	      if (!this.isScanning) {
	        setError("Device discovery start failed");
	        setBusy(false);
	      }*/
	    } else {
	     // setError("BLE not supported on this device"); //TODO need ScanView?
	    }

	 }
	 private void startDeviceActivity() {
		    /*mDeviceIntent = new Intent(this, DeviceActivity.class);
		    mDeviceIntent.putExtra(DeviceActivity.EXTRA_DEVICE, mBluetoothDevice);
		    startActivityForResult(mDeviceIntent, REQ_DEVICE_ACT);*/
		    
	}
	
	 void setError(String txt) {
		    //mScanView.setError(txt); //TODO need ScanView?
		 Toast.makeText(this, txt, Toast.LENGTH_LONG).show(); //temporary solution for debugging
	}
	 private void stopDeviceActivity() {
		    finishActivity(REQ_DEVICE_ACT);
	 }
	
	private boolean scanLeDevice(boolean enable) {
		    if (enable) {
		      this.isScanning = this.btAdapter.startLeScan(leScanCallback);
		    } else {
		      this.isScanning = false;
		      this.btAdapter.stopLeScan(leScanCallback);
		    }
		    return this.isScanning;
		  }
	
	private boolean checkDeviceFilter(BluetoothDevice device) {
		  	int  n = this.deviceFilter.length;
		  	if (n > 0) {
		  		boolean found = false;
		  		for (int i=0; i<n && !found; i++) {
		  			found = device.getName().equals(this.deviceFilter[i]);
		  		}
		  		return found;
		  	} else
		  		// Allow all devices if the device filter is empty
		  		return true;
	}
	
	private boolean deviceInfoExists(String address) {
	    for (int i = 0; i < this.deviceInfoList.size(); i++) {
	      if (this.deviceInfoList.get(i).getBluetoothDevice().getAddress().equals(address)) {
	        return true;
	      }
	    }
	    return false;
	}
	
	private BleDeviceInfo createDeviceInfo(BluetoothDevice device, int rssi) {
		    BleDeviceInfo deviceInfo = new BleDeviceInfo(device, rssi);

		    return deviceInfo;
	}
	
	private void addDevice(BleDeviceInfo device) {
	    this.numDevs++;
	    this.deviceInfoList.add(device);
	    /*mScanView.notifyDataSetChanged(); //TODO need ScanView?
	    if (this.numDevs > 1)
	      mScanView.setStatus(this.numDevs + " devices");
	    else
	      mScanView.setStatus("1 device");*/
	}
	
	private BleDeviceInfo findDeviceInfo(BluetoothDevice device) {
	    for (int i = 0; i < this.deviceInfoList.size(); i++) {
	      if (this.deviceInfoList.get(i).getBluetoothDevice().getAddress().equals(device.getAddress())) {
	        return this.deviceInfoList.get(i);
	      }
	    }
	    return null;
	 }
	// Device scan callback.
	// NB! Nexus 4 and Nexus 7 (2012) only provide one scan result per scan
	private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {

	    public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
	      runOnUiThread(new Runnable() {
	        public void run() {
	        	// Filter devices
	        	if (checkDeviceFilter(device)) {
	        		if (!deviceInfoExists(device.getAddress())) {
	        			// New device
	        			BleDeviceInfo deviceInfo = createDeviceInfo(device, rssi);
	        			addDevice(deviceInfo);
	        		} else {
	        			// Already in list, update RSSI info
	        			BleDeviceInfo deviceInfo = findDeviceInfo(device);
	        			deviceInfo.updateRssi(rssi);
	        			//mScanView.notifyDataSetChanged(); //TODO need ScanView?
	        		}
	        	}
	        }

	      });
	    }
	  };
	  
	  public void onScanViewReady(View view) {
		    // Initial state of widgets
		   // updateGuiState(); TODO do GUIevents

		    if (!this.isInitialized) {
		      // Broadcast receiver
		      registerReceiver(broadcastReceived, this.intentFilter);

		      if (this.btAdapter.isEnabled()) {
		        // Start straight away
		        startBluetoothLeService();
		      } else {
		        // Request BT adapter to be turned on
		        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		        startActivityForResult(enableIntent, REQ_ENABLE_BT);
		      }
		      this.isInitialized = true;
		    } else {
		      //mScanView.notifyDataSetChanged(); //TODO ScanView needed?
		    }
	  }
	  
	  private void startBluetoothLeService() {
		    boolean f;

		    Intent bindIntent = new Intent(this, BluetoothLeService.class);
		    startService(bindIntent);
		    f = bindService(bindIntent, btServiceConnector, Context.BIND_AUTO_CREATE);
		    if (f)
		      Log.d(TAG, "BluetoothLeService - success");
		    else {
		      CustomToast.middleBottom(this, "Bind to BluetoothLeService failed");
		      finish();
		    }
		  }
	  

	  // Code to manage Service life cycle.
	  private final ServiceConnection btServiceConnector = new ServiceConnection() {

	    public void onServiceConnected(ComponentName componentName, IBinder service) {
	    	btLeService = ((BluetoothLeService.LocalBinder) service).getService();
	      if (!btLeService.initialize()) {
	        Log.e(TAG, "Unable to initialize BluetoothLeService");
	        finish();
	        return;
	      }
	      final int n = btLeService.numConnectedDevices();
	      if (n > 0) {
	        runOnUiThread(new Runnable() {
	          public void run() {
	            mThis.setError("Multiple connections!");
	          }
	        });
	      } else {
	        startScan();
	        Log.i(TAG, "BluetoothLeService connected");
	      }
	    }

	    public void onServiceDisconnected(ComponentName componentName) {
	    	btLeService = null;
	      Log.i(TAG, "BluetoothLeService disconnected");
	    }
	  };
	  // ////////////////////////////////////////////////////////////////////////////////////////////////
	  //
	  // Broadcasted actions from Bluetooth adapter and BluetoothLeService
	  //
	  private BroadcastReceiver broadcastReceived = new BroadcastReceiver() {
	  	
		  @Override
	  	public void onReceive(Context context, Intent intent) {
	  		final String action = intent.getAction();
	  		
	  		if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
	  			// Bluetooth adapter state change
	  			switch (btAdapter.getState()) {
	  			case BluetoothAdapter.STATE_ON:
	  				mConnIndex = NO_DEVICE;
	  				startBluetoothLeService();
	  				break;
	  			case BluetoothAdapter.STATE_OFF:
	  	      Toast.makeText(context, R.string.app_closing, Toast.LENGTH_LONG).show();
	  				finish();
	  				break;
	  			default:
	  				Log.w(TAG, "Action STATE CHANGED not processed ");
	  				break;
	  			}

	  			 // updateGuiState(); TODO do GUIevents
	  		} else if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
	  			// GATT connect
	  			int status = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS, BluetoothGatt.GATT_FAILURE);
	  			if (status == BluetoothGatt.GATT_SUCCESS) {
	  				//setBusy(false); //TODO need ScanView?
	  				startDeviceActivity();
	  			} else //TODO need ScanView?
	  				setError("Connect failed. Status: " + status);
	  		} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
	  			// GATT disconnect
	  			int status = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS, BluetoothGatt.GATT_FAILURE);
	  			stopDeviceActivity();
	  			if (status == BluetoothGatt.GATT_SUCCESS) {
	  				//setBusy(false); //TODO need ScanView?
	  				//mScanView.setStatus(btDevice.getName() + " disconnected", STATUS_DURATION); //TODO need ScanView?
	  			} /*else {  //TODO need ScanView?
	  				setError("Disconnect failed. Status: " + status);  				
	  			}*/
	  			mConnIndex = NO_DEVICE;
	  			btLeService.close();
	  		} else {
	  			Log.w(TAG,"Unknown action: " + action);
	  		}

	  	}
	  
	  };
}
