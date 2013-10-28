package com.cyberlightning.webserver.entities;

import java.io.Serializable;
import java.util.HashMap;

public class Entity implements Serializable {
	
	public String uuid;
	public HashMap<String,Component> attributes;
	
}
