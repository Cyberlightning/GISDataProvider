/** @author Tomi Sarni (tomi.sarni@cyberlightning.com)
 *  Copyright: Cyberlightning Ltd.
 *  
 */

package com.cyberlightning.android.coap.application;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.cyberlightning.android.coap.TI.utils.BleDeviceInfo;
import com.cyberlightning.android.coap.TI.utils.BluetoothLeService;
import com.cyberlightning.android.coap.TI.utils.SensorTag;
import com.cyberlightning.android.coapclient.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;


public class BaseStationUI extends Activity implements DialogInterface.OnClickListener,ServiceConnection, 
														GooglePlayServicesClient.ConnectionCallbacks,
														GooglePlayServicesClient.OnConnectionFailedListener,
														LocationListener	{	
	public final String TAG = "BaseStationUi";
	// This baseStation UUID
	public static final String uuid = UUID.randomUUID().toString();
	
	private final static int BT_SCAN_PERIOD = 5000; // Scan duration for bluetooth in ms.
	private boolean bluetoothLeAvailable = true;
	private boolean bluetoothLeIsEnabled = false;
	private BluetoothAdapter btAdapter = null;
	private BluetoothManager bluetoothManager = null;
	private boolean isScanning = false;
	private Handler btHandler = null;
	private BluetoothLeService btLeService = null;
	private List<BluetoothGattService> btServices;
	
	private List<BleDeviceInfo> deviceInfoList;
	private int numDevs = 0;
	
	private Messenger serviceMessenger = null;
	private ServiceConnection serviceConnection = this;
	private static TextView connectedClientsDisplay;
	private static TextView trafficDataDisplay;
	
	private boolean hasServiceStopped = false;
	private boolean isBound = false;
	
	private final int NOTIFICATION_DELAY = 12000;
	private final Messenger messenger = new Messenger(new IncomingMessageHandler());
	
	// Tags to store saved instance state of this activity
	static final String STATE_IS_BOUND = "StateIsBound";
	static final String STATE_SERVICE_STOPPED = "StateHasServiceStopped";
	static final String STATE_CONNECTED_CLIENTS_DISPLAY = "StateConnectedClientsDisplay";
	static final String STATE_TRAFFIC_DATA_DISPLAY = "StateTrafficDataDisplay";
	
	// Tags for activity requests
	private static final int REQUEST_ENABLE_BT = 0;
	private static final int WIFI_AP_REQUEST = 1;
	
	// Intent filter for gatt
	private IntentFilter intentFilter;
	
	// Sensor values
	public double ACC_X_VALUE = 0;
	public double ACC_Y_VALUE = 0;
	public double ACC_Z_VALUE = 0;
	public double IRT_TEMP_VALUE = 0;
	
	// Sensor updater
	Handler mHandler = new Handler();
	
	// Location specific variables
	private LocationClient locationClient_;
	private LocationRequest mLocationRequest;
	private double latitude = 0;
	private double longitude = 0;
	private final int LOCATION_REQUEST_INTERVAL = 10000;
	private final int LOCATION_FASTEST_INTERVAL = 5000;
	

	// Suppress warnings of too low API level because of bluetooth. If service is not found we just disable all BT related services.
	@SuppressLint("NewApi")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setup layout before checking for savedInstanceState
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
    	this.setContentView(R.layout.activity_coapclient);
        
        if (savedInstanceState != null) {        	
        	// restore values from save state
        	this.isBound = savedInstanceState.getBoolean(STATE_IS_BOUND);
        	this.hasServiceStopped = savedInstanceState.getBoolean(STATE_SERVICE_STOPPED);
        	
        	connectedClientsDisplay = (TextView) findViewById(R.id.connectedDisplay);
        	connectedClientsDisplay.setText(savedInstanceState.getString(STATE_CONNECTED_CLIENTS_DISPLAY));
        	
            trafficDataDisplay = (TextView) findViewById(R.id.trafficDisplay);
            trafficDataDisplay.setText(savedInstanceState.getString(STATE_TRAFFIC_DATA_DISPLAY));
            
        }
        else {	    
	        this.initNetworkConnection();	        
	    	this.initializeBluetooth();
	    	// Get the location manager
		    if (servicesConnected())
		    {
			    locationClient_ = new LocationClient(this, this, this);
		        // Create the LocationRequest object
		        mLocationRequest = LocationRequest.create();
		        // Use high accuracy
		        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		        // Set the update interval
		        mLocationRequest.setInterval(LOCATION_REQUEST_INTERVAL);
		        // Set the fastest update interval
		        mLocationRequest.setFastestInterval(LOCATION_FASTEST_INTERVAL);
		    }
	    	
	    	connectedClientsDisplay = (TextView) findViewById(R.id.connectedDisplay);
	        trafficDataDisplay = (TextView) findViewById(R.id.trafficDisplay);
	        deviceInfoList = new ArrayList<BleDeviceInfo>();
        }
        connectedClientsDisplay.setMovementMethod(new ScrollingMovementMethod());
        trafficDataDisplay.setMovementMethod(new ScrollingMovementMethod());
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if (locationClient_ != null) {
			locationClient_.connect();
			Log.i(TAG, "Location client connect!");
		}
			
	}
	
	@Override
	protected void onStop() {
		// If the client is connected
        if (locationClient_.isConnected()) {
            /*
             * Remove location updates for a listener.
             * The current Activity is the listener, so
             * the argument is "this".
             */
            locationClient_.removeLocationUpdates(this);
        }
        /*
         * After disconnect() is called, the client is
         * considered "dead".
         */
        locationClient_.disconnect();
        super.onStop();
	}
	
    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) 
        {
            // In debug mode, log the status
        	System.out.println("Google Play services is available.");
            // Continue
            return true;
        // Google Play services was not available for some reason
        } 
        else 
        {
            // Get the error code
            System.out.println("Some error occured when querying google play services!");
            return false;
        }
    }
    
	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		connectedClientsDisplay.append("gps connection failed!");
	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		connectedClientsDisplay.append("gps connected!\n");
		locationClient_.requestLocationUpdates(mLocationRequest, this);
		
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		connectedClientsDisplay.append("gps disconnected!\n");
		
	}
	
	@Override
	public void onLocationChanged(Location location) {
		latitude = location.getLatitude();
		longitude = location.getLongitude();
		// we got updated location. Shutdown service because basestation is most likely a stationary service provider?
		// If we want to continue recieving updates just remove two lines declared below.
		locationClient_.removeLocationUpdates(this);
		locationClient_.disconnect();
	}

	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case WIFI_AP_REQUEST:
			initNetworkConnection();
			break;		
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle saveState) {
		// Save all the variables we need
		saveState.putBoolean(STATE_IS_BOUND, this.isBound);
		saveState.putBoolean(STATE_SERVICE_STOPPED, hasServiceStopped);
		saveState.putString(STATE_CONNECTED_CLIENTS_DISPLAY, connectedClientsDisplay.getText().toString());
		saveState.putString(STATE_TRAFFIC_DATA_DISPLAY, trafficDataDisplay.getText().toString());		
		
		// Call super so we can get the saved data bundle to onCreate method.
		super.onSaveInstanceState(saveState);
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.co_apclient, menu);
        if (!isScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
        }
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
            	if (bluetoothLeIsEnabled) {
            		deviceInfoList.clear();
            		numDevs = 0;
            		scanLeDevice(true);
                }
            	else {
            		if (btAdapter != null || btAdapter.isEnabled()) {
            			// Bluetooth enabled variable needs to be set again if user enabled BT service
            			// after application start.
            			bluetoothLeIsEnabled = true;
            			btHandler = new Handler();
            		}
            	}
                break;
            case R.id.menu_stop:
            	if (bluetoothLeIsEnabled) {
            		scanLeDevice(false);
                break;
            	}
        }
        return true;
    }
	
	@Override
	public void onResume() {
		super.onResume();
	}
    
    @Override
    public void onPause() { 
    	super.onPause();
    }
  
    @Override
    public void onBackPressed() {
    	this.showExitDialog();
    }
  
    @Override
    protected void onDestroy() {
    	super.onDestroy(); 
    	//doUnbindService();  needed?
    }
    
	/** Get application context */
    public Context getContext() {
		return this.getApplicationContext();
	}

    /** Detects connection preferences required to run the application */
    private void initNetworkConnection() {  //create code for Wifi-hotspot
    	
    	boolean hasHotSpot = false; //set to true to enable debugging. Hotspot disables wifi discovery thus failing this method.
      	boolean hasInternet = false; 
     	
      	WifiManager wifiManager = (WifiManager)getBaseContext().getSystemService(Context.WIFI_SERVICE);
      
      	Method[] wmMethods = wifiManager.getClass().getDeclaredMethods();
      		for(Method method: wmMethods){
      			if(method.getName().equals("isWifiApEnabled")) {
      				try {
      					 hasHotSpot = (Boolean) method.invoke(wifiManager);
      				} catch (IllegalArgumentException e) {
      					e.printStackTrace();
      				} catch (IllegalAccessException e) {
      					e.printStackTrace();
      				} catch (InvocationTargetException e) {
      					e.printStackTrace();
      				}
      			}
      		}
    
    	if (this.haveNetworkConnection()) hasInternet = true;
    	if (!hasHotSpot) {
    		this.showToast(getString(R.string.main_no_wifi_notification));
    		Intent settingsIntent = new Intent();
    		settingsIntent.setClassName("com.android.settings", "com.android.settings.TetherSettings");
    		this.startActivityForResult(settingsIntent, WIFI_AP_REQUEST);
    	} else {
    		
    		if (hasInternet) {
    			this.doBindService();
    			//bindService(new Intent(this, BaseStationService.class), this.serviceConnection, Service.BIND_AUTO_CREATE);	
    		} else {
    			Toast.makeText(this, R.string.main_no_connection_notification, this.NOTIFICATION_DELAY).show();
        		this.finish();	
    		}

    	}
    }
    
    /** Detects connection preferences required to run the application */
    private boolean haveNetworkConnection() {
        
    	final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService (Context.CONNECTIVITY_SERVICE);
           if (connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isAvailable() &&    connectivityManager.getActiveNetworkInfo().isConnected()) {
        	   return true;
           } else {
                 this.showToast(getText(R.string.main_no_connection_notification).toString());
               return false;
           }
    }
    
    /** Display custom Toast
     * @param _message : A string message to be displayed on Toast
     * */
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
    
    /** Display exit dialog with options to leave service running */
    private void showExitDialog() { 
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setIcon(R.drawable.cyber_icon);
    	builder.setInverseBackgroundForced(true);
    	
    	if(this.hasServiceStopped) {
    		builder.setMessage(getString(R.string.dialog_exit_program_notification_no_service));
    		
    	} else {
    	
        	builder.setNeutralButton(R.string.dialog_no_button, this);
    		builder.setMessage(getString(R.string.dialog_exit_program_notification));
    	}

    	builder.setCancelable(false);
    	builder.setPositiveButton(getString(R.string.dialog_cancel_button),this);
    	builder.setNegativeButton(getString(R.string.dialog_yes_button),this); 
    	builder.setTitle(R.string.dialog_title_exit);
    	AlertDialog alert = builder.create();
    	alert.show();
    }

	/**
     * Send data to the service
     * @param _msg The data to send
     */
    private void sendMessageToService(Object _msg) {
    	
    	if (this.isBound && this.serviceMessenger != null) {
            
    		try {
            	Message msg = Message.obtain(null, BaseStationService.MSG_UI_EVENT, _msg);
            	msg.replyTo = messenger;
            	this.serviceMessenger.send(msg);
            } catch (RemoteException e) {
                //TODO handle exception
            }         
        }
    }
    
    /** Bind this Activity to BaseStationService */
    private void doBindService() {
    	if (!this.isBound) {
    		this.isBound = true;
    		Log.d("BaseStationUI", "doBindSercive!");
    		bindService(new Intent(this, BaseStationService.class), this.serviceConnection, Context.BIND_AUTO_CREATE);
    	}
    }
    
    /** Unbind this Activity from BaseStationService */
	private void doUnbindService() {
         if (this.isBound) {
                 // If we have received the service, and hence registered with it, then now is the time to unregister.
                 if (this.serviceMessenger != null) {
                         try {
                                 Message msg = Message.obtain(null, BaseStationService.MSG_UNREGISTER_CLIENT);
                                 msg.replyTo = messenger;
                                 serviceMessenger.send(msg);
                         } catch (RemoteException e) {
                                 // There is nothing special we need to do if the service has crashed.
                         }
                 }
                 // Detach our existing connection.
                 unbindService(this.serviceConnection);
                 this.isBound = false;
               
         }
	}


	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		this.serviceMessenger = new Messenger(service);
        
		try {
        	Message msg = Message.obtain(null, BaseStationService.MSG_REGISTER_CLIENT);
            msg.replyTo = this.messenger;
            this. serviceMessenger.send(msg);
        } 
        catch (RemoteException e) {
                // TODO handle exception
        } 
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		this.serviceMessenger = null;
	}
	
	@Override
	public void onClick(DialogInterface dialog, int action) {
		
		switch(action) {
			
			case Dialog.BUTTON_POSITIVE: dialog.cancel();
			break;
			
			case Dialog.BUTTON_NEGATIVE: this.finish();
			break;

			case Dialog.BUTTON_NEUTRAL:  this.finish();
									 	 this.doUnbindService(); //TODO check 
			break;

		}	
	}
	
	/** Handle incoming messages from BaseStation service process */
	private static class IncomingMessageHandler extends Handler {  
		
		@Override
        public void handleMessage(Message msg) {
			 
			 MessageEvent messageEvent = (MessageEvent) msg.obj;
			 
			 switch (msg.what) {
	     		case BaseStationService.MSG_RECEIVED_FROM_COAPDEVICE:
	     			if (messageEvent.isNewSender()) {
	     				connectedClientsDisplay.append(messageEvent.getSenderAddress());
	     			}
	     			if (trafficDataDisplay.getHeight() < 500) {
	     				trafficDataDisplay.append(messageEvent.getSenderAddress() + " -> " + messageEvent.getTargetAddress() + "(" + messageEvent.getContent().getBytes().length + " B)" + "\n");
	     			} else {
	     				trafficDataDisplay.setText(messageEvent.getSenderAddress() + " -> " + messageEvent.getTargetAddress() + "(" + messageEvent.getContent().getBytes().length + " B)" + "\n");
	     			}
	     			
	     			break;
	     		case BaseStationService.MSG_RECEIVED_FROM_WEBSERVICE:
	     			if (trafficDataDisplay.getHeight() < 500) {
	     				trafficDataDisplay.append(messageEvent.getSenderAddress() + " -> " + messageEvent.getTargetAddress() + "(" + messageEvent.getContent().getBytes().length + " B)" + "\n");
	     			} else {
	     				trafficDataDisplay.setText(messageEvent.getSenderAddress() + " -> " + messageEvent.getTargetAddress() + "(" + messageEvent.getContent().getBytes().length + " B)" + "\n");
	     			}
	     			break;
	     		default: super.handleMessage(msg);break;
	     		
			}
        }
	}
	//-----------------------------------------------------------------
	// # Bluetooth Low Energy related code segment starts here
	//-----------------------------------------------------------------
	
	/** Initialize bluetooth LE service if available.
	 * @return Status of service availability */
	private boolean initializeBluetooth() {
		// Use this check to determine whether BLE is supported on the device. Then
    	// you can selectively disable BLE-related features.
    	if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
    	    Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
    	    bluetoothLeAvailable = false;
    	}
		if (bluetoothLeAvailable) {
			// Initializes Bluetooth adapter.
			bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			btAdapter = bluetoothManager.getAdapter();
			
			// Ensures Bluetooth is available on the device and it is enabled. If not,
			// displays a dialog requesting user permission to enable Bluetooth.
			if (btAdapter == null || !btAdapter.isEnabled()) {
			    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		    }
			else {
				bluetoothLeIsEnabled = true;
				btHandler = new Handler();
	    		startBluetoothLeService();
	    		
	    		// Broadcast reciever intent filter
	    	    this.intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
	    	    this.intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
	    	    this.intentFilter.addAction(BluetoothLeService.ACTION_DATA_NOTIFY);
	    	    this.intentFilter.addAction(BluetoothLeService.ACTION_DATA_WRITE);
	    	    this.intentFilter.addAction(BluetoothLeService.ACTION_DATA_READ);
	    	    this.intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
	    	    this.intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
	    	    
	    	    registerReceiver(mGattUpdateReceiver, this.intentFilter);
			}
		}
		return bluetoothLeAvailable;
	}
	
	// Starts the scan procedure for BT LE devices. Suppress warnings because API level is set to 16. Just make sure this is not run
	// if device API support is below 18.
	@SuppressLint("NewApi")
	private void scanLeDevice(final boolean enable) {
		if (enable) {
		// Stops scanning after a pre-defined scan period.
        btHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isScanning = false;
                btAdapter.stopLeScan(leScanCallback);
                invalidateOptionsMenu();
                connectedClientsDisplay.append(numDevs + " connected devices in list: \n");
                for (int i = 0; i < deviceInfoList.size(); ++i) {
                	connectedClientsDisplay.append("MAC address: " + deviceInfoList.get(i).getBluetoothDevice().getAddress() + "\n");
                }
        		connectDevices();
            }
        }, BT_SCAN_PERIOD);
        isScanning = true;
        btAdapter.startLeScan(leScanCallback);
        invalidateOptionsMenu();
		} else {
        	isScanning = false;
            btAdapter.stopLeScan(leScanCallback);
            invalidateOptionsMenu();
        }
	}
	
	// Device scan callback.
	// NB! Nexus 4 and Nexus 7 (2012) only provide one scan result per scan
	private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {

	    public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
	      runOnUiThread(new Runnable() {
	        public void run() {
        		if (!deviceInfoExists(device.getAddress())) {
        			// New device
        			BleDeviceInfo deviceInfo = createDeviceInfo(device, rssi);
        			addDevice(deviceInfo);
        			trafficDataDisplay.append("Device found " + numDevs + ": " + device.getAddress() + "\n");
        			
        		} else {
        			// Already in list, update RSSI info
        			BleDeviceInfo deviceInfo = findDeviceInfo(device);
        			deviceInfo.updateRssi(rssi);
        			trafficDataDisplay.append("Device already in list: " + device.getAddress());
        			//mScanView.notifyDataSetChanged();
        		}
	        }
	      });
	    }
	};
	
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
	}
	
	private BleDeviceInfo findDeviceInfo(BluetoothDevice device) {
	    for (int i = 0; i < this.deviceInfoList.size(); i++) {
	      if (this.deviceInfoList.get(i).getBluetoothDevice().getAddress().equals(device.getAddress())) {
	        return this.deviceInfoList.get(i);
	      }
	    }
	    return null;
	 }
	
	private void startBluetoothLeService() {
	    boolean f;

	    Intent bindIntent = new Intent(this, BluetoothLeService.class);
	    startService(bindIntent);
	    f = bindService(bindIntent, btServiceConnector, Context.BIND_AUTO_CREATE);
	    if (f) {
	    	this.showToast("BluetoothLeService - success");
	    	Log.d(TAG, "BluetoothLeService - success");	
	    } else {
	      this.showToast("Bind to BluetoothLeService failed");
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
	            showToast("Multiple connections!");
	          }
	        });
	      } else {
	        Log.i(TAG, "BluetoothLeService connected");
	      }
	    }

	    public void onServiceDisconnected(ComponentName componentName) {
	    	btLeService = null;
	      Log.i(TAG, "BluetoothLeService disconnected");
	    }
	  };

	  void connectDevices() {
		    if (numDevs > 0) {
		    	for (int i = 0; i < deviceInfoList.size(); ++i) {
					BluetoothDevice btDevice = deviceInfoList.get(i).getBluetoothDevice();
					
					int connState = bluetoothManager.getConnectionState(btDevice, BluetoothGatt.GATT);
					
					switch (connState) {
					case BluetoothGatt.STATE_CONNECTED:
						btLeService.disconnect(null);
						break;
					case BluetoothGatt.STATE_DISCONNECTED:
						boolean ok = btLeService.connect(btDevice.getAddress());
						if (!ok) {
							this.showToast("Connect failed");
						}
						break;
					default:
						this.showToast("Device busy (connecting/disconnecting)");
					break;
					  }
				    }
		    	}
		  }
	  
	  public final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		  	@Override
		  	public void onReceive(Context context, Intent intent) {
		  		final String action = intent.getAction();
		  		int status = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS, BluetoothGatt.GATT_SUCCESS);

		  		if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
		  			if (status == BluetoothGatt.GATT_SUCCESS) {
		  				showToast("Services discovered!");
		  				enableServices();
		  			} else {
		  				showToast("Service discovery failed");
		  				return;
		  			}
		  		} else if (BluetoothLeService.ACTION_DATA_NOTIFY.equals(action)) {
		  			// Notification
		  			byte  [] value = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
		  			String uuidStr = intent.getStringExtra(BluetoothLeService.EXTRA_UUID);
		  			onCharacteristicChanged(uuidStr, value);
		  		} else if (BluetoothLeService.ACTION_DATA_WRITE.equals(action)) {
		  			// Data written
		  			String uuidStr = intent.getStringExtra(BluetoothLeService.EXTRA_UUID);
		  			//onCharacteristicWrite(uuidStr,status);
		  		} else if (BluetoothLeService.ACTION_DATA_READ.equals(action)) {
		  			// Data read
		  			String uuidStr = intent.getStringExtra(BluetoothLeService.EXTRA_UUID);
		  			byte  [] value = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
		  			//onCharacteristicsRead(uuidStr,value,status);
		  		} else if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
		  			// GATT connect
		  			int statusConnect = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS, BluetoothGatt.GATT_FAILURE);
		  			if (statusConnect == BluetoothGatt.GATT_SUCCESS) {
		  				showToast("GATT SUCCESS connection!");
		  				BluetoothLeService.getBtGatt().discoverServices();
		  				startSensorDataUpdater();
		  			} else {
		  				//setError("Connect failed. Status: " + status);
		  			}
		  		} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
		  			// GATT disconnect
		  			int statusDisconnect = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS, BluetoothGatt.GATT_FAILURE);
		  			stopSensorDataUpdater();
		  			if (statusDisconnect == BluetoothGatt.GATT_SUCCESS) {
		  				showToast("disconnected!");
		  			} else {
		  				//setError("Disconnect failed. Status: " + status);  				
		  			}
		  			//mConnIndex = NO_DEVICE;
		  		} else {
		  			Log.w(TAG,"Unknown action: " + action);
		  		}

		  		if (status != BluetoothGatt.GATT_SUCCESS) {
		  			//setError("GATT error code: " + status);
		  		}
		  	}

			private void onCharacteristicChanged(String uuidStr, byte[] value) {
				// TODO Auto-generated method stub
				if (uuidStr.equals(SensorTag.UUID_ACC_DATA.toString())) {
					Integer x = (int) value[0];
			  		Integer y = (int) value[1];
			  		Integer z = (int) value[2] * -1;
			  		ACC_X_VALUE = (double)x / 64;
			  		ACC_Y_VALUE = (double)y / 64;
			  		ACC_Z_VALUE = (double)z / 64;
			  		
			  		trafficDataDisplay.append("Acc values: " + String.format("%.2f", ACC_X_VALUE) + ", " + String.format("%.2f", ACC_Y_VALUE) + ", " + String.format("%.2f", ACC_Z_VALUE) + "\n");
			  	}
				else if (uuidStr.equals(SensorTag.UUID_IRT_DATA.toString())) {
					int offset = 2;
					Integer lowerByte = (int) value[offset] & 0xFF; 
				    Integer upperByte = (int) value[offset+1] & 0xFF; // // Interpret MSB as signed
				    double temp = (upperByte << 8) + lowerByte;
				    IRT_TEMP_VALUE = temp/128;
				    
				    trafficDataDisplay.append("Temp: " + String.format("%.2f", IRT_TEMP_VALUE) + "\n");
				}
				else {
					trafficDataDisplay.append("Some other data inbound.\n");
				}
			}
		  };
		  
		  public void enableServices() {
			  btServices = btLeService.getSupportedGattServices();
			  for (BluetoothGattService gattService : btServices) {
				  if (gattService.getUuid().compareTo(SensorTag.UUID_IRT_SERV) == 0) {
					  trafficDataDisplay.append("IRT service found!\n");
					  
					  List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
					  
					  for (BluetoothGattCharacteristic characts : gattCharacteristics) {
						  if (characts.getUuid().compareTo(SensorTag.UUID_IRT_CONF) == 0) {
							  btLeService.writeCharacteristic(characts, (byte)1);
							  trafficDataDisplay.append("Enabling IRT!\n");
							  btLeService.waitIdle(100);
						  }
						  else if (characts.getUuid().compareTo(SensorTag.UUID_IRT_DATA) == 0) {
							  btLeService.setCharacteristicNotification(characts, true);
							  trafficDataDisplay.append("Accepting notifications from IRT!\n");
							  btLeService.waitIdle(100);
						  }
					  }
				  }
				  else if (gattService.getUuid().compareTo(SensorTag.UUID_ACC_SERV) == 0) {
					  trafficDataDisplay.append("Accelerometer service found!\n");
					  
					  List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
					  
					  for (BluetoothGattCharacteristic characts : gattCharacteristics) {
						  if (characts.getUuid().compareTo(SensorTag.UUID_ACC_CONF) == 0) {
							  btLeService.writeCharacteristic(characts, (byte)1);
							  trafficDataDisplay.append("Enabling accelerometer!\n");
							  btLeService.waitIdle(100);
						  }
						  else if (characts.getUuid().compareTo(SensorTag.UUID_ACC_DATA) == 0) {
							  btLeService.setCharacteristicNotification(characts, true);
							  trafficDataDisplay.append("Accepting notifications from acccelerometer!\n");
							  btLeService.waitIdle(100);
						  }
					  }
				  }
				  else if (gattService.getUuid().compareTo(SensorTag.UUID_GYR_SERV) == 0) {
					  trafficDataDisplay.append("GYRO service found!\n");
					  
					  List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
					  
					  for (BluetoothGattCharacteristic characts : gattCharacteristics) {
						  if (characts.getUuid().compareTo(SensorTag.UUID_GYR_CONF) == 0) {
							  btLeService.writeCharacteristic(characts, (byte) 7);
							  trafficDataDisplay.append("Enabling GYR!\n");
							  btLeService.waitIdle(100);
						  }
						  else if (characts.getUuid().compareTo(SensorTag.UUID_GYR_DATA) == 0) {
							  btLeService.setCharacteristicNotification(characts, true);
							  trafficDataDisplay.append("Accepting notifications from GYR!\n");
							  btLeService.waitIdle(100);
						  }
					  }
				  }  
			  }
		  }

		  Runnable sensorDataUpdater = new Runnable() {

				@Override
				public void run() {
					// Create JSON
					
					// Base station id
					String BaseStationID = uuid;
					// MAC address
					String deviceID = deviceInfoList.get(0).getBluetoothDevice().getAddress();
					
					// Accelerometer data
					String ACC_UUID = SensorTag.UUID_ACC_SERV.toString();
					
					// IRT data
					String IRT_UUID = SensorTag.UUID_IRT_SERV.toString();

					JSONObject wrapper = new JSONObject();
					try {
						
					JSONObject baseStation = new JSONObject();
					JSONObject device = new JSONObject();
					JSONArray sensors = new JSONArray();
					
					JSONArray values = new JSONArray();
					values.put(ACC_X_VALUE);
					values.put(ACC_Y_VALUE);
					values.put(ACC_Z_VALUE);
					
					JSONObject value = new JSONObject();
					value.put("time", Settings.getTimeStamp());
					value.put("primitive", "double");
					value.put("unit", "m/s2");
					value.put("values", values);
					
					JSONObject sensor = new JSONObject();
					sensor.put("value", value);
					
					JSONObject attributes = new JSONObject();
					attributes.put("type", "accelerometer");
					attributes.put("vendor", "Texas Instruments");
					
					sensor.put("attributes", attributes);
					
					JSONObject parameters = new JSONObject();
					parameters.put("toggleable", "true");
					parameters.put("options", "boolean");
					
					sensor.put("parameters", parameters);
					sensor.put("uuid", ACC_UUID);
					
					sensors.put(sensor);
					
					JSONObject tempvalue = new JSONObject();
					tempvalue.put("time", Settings.getTimeStamp());
					tempvalue.put("primitive", "double");
					tempvalue.put("unit", "Celsius");
					tempvalue.put("values", IRT_TEMP_VALUE);
					
					JSONObject tempsensor = new JSONObject();
					tempsensor.put("value", tempvalue);
					
					JSONObject tempattributes = new JSONObject();
					tempattributes.put("type", "temperature");
					tempattributes.put("vendor", "Texas Instruments");
					
					tempsensor.put("attributes", tempattributes);
					
					JSONObject tempparameters = new JSONObject();
					tempparameters.put("toggleable", "true");
					tempparameters.put("options", "boolean");
					
					tempsensor.put("parameters", tempparameters);
					tempsensor.put("uuid", IRT_UUID);
					
					sensors.put(tempsensor);
					
					device.put("sensors", sensors);
					JSONArray gps = new JSONArray();
					// If accurate location has not been recieved, use phones last known location.
					if (latitude == 0 || longitude == 0) {
						latitude = locationClient_.getLastLocation().getLatitude();
						longitude = locationClient_.getLastLocation().getLongitude();
					}
					gps.put(latitude);
					gps.put(longitude);
					JSONObject deviceAttributes = new JSONObject();
					deviceAttributes.put("location", gps);
					deviceAttributes.put("name", "TI CC2541 Sensor");
					device.put("attributes", deviceAttributes);
					baseStation.put(deviceID, device);
					
					wrapper.put(BaseStationID, baseStation);
					
					}
					catch (JSONException e) {
						e.printStackTrace();
					}
					// JSON is built. Send it through message service
					sendMessageToService(wrapper.toString().trim());
					
					// Repeat this function again in 1000ms.
					mHandler.postDelayed(sensorDataUpdater, 1000);
				}
		  };
		  
		  void startSensorDataUpdater() {
			  // Start the sensor data reporting to webservice. Starts on BT device connection.
			  sensorDataUpdater.run(); 
		  }

		  void stopSensorDataUpdater() {
			  // Stop sensor data reporting on disconnection. 
			  mHandler.removeCallbacks(sensorDataUpdater);
		  }
}

