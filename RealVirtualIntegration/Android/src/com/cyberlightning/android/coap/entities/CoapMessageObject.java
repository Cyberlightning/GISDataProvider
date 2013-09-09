package com.cyberlightning.android.coap.entities;

import com.cyberlightning.android.coap.service.ConnectionService;


public class CoapMessageObject {
	
	private int type;
	private String content;
	
	
	public CoapMessageObject() {
		
	}
	public CoapMessageObject(int type, String content) {
		this.type = type;
		this.content = content;
	}
	
	public String getMessage(){
		String msg = "";
		
		switch(this.type) {
		case ConnectionService.ACTUATOR_EVENT:
			
			break;
		case ConnectionService.DISCOVER_SERVICE:
			
			break;
		case ConnectionService.EXCEPTION_EVENT:
			
			break;
		case ConnectionService.SENSOR_EVENT:
			
			break;
		}
		return msg;
	}
	
	
}
