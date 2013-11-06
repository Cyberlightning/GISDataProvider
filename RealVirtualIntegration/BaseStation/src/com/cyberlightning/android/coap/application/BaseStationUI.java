/** @author Tomi Sarni (tomi.sarni@cyberlightning.com)
 *  Copyright: Cyberlightning Ltd.
 *  
 */

package com.cyberlightning.android.coap.application;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.cyberlightning.android.coap.TI.utils.BleDeviceInfo;
import com.cyberlightning.android.coap.TI.utils.BluetoothLeService;
import com.cyberlightning.android.coapclient.R;
import com.cyberlightning.android.coap.TI.utils.SensorTag;


public class BaseStationUI extends Activity implements DialogInterface.OnClickListener,ServiceConnection {
	
	public final String TAG = "BaseStationUi";
	
	private final static int BT_SCAN_PERIOD = 10000; // Scan duration for bluetooth in ms.
	private boolean bluetoothLeAvailable = true;
	private boolean bluetoothLeIsEnabled = false;
	private BluetoothAdapter btAdapter = null;
	private BluetoothManager bluetoothManager = null;
	private boolean isScanning = false;
	private Handler btHandler = null;
	private BluetoothLeService btLeService = null;
	private List<BluetoothGattService> btServices;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private ExpandableListView mGattServicesList;
	
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
	
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
	
	// Intent filter for gatt
	private IntentFilter intentFilter;
	

	// Suppress warnings of too low API level because of bluetooth. If service is not found we just disable all BT related services.
	//@SuppressLint({ "InlinedApi", "NewApi" })
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
	        connectedClientsDisplay = (TextView) findViewById(R.id.connectedDisplay);
	        trafficDataDisplay = (TextView) findViewById(R.id.trafficDisplay);
	        deviceInfoList = new ArrayList<BleDeviceInfo>();
	        
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
        }
        connectedClientsDisplay.setMovementMethod(new ScrollingMovementMethod());
        trafficDataDisplay.setMovementMethod(new ScrollingMovementMethod());
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
		  			} else {
		  				//setError("Connect failed. Status: " + status);
		  			}
		  		} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
		  			// GATT disconnect
		  			int statusDisconnect = intent.getIntExtra(BluetoothLeService.EXTRA_STATUS, BluetoothGatt.GATT_FAILURE);
		  			//stopDeviceActivity();
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
			  		double realX = (double)x / 64;
			  		double realY = (double)y / 64;
			  		double realZ = (double)z / 64;
			  		
			  		trafficDataDisplay.setText("Acc values: " + String.format("%.2f", realX) + ", " + String.format("%.2f", realY) + ", " + String.format("%.2f", realZ) + "\n");
			  	} 
			}
		  };
		  
		  public void enableServices() {
			  btServices = btLeService.getSupportedGattServices();
			  int i = 0;
			  
			  trafficDataDisplay.append("Finding accelerometer service!\n"+SensorTag.UUID_ACC_SERV.toString() + "\n");
			  
			  for (BluetoothGattService gattService : btServices) {
				  if (gattService.getUuid().compareTo(SensorTag.UUID_ACC_SERV) == 0) {
					  trafficDataDisplay.append("Accelerometer service found!\n");
					  
					  // We have our accelerometer service now. Check for characteristic.
					  int j = 0;
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
						  ++j;
					  }
				  }
				  ++i;  
			  }
		  }
}

