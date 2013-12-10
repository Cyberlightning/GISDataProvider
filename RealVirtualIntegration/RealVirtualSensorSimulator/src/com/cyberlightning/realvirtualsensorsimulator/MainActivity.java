package com.cyberlightning.realvirtualsensorsimulator;



import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import com.cyberlightning.realvirtualsensorsimulator.interfaces.IClientSocket;
import com.cyberlightning.realvirtualsensorsimulator.interfaces.IMainActivity;
import com.cyberlightning.realvirtualsensorsimulator.interfaces.ISensorListener;
import com.cyberlightning.realvirtualsensorsimulator.views.MainViewFragment;
import com.cyberlightning.realvirtualsensorsimulator.views.MarkerViewFragment;
import com.cyberlightning.realvirtualsensorsimulator.views.SettingsViewFragment;
import com.example.realvirtualsensorsimulator.R;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings.Secure;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("HandlerLeak") public class MainActivity extends Activity implements Observer,IMainActivity {
	
	
	private IClientSocket clientSocket;
	private MainViewFragment mainViewFragment;
	private SettingsViewFragment settingsFragment;
	private MarkerViewFragment markerViewFragment;
	
	public Bundle savedStateBundle;
	public ISensorListener sensorListener;
	
	public boolean isLandScape = false;
	
	public static String deviceId;
	public static String deviceName = "Android device";
	public static String displayActuator = "display";
	public static String displayParameter = "viewstate";
	
	public static final int MESSAGE_FROM_SENSOR_LISTENER = 1;
	public static final int MESSAGE_FROM_SERVER = 2;
	public static final int MESSAGE_TYPE_UNKNOWNHOST_ERROR = 3;
	public static final int MESSAGE_TYPE_SET_MARKER= 4;
	
	public static final String MAIN_VIEW_FRAGMENT = "MainViewFragment";
	
	private Handler messageHandler = new Handler(Looper.getMainLooper()) {
        
		@Override
        public void handleMessage(Message _msg) {
            switch (_msg.what) {
            case MESSAGE_FROM_SENSOR_LISTENER: 	
            	mainViewFragment.addNewMessage(_msg.obj.toString(), false);
            	break;
            case MESSAGE_FROM_SERVER: 			
            	mainViewFragment.addNewMessage(_msg.obj.toString(), true);
            	break;
            case MESSAGE_TYPE_UNKNOWNHOST_ERROR:	
            	showToast(getString(R.string.exception_unknown_host) + _msg.obj.toString());
            case MESSAGE_TYPE_SET_MARKER:
            	onMarkerViewCalled(_msg.obj.toString());
            	break;
    			
            }
        }
    };
	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        FragmentManager fm = getFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_content); 
        if (fragment == null ){
        	FragmentTransaction ft = fm.beginTransaction();
            this.mainViewFragment = new MainViewFragment();
            ft.add(R.id.fragment_content,  this.mainViewFragment);
            ft.commit();
        }
             
        deviceId = Secure.getString((getApplicationContext().getContentResolver()), Secure.ANDROID_ID);
        this.StartApplication();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig); 

        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
        	 //TODO Make implementation if the need occurs. 
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        	//TODO Make implementation if the need occurs. 
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
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
    	if (this.settingsFragment != null){
    		this.settingsFragment.onBackPressed();
    		this.onMainItemClicked();
    	}
    }
  
    @Override
    protected void onDestroy() {
    	this.sensorListener.end();
    	this.clientSocket.end();
    	this.finish();
    	super.onDestroy(); 
    }
    
    /**
     * Called when menu item is being clicked.
     * @param item The item being clicked.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       
        switch (item.getItemId()) {
            case R.id.menu_settings: this.onSettingsItemClicked();
            	return true;
            case R.id.menu_main: 
            		this.onMainItemClicked();
            		if (this.settingsFragment != null) {
            			if (this.settingsFragment.isVisible())this.settingsFragment.onBackPressed();
            		}
        		return true;
            case R.id.menu_quit: this.onDestroy();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    /**
     * Called when in any other view than settings view to show settings view fragment
     */
    public void onSettingsItemClicked() {
    	this.savedStateBundle = this.mainViewFragment.getSavedState();
    	this.sensorListener.pause();
    	
    	FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
    	this.settingsFragment = new SettingsViewFragment();
        fragmentTransaction.replace(R.id.fragment_content, this.settingsFragment);
        fragmentTransaction.commit();
        
        if(this.mainViewFragment.isPause && !this.markerViewFragment.isInLayout()) this.showToast(getString(R.string.toast_sensorlistener_stopped));
    }
    
    /**
     * Called when in any other view than settings view to show settings view fragment
     */
    public void onMarkerViewCalled(String _id) {
    	this.savedStateBundle = this.mainViewFragment.getSavedState();
    	this.sensorListener.pause();
    	
    	FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
    	this.markerViewFragment = new MarkerViewFragment();
    	Bundle bundle = new Bundle();
    	bundle.putString("markerID", _id);
    	this.markerViewFragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.fragment_content, this.markerViewFragment);
        fragmentTransaction.commit();
        
        if(this.mainViewFragment.isPause) this.showToast(getString(R.string.toast_sensorlistener_stopped));
    }
    
   /**
    * Called only when orientation is changed 
    * @param _isLandScape true if new Configuration.ORIENTATION_LANDSCAPE is called
    */
    public void onSettingsItemClicked(Boolean _isLandScape) {
    	this.isLandScape = _isLandScape;
    	FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
    	this.settingsFragment = new SettingsViewFragment();
        fragmentTransaction.replace(R.id.fragment_content, this.settingsFragment);
        fragmentTransaction.commit();
    }
    
    /**
     * Called from any other view than main view to initiate showing main view fragment
     */
    private void onMainItemClicked() {
    	FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
    	fragmentTransaction.replace(R.id.fragment_content,this.mainViewFragment);
        fragmentTransaction.commit();
    }
    
    /**
     * Generic customized toast to be used by the application. 
     * @param _message Toast content
     */
    public void showToast(String _message) {
    	
	    LayoutInflater inflater = getLayoutInflater();
	    View layout = inflater.inflate(R.layout.custom_toast,(ViewGroup) findViewById(R.id.toast_layout_root));

	    TextView text = (TextView) layout.findViewById(R.id.text);
	    text.setText(_message);

	    Toast toast = new Toast(getApplicationContext());
	    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
	    toast.setDuration(Toast.LENGTH_SHORT);
	    toast.setView(layout);
	    toast.show();  	
	}
    /**
     * Called when activity started and no sensors selected for listening. User can cancel the dialog or redirect to
     * settings view for selecting sensors. A convenience method. 
     */
    private void showNoSensorsAlertDialog() {
    	
    	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
    	
    	// set title
		alertDialogBuilder.setTitle(R.string.alert_no_sensors_selected_title);
 
		// set dialog message
		alertDialogBuilder.setMessage(R.string.alert_no_sensors_selected_content)
				.setCancelable(false)
				.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						onSettingsItemClicked();
					}
				  })
				.setNegativeButton("No",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						// if this button is clicked, just close
						// the dialog box and do nothing
						dialog.cancel();
					}
				});
 
		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();
 
		// show it
		alertDialog.show();
    }
    
    
    /**
     * This method is initiated on startup and will launch necessary components. Also if no sensors selected for listening it will notify
     * the user with an alert dialog.
     */
    private void StartApplication() {
    	ClientSocket clientSocket= new ClientSocket(this);
		clientSocket.addObserver(this);
		this.clientSocket = clientSocket;
		
		SensorListener sensorListener = new SensorListener(this);
		sensorListener.addObserver(this);
		this.sensorListener = sensorListener;
		
		SharedPreferences settings = getContext().getSharedPreferences(SettingsViewFragment.PREFS_NAME, 0);
		Set<String> sensors = settings.getStringSet(SettingsViewFragment.SHARED_SENSORS, null);
		if (sensors == null || sensors.size() ==0) this.showNoSensorsAlertDialog();
    }
    
    /**
     * This method returns a reference to main activity context giving access to system resources through this. 
     */
	@Override
	public Context getContext() {
		// TODO Auto-generated method stub
		return this.getApplicationContext();
	}

	/**
	 * This method returns a reference to message handler instance of main activity. This enables any class to fetch the address of main class for sending
	 * message events using Android system messaging. 
	 */
	@Override
	public Handler getTarget() {
		return messageHandler;
	}
	
	/**
	 * This method is part of Java Observer pattern and used for passing data between sensor listener and client socket. Notice: Calling
	 * display within this method will cause exception on Android. 
	 * @param observable class
	 * @param data object passed from observable class 
	 */
	@Override
    public void update(Observable observable, Object data) {
		
		if (observable instanceof ClientSocket) {
			//implement something here do not draw to UI 
        } else if (observable instanceof SensorListener) {
            this.clientSocket.sendMessage((Message)data);
        }   
    }

	@Override
	public void showNoGpsAlert() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
    	
    	// set title
		alertDialogBuilder.setTitle(R.string.alert_no_gps_selected_title);
 
		// set dialog message
		alertDialogBuilder.setMessage(R.string.alert_no_gps_selected_content)
				.setCancelable(false)
				.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						callGPSSettingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			            getContext().startActivity(callGPSSettingIntent);
					}
				  })
				.setNegativeButton("No",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						// if this button is clicked, just close
						// the dialog box and do nothing
						dialog.cancel();
					}
				});
 
		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();
 
		// show it
		alertDialog.show();
		
	}
}
