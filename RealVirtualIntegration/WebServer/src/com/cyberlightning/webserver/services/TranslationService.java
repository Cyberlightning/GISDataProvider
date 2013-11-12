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

/**
 * 
 * @author Tomi
 *
 */
public abstract class TranslationService {
	
	/*Sample JSON string from sensors
	 * 
	 * {
    "550e8400-e29b-41d4-a716-446655440000": {
        "sensors": [
            {
                "values": [
                    {
                        "unit": "3DPoint",
                        "time": "YY-MM-DD HH:MM",
                        "value": [12,13,14]
                    },
                    {
                        "unit": "Celcius",
                        "time": "YY-MM-DD HH:MM",
                        "value": 13
                    },
                    {
                        "unit": "Celcius",
                        "time": "YY-MM-DD HH:MM",
                        "value": 13
                    },
                    {
                        "unit": "Celcius",
                        "time": "YY-MM-DD HH:MM",
                        "value": 13
                    },
                    {
                        "unit": "Celcius",
                        "time": "YY-MM-DD HH:MM",
                        "value": 13
                    },
                    {
                        "unit": "Celcius",
                        "time": "YY-MM-DD HH:MM",
                        "value": 13
                    },
                    {
                        "unit": "Celcius",
                        "time": "YY-MM-DD HH:MM",
                        "value": 13
                    },
                    {
                        "unit": "Celcius",
                        "time": "YY-MM-DD HH:MM",
                        "value": 13
                    },
                    {
                        "unit": "Celcius",
                        "time": "YY-MM-DD HH:MM",
                        "value": 13
                    },
                    {
                        "unit": "Celcius",
                        "time": "YY-MM-DD HH:MM",
                        "value": 13
                    },
                    {
                        "unit": "Celcius",
                        "time": "YY-MM-DD HH:MM",
                        "value": 13
                    }
                ],
                "parameters": {
                    "callback": false,
                    "options": null
                },
                "attributes": {
                    "callback": false,
                    "options": null
                },
                "uuid": null
            }
        ],
        "attributes": {
            "name": "Power wall outlet"
        },
        "actuators": [
            {
                "parameters": {
                    "callback": false,
                    "options": null
                },
                "attributes": {
                    "callback": false,
                    "options": null
                },
                "uuid": null,
                "variables": [
                    {
                        "relay": null,
                        "type": null
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
			JSONObject context = (JSONObject) parser.parse(_jsonString);
			String contextUUID = (String) context.keySet().iterator().next();
			entity = (JSONObject) context.get(contextUUID);
			Iterator<?> keys = entity.keySet().iterator();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				Entity e = new Entity();
				e.contextUUID = contextUUID; 
				e.uuid = key;
				JSONObject content = (JSONObject) entity.get(key);
				
				if (content.containsKey("attributes")) {
					JSONObject attributes = (JSONObject) content.get("attributes");
					Iterator<?> i = attributes.keySet().iterator();
					while (i.hasNext()) {
						String attrKey = (String)i.next();
						
						if (attrKey.contentEquals("gps")) {
							
							JSONArray loc = (JSONArray) attributes.get(attrKey); //TODO could 
							e.location[0] = (double)loc.get(0);
							e.location[1] = (double)loc.get(1);
							
						} else {
							e.attributes.put(attrKey, attributes.get(attrKey));
						}
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
						} if (sen.containsKey("value")) {
							JSONObject value =(JSONObject)sen.get("value");
							String primitive = (String)value.get("primitive");
							boolean is3DPoint = false;
							if (primitive.contentEquals("3DPoint")) {
								is3DPoint = true;
							}
							HashMap<String,Object> v = new HashMap<String,Object>();
							Iterator<?> valueKeys = value.keySet().iterator();
							
							while (valueKeys.hasNext()) {
								
								String valueKey = (String)valueKeys.next();
								if(is3DPoint && valueKey.contentEquals("values")) {
									v.put(valueKey, (JSONArray) value.get("values"));
								} else {
									v.put(valueKey, value.get(valueKey));
								}
								
							}
							sensor.values.add(v);
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
			
			
			Iterator<String> attrsKeys = entity.attributes.keySet().iterator();
			JSONObject attributeJson = new JSONObject();
			while (attrsKeys.hasNext()) {
				String attrKey = attrsKeys.next();
				attributeJson.put(attrKey, entity.attributes.get(attrKey));
			}
			entityJson.put("attributes", attributeJson);
			JSONArray actuatorsJson = new JSONArray();
			for (Actuator actuator : entity.actuators) {
				JSONObject actuatorJson = new JSONObject();
				actuatorJson.put("uuid", actuator.uuid);
				
				Iterator<String> actAttrsKeys = actuator.parameters.keySet().iterator();
				JSONObject actuatorAttrsJson = new JSONObject();
				while (actAttrsKeys.hasNext()) {
					String attrKey = actAttrsKeys.next();
					actuatorAttrsJson.put(attrKey, actuator.parameters.get(attrKey));
				}
				actuatorJson.put("attributes", actuatorAttrsJson);
				
				Iterator<String> paramsKeys = actuator.parameters.keySet().iterator();
				JSONObject actuatorParamsJson = new JSONObject();
				while (paramsKeys.hasNext()) {
					String paramKey = paramsKeys.next();
					actuatorParamsJson.put(paramKey, actuator.parameters.get(paramKey));
				}
				actuatorJson.put("parameters", actuatorParamsJson);
				JSONArray actuatorVarsJson = new JSONArray();
				for (int i = 0 ; i < actuator.variables.size(); i ++) {
					
					HashMap<String,Object > variable = actuator.variables.get(i);
					Iterator<String> j = variable.keySet().iterator();
					JSONObject actuatorVarJson = new JSONObject();
					while (j.hasNext()) {
						String varKey = j.next();
						actuatorVarJson.put(varKey, variable.get(varKey));
					}
					
					actuatorVarsJson.add(actuatorVarJson);
				}
				actuatorJson.put("variables", actuatorVarsJson);
				actuatorsJson.add(actuatorJson);
			
			}
			entityJson.put("actuators", actuatorsJson);
			JSONArray sensorsJson = new JSONArray();
			for (Sensor sensor : entity.sensors) {
				JSONObject sensorJson = new JSONObject();
				sensorJson.put("uuid", sensor.uuid);
				
				Iterator<String> senAttrsKeys = sensor.attributes.keySet().iterator();
				JSONObject sensorAttrsJson = new JSONObject();
				while (senAttrsKeys.hasNext()) {
					String senAttrKey = senAttrsKeys.next();
					sensorAttrsJson.put(senAttrKey, sensor.attributes.get(senAttrKey));
				}
				sensorJson.put("attributes", sensorAttrsJson);//here
				
				Iterator<String> paramsKeys = sensor.parameters.keySet().iterator();
				JSONObject sensorParamsJson = new JSONObject();
				while (paramsKeys.hasNext()) {
					String paramKey = paramsKeys.next();
					sensorParamsJson.put(paramKey, sensor.parameters.get(paramKey));
				}
				sensorJson.put("parameters", sensorParamsJson);
				JSONArray sensorValuesJson = new JSONArray();
				int count = 1;
				for (int i = (sensor.values.size()-1) ; i > -1; i--) { //return latest value first;
					
					if(count > _numOfValues) break;
					count++;
					
					HashMap<String,Object > variable = sensor.values.get(i);
					Iterator<String> j = variable.keySet().iterator();
					JSONObject sensorValueJson = new JSONObject();
					while (j.hasNext()) {
						String varKey = j.next();
						sensorValueJson.put(varKey, variable.get(varKey));
					}
					sensorValuesJson.add(sensorValueJson);
				}
				sensorJson.put("values", sensorValuesJson);
				sensorsJson.add(sensorJson);	
			}
			
			entityJson.put("sensors", sensorsJson);
			content.put(entity.uuid, entityJson);
		}
		
		return content.toJSONString();
	}
	

}
