package com.cyberlightning.realvirtualsensorsimulator;

import java.util.HashMap;
import java.util.Set;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.realvirtualsensorsimulator.R;

public class MainViewFragment extends Fragment implements OnClickListener{
	
	private boolean isPause = false;
	private Button resetButton;
	private Button toggleButton;
	private LinearLayout statusMessageHolder;
	private LayoutInflater inflater;
	private View view;
	private HashMap<String,Boolean> messages = new HashMap<String,Boolean>();
	
	// Tags to store saved instance state of this fragment
	private static final String STATE_STATUS_MESSAGES = "StateStatusMessages";
	private static final String STATE_TOGGLE_BUTTON = "StateToggleButton";

	
	@SuppressWarnings("unchecked")
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
        this.view = inflater.inflate(R.layout.events, container, false);
        this.inflater = inflater;
        this.statusMessageHolder = (LinearLayout) view.findViewById(R.id.event_status_message_holder);
        this.resetButton = (Button) view.findViewById(R.id.event_reset_button);
        this.resetButton.setOnClickListener(this);
        this.toggleButton = (Button) view.findViewById(R.id.event_toggle_button);
        this.toggleButton.setOnClickListener(this);
        
 
        if (savedInstanceState != null) {
				this.messages = (HashMap<String, Boolean>) savedInstanceState.getSerializable(STATE_STATUS_MESSAGES);
        		Set<String>messages = this.messages.keySet();
        		for (String message : messages) {
        			this.addNewMessage(message, this.messages.get(message));
        			this.view.invalidate();
        		}
        		this.isPause = savedInstanceState.getBoolean(STATE_TOGGLE_BUTTON);
		}

        return view;
    }
	
	@Override
	public void onSaveInstanceState(Bundle saveState) {
		
		// TODO save states of relevant objects here
		if(this.statusMessageHolder != null) {
			saveState.putSerializable(STATE_STATUS_MESSAGES, this.messages);
		} 
		saveState.putBoolean(STATE_TOGGLE_BUTTON, this.isPause);
		super.onSaveInstanceState(saveState);
	}
	
	public void addNewMessage(String _msg, Boolean _isInbound) {
		
		TextView textView = (TextView) this.inflater.inflate(R.layout.entry, null);
		textView.setText(_msg);
		
		if (_isInbound) {
			textView.setTextColor(Color.YELLOW);
		} else {
			textView.setTextColor(Color.CYAN);
		}
		
		this.statusMessageHolder.addView(textView, 0);
		
		if (!this.messages.containsKey(_msg)){
			this.messages.put(_msg, _isInbound);
			this.view.invalidate();
		}

	}
	
	private void clearAll() {
		this.messages.clear();
		this.statusMessageHolder.removeAllViews();
		this.statusMessageHolder.invalidate();
	}
	
	private void togglePause() {
		if (this.isPause) {
			this.isPause = false;
			this.toggleButton.setText(R.string.event_button_toggle_start_title);
			if (getActivity() instanceof MainActivity)((MainActivity)getActivity()).sensorListener.resume();
		} else {
			this.isPause = true;
			this.toggleButton.setText(R.string.event_button_toggle_pause_title);
			if (getActivity() instanceof MainActivity)((MainActivity)getActivity()).sensorListener.pause();
		}
	}

	@Override
	public void onClick(View v) {
		
		switch(v.getId()) {
		case R.id.event_reset_button: this.clearAll();
			break;
		case R.id.event_toggle_button: this.togglePause();
			break;
		}
	}
}