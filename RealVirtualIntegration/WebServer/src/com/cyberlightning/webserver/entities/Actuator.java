package com.cyberlightning.webserver.entities;

import java.util.HashMap;

public class Actuator {
	public String uuid;
	public HashMap<String,Object> attributes = new HashMap<String,Object>();
	public HashMap<String,Object> parameters = new HashMap<String,Object>();
}
