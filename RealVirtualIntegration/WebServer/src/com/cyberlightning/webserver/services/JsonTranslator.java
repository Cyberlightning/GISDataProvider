package com.cyberlightning.webserver.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.cyberlightning.webserver.entities.Actuator;
import com.cyberlightning.webserver.entities.Entity;
import com.cyberlightning.webserver.entities.Sensor;


public abstract class JsonTranslator {
	
	public static ArrayList<Entity> decodeJson(String _jsonString)  {
		ArrayList<Entity> entities = new ArrayList<Entity>();
		JSONParser parser = new JSONParser();
		JSONObject entity;
		
		try {
			entity = (JSONObject) parser.parse(_jsonString);
			Iterator<?> keys = entity.keySet().iterator();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				Entity e = new Entity();
				JSONObject content = (JSONObject) entity.get(key);
				
				if (content.containsKey("attributes")) {
					JSONObject attributes = (JSONObject) content.get("attributes");
					Iterator<?> i = attributes.keySet().iterator();
					while (i.hasNext()) {
						String attrKey = (String)i.next();
						e.attributes.put(attrKey, attributes.get(attrKey));		
					}
				}
				if (content.containsKey("actuators")) {
					JSONArray actuators  = (JSONArray) content.get("actuators");
					int l = 0;
					while (l < actuators.size()) {
						Actuator actuator = new Actuator();
						JSONObject act = (JSONObject) actuators.get(l);
						l++;
						
						if (act.containsKey("attributes")) {
							JSONObject actAttrs = (JSONObject) act.get("attributes");
							Iterator<?> i = actAttrs.keySet().iterator();
							
							while (i.hasNext()) {
								String actKey = (String)i.next();	
								actuator.attributes.put(actKey, actAttrs.get(actKey));
							}
						} if (act.containsKey("parameters")) {
							JSONObject actParams = (JSONObject) act.get("parameters");
							Iterator<?> i = actParams.keySet().iterator();
								
							while (i.hasNext()) {
								String paramsKey = (String)i.next();	
								actuator.parameters.put(paramsKey, actParams.get(paramsKey));
							}
						}
						
						e.actuators.add(actuator);
					}
				}
				if (content.containsKey("sensors")) {
					JSONArray sensors  = (JSONArray) content.get("sensors");
					int m = 0;
					
					while (m < sensors.size()) {
						
						JSONObject sen = (JSONObject) sensors.get(m);
						Sensor sensor = new Sensor();
						m++;
						
						if (sen.containsKey("attributes")) {
							JSONObject senAttrs = (JSONObject) sen.get("attributes");
							Iterator<?> i = senAttrs.keySet().iterator();	
							while (i.hasNext()) {
								String attrsKey = (String)i.next();	
								sensor.attributes.put(attrsKey, senAttrs.get(attrsKey));
							}
						} if (sen.containsKey("parameters")) {
							JSONObject senParams = (JSONObject) sen.get("parameters");
							Iterator<?> i = senParams.keySet().iterator();	
				
							while (i.hasNext()) {
								String paramsKey = (String)i.next();	
								sensor.parameters.put(paramsKey, senParams.get(paramsKey));

							}
						}
						e.sensors.add(sensor);
					}
				}
				
				entities.add(e);
			}
			
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return entities;
	}
}
