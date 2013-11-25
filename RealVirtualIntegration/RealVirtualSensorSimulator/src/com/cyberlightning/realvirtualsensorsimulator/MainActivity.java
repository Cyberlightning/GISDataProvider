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
	public ISensorListener sensorListener;

	private TextView receivedMessages;
	private TextView sendMessages;
	private MainViewFragment mainViewFragment;
	private SettingsFragment settingsFragment;
	
	public static String deviceId;
	public static String deviceName = "Android device";
	
	
	
	// Tags to store saved instance state of this activity
	private static final String STATE_RECEIVED_MESSAGES = "StateReceivedMessages";
	private static final String STATE_SEND_MESSAGES = "StateSendMessages";
	
	public static final int MESSAGE_FROM_SENSOR_LISTENER = 1;
	public static final int MESSAGE_FROM_SERVER = 2;
	
	Handler messageHandler = new Handler(Looper.getMainLooper()) {
        
		@Override
        public void handleMessage(Message _msg) {
            switch (_msg.what) {
            case MESSAGE_FROM_SENSOR_LISTENER: 
            	//((TextView)mainViewFragment.getView().findViewById(R.id.outboundMessagesDisplay)).setText(_msg.obj.toString());
            	break;
            case MESSAGE_FROM_SERVER: //TODO draw to UI
            	((TextView)mainViewFragment.getView().findViewById(R.id.inboundMessagesDisplay)).setText(_msg.obj.toString());
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
        
        
        this.sendMessages = (TextView)findViewById(R.id.outboundMessagesDisplay);
        
        if (savedInstanceState != null) {
			this.sendMessages.setText(savedInstanceState.getString(STATE_SEND_MESSAGES));
			this.receivedMessages.setText(savedInstanceState.getString(STATE_RECEIVED_MESSAGES));
		}
        deviceId = Secure.getString((getApplicationContext().getContentResolver()), Secure.ANDROID_ID);
        this.StartApplication();
	
    }
    
    @Override
	public void onSaveInstanceState(Bundle saveState) {
		// TODO save states of relevant objects here
		saveState.putString(STATE_RECEIVED_MESSAGES, this.receivedMessages.getText().toString());
		saveState.putString(STATE_SEND_MESSAGES, this.sendMessages.getText().toString());
		
		super.onSaveInstanceState(saveState);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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
    
    private void onSettingsItemClicked() {
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
