
package com.cyberlightning.android.coap;

import com.cyberlightning.android.coap.service.BaseStationService;
import com.cyberlightning.android.coap.service.BaseStationService.BaseStationServiceBinder;
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
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.provider.Settings;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;


public class CoapBaseStation extends Activity implements DialogInterface.OnClickListener {
   
   
	private boolean hasServiceStopped = false;
	private final int NOTIFICATION_DELAY = 12000;
	
	private BaseStationServiceBinder<BaseStationService> coapService;
	private TextView statustext;
	private ServiceConnection serviceConnection = new ServiceConnection() {
	 
		
		@Override
		public void onServiceDisconnected(ComponentName name){
			//TODO onServiceDisconnected
		}

		@SuppressWarnings("unchecked")
		@Override
		public void onServiceConnected(ComponentName name, IBinder service){
			//Service is connected.
			coapService = (BaseStationServiceBinder<BaseStationService>) service;
			
		}
	};

   
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
       //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activity_coapclient);
        //this.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy); 
        this.initNetworkConnection();
        this.statustext = (TextView) findViewById(R.id.displayStatus);
  
   
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
    }
	
    public Context getContext() {
		return this.getApplicationContext();
	}
	
    public void sendMessage(Message _msg) {
		this.coapService.sendMessage(_msg);
	}

   
    private void initNetworkConnection() {  //create code for Wifi-hotspot
    	
    	boolean hasWifi = true; //set to true to enable debugging. Hotspot disables wifi discovery thus failing this method.
      	boolean hasInternet = false;
    	
      	WifiManager wifiManager = (WifiManager)getBaseContext().getSystemService(Context.WIFI_SERVICE);
        
         if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
        	 hasWifi = true;
        	//wifiManager.setWifiEnabled(true); //TODO ask permission to set WIFI on
         }
    	
    	if (this.haveNetworkConnection()) hasInternet = true;
    	if (!hasWifi) {
    		
    		Toast.makeText(this, R.string.main_no_wifi_notification, this.NOTIFICATION_DELAY).show();
    		Intent settingsIntent = new Intent( Settings.ACTION_WIFI_SETTINGS); 
    		this.startActivityForResult(settingsIntent, 1); //TODO onActivityResult() callback needs interception
    		this.finish();
    		
    	} else {
    		if (hasInternet) {
    			bindService(new Intent(this, BaseStationService.class), this.serviceConnection, Service.BIND_AUTO_CREATE);
    			
    		} else {
    			Toast.makeText(this, R.string.main_no_connection_notification, this.NOTIFICATION_DELAY).show();
        		this.finish();	
    		}
    		
    		
    	}
    }
    private boolean haveNetworkConnection() {
        final ConnectivityManager conMgr = (ConnectivityManager) getSystemService (Context.CONNECTIVITY_SERVICE);
           if (conMgr.getActiveNetworkInfo() != null && conMgr.getActiveNetworkInfo().isAvailable() &&    conMgr.getActiveNetworkInfo().isConnected()) {
                 return true;
           } else {
                 System.out.println("Internet Connection Not Present");
               return false;
           }
    }

    private void showExitDialog() { 
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	
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

	@Override
	public void onClick(DialogInterface dialog, int action) {
		
		switch(action) {
			
			case Dialog.BUTTON_POSITIVE: dialog.cancel();
			break;
			
			case Dialog.BUTTON_NEGATIVE: this.finish();
			break;

			case Dialog.BUTTON_NEUTRAL:  this.finish(); //this.stopService();
			break;

		}	
	}

}

