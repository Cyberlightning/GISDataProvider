package com.cyberlightning.webserver.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Component implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 940216814150139738L;
	
	private static final int TYPE_SENSOR = 0;
	private static final int TYPE_ACTUATOR = 1;
	
	private int type;
	private String macAddress;
	private String parentId;
	
	private HashMap<String,Object> parameters = new HashMap<String,Object>();
	private ArrayList attributes = new ArrayList();
	

}
