package com.cyberlightning.android.coap.application;

import java.util.ArrayList;
import java.util.HashMap;
/**
 * 
 * @author Tomi
 *
 */
public class Actuator {
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
