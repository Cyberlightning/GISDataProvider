package com.cyberlightning.webserver.entities;

import java.io.Serializable;

public class RowEntry implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -203622951574810692L;
	public String address;
	public String latestEvent;
	public String entityUUID;
	public String contextUUID;
	public float[] location;
	

	public RowEntry () {
	}
	
	public RowEntry (String _timeStamp) {
		this.latestEvent = _timeStamp;
	}
	
	public void setLocation(Float _lat, Float _lon) {
		this.location = new float[2];
		this.location[0] = _lat;
		this.location[1] = _lon;
	}
}
