/** @author Tomi Sarni (tomi.sarni@cyberlightning.com)
 *  Copyright: Cyberlightning Ltd.
 *  
 */

package com.cyberlightning.android.coap.application;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import com.cyberlightning.android.coapclient.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;


public class BaseStationUI extends Activity implements DialogInterface.OnClickListener,ServiceConnection {
	
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
	static final int WIFI_AP_REQUEST = 1;

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
        }
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == WIFI_AP_REQUEST) {
			initNetworkConnection();
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
    	String bound = "false";
    	if (this.isBound)
    		bound = "true";
    	else
    		bound = "false";
    	Log.d("BaseStationUI", bound);
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


}

