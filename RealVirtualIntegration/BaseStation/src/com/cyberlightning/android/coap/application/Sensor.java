package com.cyberlightning.android.coap.application;

import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Sensor {

	public HashMap<String,Object> parameters = new HashMap<String,Object>();
	public HashMap<String,Object> attributes = new HashMap<String,Object>();
	public CopyOnWriteArrayList<HashMap<String, Object>> values = new CopyOnWriteArrayList<HashMap<String,Object>>();
	public String uuid;
}
