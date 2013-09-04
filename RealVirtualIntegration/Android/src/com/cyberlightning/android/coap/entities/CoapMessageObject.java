package com.cyberlightning.android.coap.entities;

import com.cyberlightning.android.coap.service.ConnectionService;


public class CoapMessageObject {
	
	private int state;
	private int content;
	private boolean isRead;
	
	public CoapMessageObject() {
		
	}
	public CoapMessageObject(int state, int content) {
		this.state = state;
		this.content = content;
	}
	
	public String getMessage(){
		String msg = "";
		
		switch(this.state) {
		case ConnectionService.STATE_UP:
			msg = "[State-up]";
			break;
		case ConnectionService.STATE_DOWN:
			msg = "[State-down: " + this.content + "]";
			break;
		case ConnectionService.FREQUENCY_CHANGE:
			msg = "[Frequency-change: " + "this.frequency" + "]";
			break;
		case ConnectionService.LONG_STATE_UP:
			msg = "[Long-state-up]";
			break;
		}
		return msg;
	}
	
	
}
