package com.cyberlightning.webserver.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
/**
 * 
 * @author Tomi
 *
 */
public class Actuator implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3195446211096382939L;
	/**
	 * 
	 */
	public String uuid;
	/**
	 * 
	 */
	public HashMap<String,Object> attributes = new HashMap<String,Object>();
	/**
	 * 
	 */
	public ArrayList<HashMap<String,Object>> configuration = new ArrayList<HashMap<String,Object>>();
	/**
	 * 
	 */
	public ArrayList<HashMap<String,Object>> actions = new ArrayList<HashMap<String,Object>>();
	public ArrayList<HashMap<String,Object>> callbacks = new ArrayList<HashMap<String,Object>>();
	
}
