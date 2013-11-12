package com.cyberlightning.realvirtualsensorsimulator;


import java.util.Observable;
import java.util.Observer;

import com.example.realvirtualsensorsimulator.R;

import android.os.Bundle;
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

public class MainActivity extends Activity implements Observer {
	
	private Button showButton;
	private SensorListener sensorListener;
	private TextView receivedMessages;
	private TextView sendMessages;
	
	
	
	// Tags to store saved instance state of this activity
	private static final String STATE_RECEIVED_MESSAGES = "StateReceivedMessages";
	private static final String STATE_SEND_MESSAGES = "StateSendMessages"; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
     
        if (savedInstanceState != null) {
			this.sendMessages.setText(savedInstanceState.getString(STATE_SEND_MESSAGES));
			this.receivedMessages.setText(savedInstanceState.getString(STATE_RECEIVED_MESSAGES));
		}
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
    
    public static Context getAppContext() {
    return MainActivity.getAppContext(); 
    }
    
    private void StartApplication() {
    	this.receivedMessages = (TextView)findViewById(R.id.inboundMessagesDisplay);
		this.sendMessages = (TextView)findViewById(R.id.outboundMessagesDisplay);
		this.receivedMessages.setMovementMethod(new ScrollingMovementMethod());
		this.sendMessages.setMovementMethod(new ScrollingMovementMethod());
		
		this.sensorListener = new SensorListener();
		this.sensorListener.addObserver(this);
		
    }

	@Override
	public void update(Observable observable, Object data) {
		// TODO Auto-generated method stub
		
	}
    
}
