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
	public HashMap<String,Object> parameters = new HashMap<String,Object>();
	/**
	 * 
	 */
	public ArrayList<HashMap<String,Object>> variables = new ArrayList<HashMap<String,Object>>();
	
}
