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
	

	public RowEntry () {
	}
	
	public RowEntry (String _timeStamp) {
		this.latestEvent = _timeStamp;
	}
}
