
package com.cyberlightning.android.coap;

import com.cyberlightning.android.coap.sensor.SensorListener;
import com.cyberlightning.android.coap.service.CoapService;
import com.cyberlightning.android.coapclient.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.StrictMode;
import android.provider.Settings;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;


public class Application extends Activity implements DialogInterface.OnClickListener {
   
    private boolean isBound = false;
	private boolean hasServiceStopped = false;
	
	private Messenger messengerService = null;
	private TextView statustext;
	
	private final int NOTIFICATION_DELAY = 12000;
	private final Messenger mMessenger = new Messenger(new IncomingHandler());

   
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
 
    
    private void initNetworkConnection() { 
    	
    	ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
      	boolean hasInternet = false;
    	
    	for (NetworkInfo networkInfo : connectivityManager.getAllNetworkInfo()) { 
            if (networkInfo.getTypeName().equalsIgnoreCase("WIFI")) {
            	if (networkInfo.isConnected()) hasInternet = true;       	
            }
                
            if (networkInfo.getTypeName().equalsIgnoreCase("MOBILE")) {
            	if (networkInfo.isConnected()) hasInternet = true;	
            }  
    	}
    	
    	if (!hasInternet) {
    		Intent settingsIntent = new Intent( Settings.ACTION_WIFI_SETTINGS); 
    		this.startActivityForResult(settingsIntent, 1); //TODO onActivityResult() callback needs interception
    		Toast.makeText(this, R.string.main_no_connection_notification, this.NOTIFICATION_DELAY).show();
    		this.finish();	
    	} else {
    		this.doBindService();
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
    
    private void doBindService() { 
        bindService(new Intent(this, CoapService.class), serviceConnection, Context.BIND_AUTO_CREATE);
        this.isBound = true;
    }
    
    private void doUnbindService() { 

    	if (this.isBound) {

        	if (this.messengerService != null) {
                try {
                    Message msg = Message.obtain(null, CoapService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    this.messengerService.send(msg);
                   
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            
            unbindService(serviceConnection);
            this.isBound = false;
        }
    }

 	private void stopService() {
 		this.doUnbindService();
        stopService(new Intent(Application.this, CoapService.class));
 	}
 	
	@Override
	public void onClick(DialogInterface dialog, int action) {
		
		switch(action) {
			
			case Dialog.BUTTON_POSITIVE: dialog.cancel();
			break;
			
			case Dialog.BUTTON_NEGATIVE: this.finish();
			break;

			case Dialog.BUTTON_NEUTRAL: this.stopService(); this.finish();
			break;

		}	
	}
	public void initiateSensorListener() {
		  Runnable sensorListener = new SensorListener(this,this.messengerService);
	      Thread sensorThread = new Thread(sensorListener);
	      sensorThread.start();
	}
	
	/**
	 * Handler of incoming messages from service.
	 */
	public class IncomingHandler extends Handler {
	    @Override
	    public void handleMessage(Message msg) {
	        switch (msg.what) {
	            case StaticResources.SENSOR_EVENT:
				try {
					messengerService.send(msg);
					statustext.setTextColor(Color.GREEN);
					
					statustext.setText("Sensor event send -> sensor number: " + msg.arg2);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	               
	                break;
	            default:
	                super.handleMessage(msg);
	        }
	    }
	}
	
	private ServiceConnection serviceConnection = new ServiceConnection() { 
		
		public void onServiceConnected(ComponentName className, IBinder service) {
	        	
			messengerService= new Messenger(service);
	            
			try {
	            Message msg = Message.obtain(null, CoapService.MSG_REGISTER_CLIENT);
	            msg.replyTo = mMessenger;
	            messengerService.send(msg);
	               
	        } catch (RemoteException e) {
	        	e.printStackTrace();
	        } 
		    initiateSensorListener();
		}
	    public void onServiceDisconnected(ComponentName className) {
	        messengerService = null;
	    }
	}; 
	

}

