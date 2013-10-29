package com.cyberlightning.webserver.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Sensor implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 940216814150139738L;
	
	private static final int TYPE_SENSOR = 0;
	private static final int TYPE_ACTUATOR = 1;
	
	private int type;
	public String uuid;
	private String parentId;
	
	public HashMap<String,Object> parameters = new HashMap<String,Object>();
	public HashMap<String,Object> attributes = new HashMap<String,Object>();
	
	

}
