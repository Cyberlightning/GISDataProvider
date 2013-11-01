package com.cyberlightning.webserver.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
/**
 * 
 * @author Tomi
 *
 */
public class Entity implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6532840481222575471L;
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
	public float[] gps;
	
}
