package com.cyberlightning.webserver.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.cyberlightning.webserver.entities.Entity;


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
				Iterator<?> subKeys = content.keySet().iterator();
				while (subKeys.hasNext()) {
					if (subKeys)
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
