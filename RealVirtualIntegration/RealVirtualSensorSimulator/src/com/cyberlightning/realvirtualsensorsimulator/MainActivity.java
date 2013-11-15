package com.cyberlightning.realvirtualsensorsimulator;



import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.example.realvirtualsensorsimulator.R;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings.Secure;
import android.app.Activity;
import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements Observer,IMainActivity {
	
	private IClientSocket clientSocket;
	private ISensorListener sensorListener;

	private TextView receivedMessages;
	private TextView sendMessages;
	
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
            	sendMessages.setText(_msg.obj.toString());
            	break;
            case MESSAGE_FROM_SERVER: //TODO draw to UI
            	receivedMessages.append(_msg.obj.toString());
            	break;
      
            }
       
        }
      
    };
	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
     
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
        getMenuInflater().inflate(R.menu.main, menu);
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
    	//TODO onBackPressed()
    }
  
    @Override
    protected void onDestroy() {
    	super.onDestroy(); 
    }
    
//    private void showToast(String _message) {
//    	
//	    LayoutInflater inflater = getLayoutInflater();
//	    View layout = inflater.inflate(R.layout.toast_layout,(ViewGroup) findViewById(R.id.toast_layout_root));
//
//	    TextView text = (TextView) layout.findViewById(R.id.text);
//	    text.setText(_message);
//
//	    Toast toast = new Toast(getApplicationContext());
//	    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
//	    toast.setDuration(Toast.LENGTH_LONG);
//	    toast.setView(layout);
//	    toast.show();
//	    	
//	}
    
   
    private void StartApplication() {
    	this.receivedMessages = (TextView)findViewById(R.id.inboundMessagesDisplay);
		this.sendMessages = (TextView)findViewById(R.id.outboundMessagesDisplay);
		this.receivedMessages.setMovementMethod(new ScrollingMovementMethod());
		this.sendMessages.setMovementMethod(new ScrollingMovementMethod());
		
		

		
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
                   //TODO handle input commands
                    
            } else if (observable instanceof SensorListener) {
                    this.clientSocket.sendMessage((Message)data);
            }
            
    }
}
