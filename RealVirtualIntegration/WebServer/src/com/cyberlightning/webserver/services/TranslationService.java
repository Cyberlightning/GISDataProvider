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
	 
	 {
	 "440cd2d8c18d7d3a": {
        "440cd2d8c18d7d3a": {
            "sensors": [
                {
                    "value": {
                        "unit": "rads",
                        "primitive": "3DPoint",
                        "time": "2013-12-10 10:07:30",
                        "values": [
                            21.117462158203125,
                            -0.9801873564720154,
                            -0.6045787930488586
                        ]
                    },
                    "configuration": [
                        {
                            "interval": "ms",
                            "toggleable": "boolean"
                        }
                    ],
                    "attributes": {
                        "type": "gyroscope",
                        "power": 0.5,
                        "vendor": "Invensense",
                        "name": "MPL Gyro"
                    }
                },
                {
                    "value": {
                        "unit": "ms2",
                        "primitive": "3DPoint",
                        "time": "2013-12-10 10:07:30",
                        "values": [
                            149.10000610351562,
                            420.20001220703125,
                            -1463.9000244140625
                        ]
                    },
                    "configuration": [
                        {
                            "interval": "ms",
                            "toggleable": "boolean"
                        }
                    ],
                    "attributes": {
                        "type": "accelerometer",
                        "power": 0.5,
                        "vendor": "Invensense",
                        "name": "MPL accel"
                    }
                },
                {
                    "value": {
                        "unit": "uT",
                        "primitive": "3DPoint",
                        "time": "2013-12-10 10:07:30",
                        "values": [
                            -0.08577163517475128,
                            0.16211289167404175,
                            9.922416687011719
                        ]
                    },
                    "configuration": [
                        {
                            "interval": "ms",
                            "toggleable": "boolean"
                        }
                    ],
                    "attributes": {
                        "type": "magneticfield",
                        "power": 0.5,
                        "vendor": "Invensense",
                        "name": "MPL magnetic field"
                    }
                },
                {
                    "value": {
                        "unit": "orientation",
                        "primitive": "3DPoint",
                        "time": "2013-12-10 10:07:30",
                        "values": [
                            -0.004261057823896408,
                            -0.017044231295585632,
                            0.019174760207533836
                        ]
                    },
                    "configuration": [
                        {
                            "interval": "ms",
                            "toggleable": "boolean"
                        }
                    ],
                    "attributes": {
                        "type": "orientation",
                        "power": 9.699999809265137,
                        "vendor": "Invensense",
                        "name": "MPL Orientation (android deprecated format)"
                    }
                }
            ],
            "actuators": [
                {
                    "configuration": [
                        {
                            "value": "100",
                            "unit": "percent",
                            "name": "viewsize"
                        }
                    ],
                    "actions": [
                        {
                            "value": "[marker1,marker2,marker3,marker4,marker6,marker7,marker8,marker9,marker10,marker11,marker12,marker13,marker14,marker15,marker15,marker16,marker17,marker18,marker19]",
                            "primitive": "array",
                            "unit": "string",
                            "parameter": "viewstate"
                        }
                    ],
                    "callbacks": [
                        {
                            "target": "viewstate",
                            "return_type": "boolean"
                        }
                    ],
                    "attributes": {
                        "dimensions": "[480,800]"
                    }
                }
            ]
        },
        "attributes": {
            "name": "Android device"
        }
    }
}

