package com.cyberlightning.webserver.entities;

import java.io.Serializable;
import java.util.HashMap;

public class Entity implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6532840481222575471L;
	public String uuid = null;
	public HashMap<String,Object> history = new HashMap<String,Object>();
	public HashMap<String,Object> attributes = new HashMap<String,Object>();
	public HashMap<String,Component> components = new HashMap<String,Component>();
	
	
}
