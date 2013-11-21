package com.cyberlightning.realvirtualsensorsimulator;

import android.app.Fragment;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.realvirtualsensorsimulator.R;

public class MainViewFragment extends Fragment {
	
	public TextView receivedMessages;
	public TextView sendMessages;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
        View view = inflater.inflate(R.layout.events_fragment, container, false);

        this.receivedMessages = (TextView)getActivity().findViewById(R.id.inboundMessagesDisplay);
		this.sendMessages = (TextView)getActivity().findViewById(R.id.outboundMessagesDisplay);
		
		//this.receivedMessages.setMovementMethod(new ScrollingMovementMethod());
		//this.sendMessages.setMovementMethod(new ScrollingMovementMethod());

        return view;
    }
}