*/
	
	
	/**Serialize sensor event in JSON form in to a list of entity objects.
	 * @param _jsonString
	 * @return ArrayList<Entity> 
	 */
	public static ArrayList<Entity> decodeSensorJson(String _jsonString)  {
		ArrayList<Entity> entities = new ArrayList<Entity>();
		JSONParser parser = new JSONParser();
		JSONObject entity;
		
		try {
			JSONObject context = (JSONObject) parser.parse(_jsonString.trim());
			String contextUUID = (String) context.keySet().iterator().next();
			entity = (JSONObject) context.get(contextUUID);
			Iterator<?> keys = entity.keySet().iterator();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				Entity e = new Entity();
				e.contextUUID = contextUUID; 
				e.uuid = key;
				JSONObject content = (JSONObject) entity.get(key);
				
				if (entity.containsKey("attributes")) {
					JSONObject attributes = (JSONObject) entity.get("attributes");
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
						} if (act.containsKey("configuration")) {
							JSONArray configurations = (JSONArray) act.get("configuration");
							int k = 0;
							while (k < configurations.size()) {
								JSONObject param = (JSONObject)configurations.get(k);
								Iterator<?> paramKeys = param.keySet().iterator();
								HashMap<String,Object> parameter = new HashMap<String,Object>();
								while (paramKeys.hasNext()) {
									String paramsKey = (String)paramKeys.next();	
									parameter.put(paramsKey, param.get(paramsKey));
								}
								actuator.configuration.add(parameter);
								k++;
							}
	
						} if (act.containsKey("actions")) {
							JSONArray actions = (JSONArray) act.get("actions");
							int m = 0;
							while (m < actions.size()) {
								JSONObject actionJson = (JSONObject)actions.get(m);
								Iterator<?> actionKeys = actionJson.keySet().iterator();
								HashMap<String,Object> action = new HashMap<String,Object>();
								while (actionKeys.hasNext()) {
									String actionKey = (String)actionKeys.next();	
									action.put(actionKey, actionJson.get(actionKey));
								}
								actuator.actions.add(action);
								m++;
							}
	
						}if (act.containsKey("callbacks")) {
							JSONArray callbacks = (JSONArray) act.get("callbacks");
							int n = 0;
							while (n < callbacks.size()) {
								JSONObject callbackJson = (JSONObject)callbacks.get(n);
								Iterator<?> callbackKeys = callbackJson.keySet().iterator();
								HashMap<String,Object> callback = new HashMap<String,Object>();
								while (callbackKeys.hasNext()) {
									String callbackKey = (String)callbackKeys.next();	
									callback.put(callbackKey, callbackJson.get(callbackKey));
								}
								actuator.callbacks.add(callback);
								n++;
							}
	
						} if (act.containsKey("attributes")) {
							JSONObject actuatorsAttrs = (JSONObject) act.get("attributes");
							Iterator<?> actuatorAttrKeys = actuatorsAttrs.keySet().iterator();	
							while (actuatorAttrKeys.hasNext()) {
								String actuatorAttrKey = (String)actuatorAttrKeys.next();	
								actuator.attributes.put(actuatorAttrKey, actuatorsAttrs.get(actuatorAttrKey));
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
						}if (sen.containsKey("configuration")) {
							JSONArray configurations = (JSONArray) sen.get("configuration");
							int k = 0;
							while (k < configurations.size()) {
								JSONObject param = (JSONObject)configurations.get(k);
								Iterator<?> paramKeys = param.keySet().iterator();
								HashMap<String,Object> parameter = new HashMap<String,Object>();
								while (paramKeys.hasNext()) {
									String paramsKey = (String)paramKeys.next();	
									parameter.put(paramsKey, param.get(paramsKey));
								}
								sensor.configuration.add(parameter);
								k++;
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
			System.out.print("ParseException: " + e1.getMessage());
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

				
				Iterator<String> actAttrsKeys = actuator.attributes.keySet().iterator();
				JSONObject actuatorAttrsJson = new JSONObject();
				while (actAttrsKeys.hasNext()) {
					String attrKey = actAttrsKeys.next();
					actuatorAttrsJson.put(attrKey, actuator.attributes.get(attrKey));
				}
				actuatorJson.put("attributes", actuatorAttrsJson);
				
				JSONArray configuration = new JSONArray();
				
				for (int i = 0; i < actuator.configuration.size(); i++) {
					
					HashMap<String,Object> param = actuator.configuration.get(i);
					Iterator<String> paramsKeys = param.keySet().iterator();
					JSONObject parameter = new JSONObject();
					
					while (paramsKeys.hasNext()) {
						String paramKey = paramsKeys.next();
						parameter.put(paramKey, param.get(paramKey));
					}
					configuration.add(parameter);
				}
				
				actuatorJson.put("configuration", configuration);
				
				JSONArray actions = new JSONArray();
				
				for (int j = 0; j < actuator.actions.size(); j++) {
					
					HashMap<String,Object> actuatorActions = actuator.actions.get(j);
					Iterator<String> actionKeys = actuatorActions.keySet().iterator();
					JSONObject action = new JSONObject();
					
					while (actionKeys.hasNext()) {
						String actionKey = actionKeys.next();
						action.put(actionKey, actuatorActions.get(actionKey));
					}
					actions.add(action);
				}
				
				actuatorJson.put("actions", actions);
				
				
				JSONArray callbacks = new JSONArray();
				
				for (int k = 0; k < actuator.callbacks.size(); k++) {
					
					HashMap<String,Object> actuatorCallbacks = actuator.callbacks.get(k);
					Iterator<String> callbackKeys = actuatorCallbacks.keySet().iterator();
					JSONObject callback = new JSONObject();
					
					while (callbackKeys.hasNext()) {
						String callbackKey = callbackKeys.next();
						callback.put(callbackKey, actuatorCallbacks.get(callbackKey));
					}
					callbacks.add(callback);
				}
				
				actuatorJson.put("callbacks", callbacks);
				actuatorsJson.add(actuatorJson);
			}
			
			if (!actuatorsJson.isEmpty())entityJson.put("actuators", actuatorsJson);
			
			JSONArray sensorsJson = new JSONArray();
			
			for (Sensor sensor : entity.sensors) {
				JSONObject sensorJson = new JSONObject();
			
				
				Iterator<String> senAttrsKeys = sensor.attributes.keySet().iterator();
				JSONObject sensorAttrsJson = new JSONObject();
				while (senAttrsKeys.hasNext()) {
					String senAttrKey = senAttrsKeys.next();
					sensorAttrsJson.put(senAttrKey, sensor.attributes.get(senAttrKey));
				}
				sensorJson.put("attributes", sensorAttrsJson);//here
				
				JSONArray configuration = new JSONArray();
				
				for (int k = 0; k < sensor.configuration.size(); k++) {
					
					HashMap<String,Object> param = sensor.configuration.get(k);
					Iterator<String> paramsKeys = param.keySet().iterator();
					JSONObject parameter = new JSONObject();
					
					while (paramsKeys.hasNext()) {
						String paramKey = paramsKeys.next();
						parameter.put(paramKey, param.get(paramKey));
					}
					configuration.add(parameter);
				}
				
				sensorJson.put("configuration", configuration);
				
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
