package com.cyberlightning.webserver.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Sensor implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 940216814150139738L;

	public HashMap<String,Object> parameters = new HashMap<String,Object>();
	public HashMap<String,Object> attributes = new HashMap<String,Object>();
	public ArrayList<HashMap<String,Object>> values = new ArrayList<HashMap<String,Object>>();
	public String uuid;
}
