package com.cyberlightning.webserver;

import java.io.File;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class SimulateSensorResponse {
	private static final String uuid = "550e8400-e29b-41d4-a716-446655440000";
	private static float powerConsumption = 1.23f;
	private static boolean relayOn = false;
	private static String address;
	private static String type = "power_switch";
	
	
	public static String loadBySpatial(String _lat, String _lon, int _radius) {
		
		JSONObject response = new JSONObject();
		JSONArray coordinates = new JSONArray();
		coordinates.add(_lat);
		coordinates.add(_lon);
		response.put("coordinates", coordinates);
		response.put("radius", _radius );		
		JSONArray devices = new JSONArray();
		JSONObject device = new JSONObject();
		device.put("uuid", uuid);
		JSONArray sensors = new JSONArray();
		JSONObject sensor = new JSONObject();
		sensor.put("power", "A");
		sensors.add(sensor);
		device.put("sensors", sensors);
		JSONArray actuators = new JSONArray();
		JSONObject actuator = new JSONObject();
		actuator.put("relay", relayOn);
		actuators.add(actuator);
		device.put("actuators", actuators);
		devices.add(device);
		response.put("devices", devices);
		return response.toJSONString();
		
		/*{
			"coordinates": 
				[
            "lat",
            "lon"
            ],
        "radius": "int",
        "devices": [
            {
                "uuid": "value",
                "sensors": 
                	[
                    {
                        "sensor": "unit"
                    },
                    {
                        "sensor": "unit"
                    }
                	],
            
            	
                "actuators": 
                	[
                    	{
                        	"actuator": "status"
                    	},
                    	{
                        	"actuator": "status"
                    	}
                	]
            }
        ]
    }*/
		
			    

	}
	
	public static String loadById(String _uuid) {
		
		String content;
	
		
		if(_uuid.trim().contentEquals(uuid)) {
			
			JSONObject response = new JSONObject();
			
			response.put("uuid", uuid );	
			response.put("address",address);
			JSONObject attributes = new JSONObject();
			attributes.put("name", "Power wall outlet");
			response.put("attributes", attributes);
			JSONArray actuators = new JSONArray();
			JSONObject actuator = new JSONObject();
			actuator.put("uuid", null);
			JSONObject actuatorAttr = new JSONObject();
			actuatorAttr.put("type", "power_switch");
			actuator.put("attributes", actuatorAttr);
			JSONObject parameters = new JSONObject();
			parameters.put("relay", false);
			actuator.put("parameters", parameters);
			actuators.add(actuator);
			response.put("actuators", actuators);
			
			JSONArray sensors = new JSONArray();
			JSONObject sensor = new JSONObject();
			sensor.put("uuid", null);
			
			JSONObject sensorAttr = new JSONObject();
			sensorAttr.put("type", "Power sensor");
			sensor.put("attributes", sensorAttr);
			
			JSONObject readings = new JSONObject();
			readings.put("unit", "A");
			
			JSONArray values = new JSONArray();
			values.add(powerConsumption);
			
			readings.put("values",values );
			sensor.put("options", null);
			
			sensors.add(sensor);
			response.put("sensors", sensors);
			
			
			/*{
			    "uuid": "value",
			    "address": "value",
			    "attributes": {
			        "name": "value"
			    },
			    "actuators": [
			        {
			            "uuid": "value",
			            "attributes": {
			                "name": "value"
			            },
			            "parameters": {
			                "variable": "status"
			            }
			        }
			    ],
			    "sensors": [
			        {
			            "uuid": "value",
			            "attributes": {
			                "name": "value"
			            },
			            "readings": {
			                "unit": "symbol",
			                "values": [
			                    "value",
			                    "value"
			                ]
			            },
			            "options": {
			                "parameter": "value"
			            }
			        }
			    ]
			}*/
			
			content = response.toJSONString();
			
		} else {
			content = "404 NOT FOUND";
		}
		
		return content;

	}
	
	public static String updateActuator(String _uuid,String _actuator,String _parameter, String _value) {
		String responseCode = "204 CHANGED";
		
		if(!_uuid.trim().contentEquals(uuid)) {
			responseCode = "404 NOT FOUND";
		} else {
			if (!_actuator.trim().contentEquals("power_switch")) {
				responseCode = "402 BAD OPTION ";
			} else {
				if (!_parameter.trim().contentEquals("relay")) {
					responseCode = "402 BAD OPTION ";
				} else {
					if (_value.trim().contentEquals("true") || _value.trim().contentEquals("false") ) {
					} else {
						responseCode = "405 METHOD NOT ALLOWED";
					}
					
				}
			}
		}
		
		
		return responseCode;
	}
	
	public static String uploadFile(File _file) {
		String responseCode = "201 CREATED";
		
		return responseCode;
	}
}
