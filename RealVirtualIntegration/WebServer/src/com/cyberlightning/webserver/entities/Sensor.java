package com.cyberlightning.webserver.entities;

import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Sensor implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 940216814150139738L;

	public HashMap<String,Object> parameters = new HashMap<String,Object>();
	public HashMap<String,Object> attributes = new HashMap<String,Object>();
	public CopyOnWriteArrayList<HashMap<String, Object>> values = new CopyOnWriteArrayList<HashMap<String,Object>>();
	public String uuid;
}
