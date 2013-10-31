package com.cyberlightning.webserver.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.cyberlightning.webserver.entities.Actuator;
import com.cyberlightning.webserver.entities.Entity;
import com.cyberlightning.webserver.entities.Sensor;


public abstract class JsonTranslator {
	
	/*Sample JSON string from sensors
	 * 
	 * {
	    "550e8400-e29b-41d4-a716-446655440000": {
	        "attributes": {
	            "name": "Power wall outlet",
	            "address": null
	        },
	        "actuators": [
	            {
	                "uuid": null,
	                "attributes": {
	                    "type": "power_switch"
	                },
	                "parameters": {
	                    "callback": false
	                },
	                "variables": [
	                    {
	                        "relay": false,
	                        "type": "boolean"
	                    }
	                ]
	            }
	        ],
	        "sensors": [
	            {
	                "uuid": null,
	                "attributes": {
	                    "type": "Power sensor"
	                },
	                "parameters": {
	                    "options": null
	                },
	                "values": [
	                   {
	                        "value": 16,
	                        "time": "YY-MM-DD HH:MM",
	                        "unit" : "Celcius"
	                   },
	                   {
	                        "value": 13,
	                        "time": "YY-MM-DD HH:MM",
	                        "unit" : "Celcius"
	                   }
	                ]
	            }
	        ]
	    }
	}*/
	
	
	/**Serialize sensor event in JSON form in to a list of entity objects.
	 * @param _jsonString
	 * @return ArrayList<Entity> 
	 */
	public static ArrayList<Entity> decodeSensorJson(String _jsonString)  {
		ArrayList<Entity> entities = new ArrayList<Entity>();
		JSONParser parser = new JSONParser();
		JSONObject entity;
		
		try {
			entity = (JSONObject) parser.parse(_jsonString);
			Iterator<?> keys = entity.keySet().iterator();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				Entity e = new Entity();
				e.uuid = key;
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
						} if (act.containsKey("variables")) {
							JSONArray variables  = (JSONArray) act.get("variables");
							int n = 0;
							HashMap<String,Object> variable = new HashMap<String,Object>();
							
							while (n < variables.size()) {
								JSONObject variableJson = (JSONObject) variables.get(n);
								Iterator<?> o = variableJson.keySet().iterator();
								
								while (o.hasNext()) {
									String varKey = (String)o.next();
									variable.put(varKey, variable.get(varKey));
								}
								n++;
								actuator.variables.add(variable);
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
						} if (sen.containsKey("values")) {
							JSONArray values = (JSONArray) sen.get("values");
							int q = 0;
							while (q < values.size()) {
								JSONObject valueJson = (JSONObject) values.get(q);
								Iterator<?> valueKeys = valueJson.keySet().iterator();
								HashMap<String,Object> value = new HashMap<String,Object>();
								
								while (valueKeys.hasNext()) {
									String valueKey = (String)valueKeys.next();
									value.put(valueKey, valueJson.get(valueKey));
								}
								
								sensor.values.add(value);
								q++;
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
	
	/**Deserialize list of requested devices with desired number of values in JSON form string. 
	 * @param _entities
	 * @param _numOfValues
	 * @return String
	 */
	@SuppressWarnings("unchecked")
	public static String encodeJson(ArrayList<Entity> _entities, int _numOfValues) {
	
		JSONObject content = new JSONObject();
		
		for(Entity entity : _entities) {
			JSONObject entityJson = new JSONObject();
			JSONObject attributeJson = new JSONObject();
			JSONArray actuatorsJson = new JSONArray();
			JSONArray sensorsJson = new JSONArray();
			JSONObject componentJson = new JSONObject();
			JSONObject attributesJson = new JSONObject();
			JSONObject parametersJson = new JSONObject();
			JSONArray variablesJson = new JSONArray();
			JSONArray valuesJson = new JSONArray();
			JSONObject variableJson = new JSONObject();
			
			Iterator<String> attrsKeys = entity.attributes.keySet().iterator();
			while (attrsKeys.hasNext()) {
				String attrKey = attrsKeys.next();
				attributeJson.put(attrKey, entity.attributes.get(attrKey));
			}
			entityJson.put("attributes", attributeJson);
			
			for (Actuator actuator : entity.actuators) {
				
				componentJson.put("uuid", actuator.uuid);
				
				Iterator<String> actAttrsKeys = actuator.parameters.keySet().iterator();
				while (actAttrsKeys.hasNext()) {
					String attrKey = actAttrsKeys.next();
					attributesJson.put(attrKey, actuator.parameters.get(attrKey));
				}
				componentJson.put("attributes", attributesJson);
				
				Iterator<String> paramsKeys = actuator.parameters.keySet().iterator();
				while (paramsKeys.hasNext()) {
					String paramKey = paramsKeys.next();
					parametersJson.put(paramKey, actuator.parameters.get(paramKey));
				}
				componentJson.put("parameters", parametersJson);
				
				for (int i = 0 ; i < actuator.variables.size(); i ++) {
					
					HashMap<String,Object > variable = actuator.variables.get(i);
					Iterator<String> j = variable.keySet().iterator();
					while (j.hasNext()) {
						String varKey = j.next();
						variableJson.put(varKey, variable.get(varKey));
					}
					variablesJson.add(variableJson);
				}
				componentJson.put("variables", variablesJson);
				actuatorsJson.add(componentJson);
			}
			entityJson.put("actuators", actuatorsJson);
			
			for (Sensor sensor : entity.sensors) {
				
				componentJson.put("uuid", sensor.uuid);
				
				Iterator<String> senAttrsKeys = sensor.parameters.keySet().iterator();
				while (senAttrsKeys.hasNext()) {
					String senAttrKey = senAttrsKeys.next();
					attributesJson.put(senAttrKey, sensor.parameters.get(senAttrKey));
				}
				componentJson.put("attributes", attributesJson);//here
				
				Iterator<String> paramsKeys = sensor.parameters.keySet().iterator();
				while (paramsKeys.hasNext()) {
					String paramKey = paramsKeys.next();
					parametersJson.put(paramKey, sensor.parameters.get(paramKey));
				}
				componentJson.put("parameters", parametersJson);
				
				for (int i = 0 ; i < _numOfValues; i ++) {
					
					if(!(i < sensor.values.size())) break;
					
					HashMap<String,Object > variable = sensor.values.get(i);
					Iterator<String> j = variable.keySet().iterator();
					while (j.hasNext()) {
						String varKey = j.next();
						variableJson.put(varKey, variable.get(varKey));
					}
					valuesJson.add(variableJson);
				}
				componentJson.put("variables", valuesJson);
				sensorsJson.add(componentJson);	
			}
			
			entityJson.put("sensors", sensorsJson);
			content.put(entity.uuid, entityJson);
		}
		
		return content.toJSONString();
	}
	

}
