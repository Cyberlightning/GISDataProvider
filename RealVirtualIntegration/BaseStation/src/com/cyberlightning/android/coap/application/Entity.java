package com.cyberlightning.android.coap.application;

import java.util.ArrayList;
import java.util.HashMap;
/**
 * 
 * @author Tomi
 *
 */
public class Entity {
	
	/**
	 * 
	 */
	public String uuid = null;
	/**
	 * 
	 */
	public String contextUUID = null;
	/**
	 * 
	 */
	public HashMap<String,Object> history = new HashMap<String,Object>();
	/**
	 * 
	 */
	public HashMap<String,Object> attributes = new HashMap<String,Object>();
	/**
	 * 
	 */
	public ArrayList<Sensor> sensors = new ArrayList<Sensor>();
	/**
	 * 
	 */
	public ArrayList<Actuator> actuators = new ArrayList<Actuator>();
	/**
	 * 
	 */
	public double[] location = new double[2];
	
}
