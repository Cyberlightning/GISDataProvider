package com.cyberlightning.realvirtualsensorsimulator;



import java.util.Observable;
import java.util.Observer;

import com.example.realvirtualsensorsimulator.R;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings.Secure;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Configuration;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements Observer,IMainActivity {
	
	
	private IClientSocket clientSocket;
	private MainViewFragment mainViewFragment;
	private SettingsFragment settingsFragment;
	
	public Bundle savedStateBundle;
	public ISensorListener sensorListener;
	
	public boolean isLandScape = false;
	
	public static String deviceId;
	public static String deviceName = "Android device";
	
	public static final int MESSAGE_FROM_SENSOR_LISTENER = 1;
	public static final int MESSAGE_FROM_SERVER = 2;
	
	public static final String MAIN_VIEW_FRAGMENT = "MainViewFragment";
	
	Handler messageHandler = new Handler(Looper.getMainLooper()) {
        
		@Override
        public void handleMessage(Message _msg) {
            switch (_msg.what) {
            case MESSAGE_FROM_SENSOR_LISTENER: 	mainViewFragment.addNewMessage(_msg.obj.toString(), false);
            	break;
            case MESSAGE_FROM_SERVER: 			mainViewFragment.addNewMessage(_msg.obj.toString(), true);
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
        	 //TODO setContentView(Custom Portrait view)
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //TODO setContentView(Custom Landscape view)
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
    
    public void onSettingsItemClicked() {
    	this.savedStateBundle = this.mainViewFragment.getSavedState();
    	this.sensorListener.pause();
    	
    	FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
    	this.settingsFragment = new SettingsFragment();
        fragmentTransaction.replace(R.id.fragment_content, this.settingsFragment);
        fragmentTransaction.commit();
        
        if(this.mainViewFragment.isPause) this.showToast(getString(R.string.toast_sensorlistener_stopped));
    }
    public void onSettingsItemClicked(Boolean _isLandScape) {
    	this.isLandScape = _isLandScape;
    	FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
    	this.settingsFragment = new SettingsFragment();
        fragmentTransaction.replace(R.id.fragment_content, this.settingsFragment);
        fragmentTransaction.commit();
    }
    private void onMainItemClicked() {
    	FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
    	fragmentTransaction.replace(R.id.fragment_content,this.mainViewFragment);
        fragmentTransaction.commit();
    }
  
    public void showToast(String _message) {
    	
	    LayoutInflater inflater = getLayoutInflater();
	    View layout = inflater.inflate(R.layout.custom_toast,(ViewGroup) findViewById(R.id.toast_layout_root));

	    TextView text = (TextView) layout.findViewById(R.id.text);
	    text.setText(_message);

	    Toast toast = new Toast(getApplicationContext());
	    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
	    toast.setDuration(Toast.LENGTH_LONG);
	    toast.setView(layout);
	    toast.show();  	
	}
    
    private void StartApplication() {
    	ClientSocket clientSocket= new ClientSocket(this);
		clientSocket.addObserver(this);
		this.clientSocket = clientSocket;
		
		SensorListener sensorListener = new SensorListener(this);
		sensorListener.addObserver(this);
		this.sensorListener = sensorListener;
    }
    

	@Override
	public Context getContext() {
		// TODO Auto-generated method stub
		return this.getApplicationContext();
	}


	@Override
	public Handler getTarget() {
		return this.messageHandler;
	}

	@Override
    public void update(Observable observable, Object data) {
		
		if (observable instanceof ClientSocket) {
			this.sensorListener.toggleSensor(1); //TODO implement     
        } else if (observable instanceof SensorListener) {
            this.clientSocket.sendMessage((Message)data);
        }    
    }
}
