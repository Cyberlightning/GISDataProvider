package com.cyberlightning.realvirtualsensorsimulator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.location.Location;

public abstract class JsonParser {
	
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";
	
	/*{
	    "9627f38e-51a1-4d87-9d1f-9790e01ecfef": {

	        "BC:6A:29:AB:E8:B5": {
	            "sensors": [
	                {
	                    "value": {
	                        "unit": "m/s2",
	                        "primitive": "double",
	                        "time": "2013-11-07 16:03",
	                        "values": [
	                            0,
	                            0.109375,
	                            -1.015625

	                        ]
	                    },
	                    "uuid": "f000aa10-0451-4000-b000-000000000000",
	                    "parameters": {
	                        "toggleable": "true",
	                        "options": "boolean"
	                    },
	                    "attributes": {
	                        "type": "accelerometer",
	                        "vendor": "Texas Instruments"
	                    }
	                },
	                {
	                    "value": {
	                        "unit": "Celsius",
	                        "primitive": "double",
	                        "time": "2013-11-07 16:03",
	                        "values": 22.8125

	                    },
	                    "uuid": "f000aa00-0451-4000-b000-000000000000",
	                    "parameters": {
	                        "toggleable": "true",
	                        "options": "boolean"
	                    },
	                    "attributes": {
	                        "type": "temperature",
	                        "vendor": "Texas Instruments"
	                    }
	                }
	            ],
	            "attributes": {
	                "location": [
	                    "65.567",
	                    "25.765"
	                ],
	                "name": "TI CC2541 Sensor"
	            }
	        }
	    }
	}*/
	
	
	public static String createFromSensorEvent(HashMap<String,SensorEvent> _sensorEvents, Location _location) {
		
		JSONObject wrapper = new JSONObject();
		JSONObject device = new JSONObject();
		JSONObject sensorWraper = new JSONObject();
		JSONObject attributes = new JSONObject();
		JSONArray sensors = new JSONArray();
		JSONArray location = new JSONArray();
		
		try {
			
			if (_location != null) {
				location.put(_location.getLatitude());
				location.put(_location.getLongitude());
			} else {
				location.put(65.5);
				location.put(25.3);
			}
			
			attributes.put("gps", location);
			attributes.put("name", MainActivity.deviceName);
			
			Iterator<String> keys = _sensorEvents.keySet().iterator();
			while (keys.hasNext()) {
				SensorEvent event = _sensorEvents.get(keys.next());
				JSONObject sensorAttrs = new JSONObject();
				JSONObject sensor = new JSONObject();
				
				JSONObject sensorParams = new JSONObject();
				JSONObject value = new JSONObject();
				
				sensorAttrs.put("type", resolveSensorTypeById(event.sensor.getType()));
				sensorAttrs.put("vendor", event.sensor.getVendor());
				sensorAttrs.put("power", event.sensor.getPower());
				sensorAttrs.put("name", event.sensor.getName());
				sensor.put("attributes", sensorAttrs);
				
				sensorParams.put("toggleable", "boolean");
				sensorParams.put("interval", "ms");
				sensor.put("parameters", sensorParams);
				
				value.put("values", resolveValues(event.values,event.sensor.getType()));
				value.put("time", getTimeStamp());
				value.put("primitive",resolvePrimitive(event.sensor.getType()));
				value.put("unit", resolveSensorUnitById(event.sensor.getType())); //TODO implement a more accurate and dynamic way
				
				sensor.put("value", value);
				sensors.put(sensor);
			}

			sensorWraper.put("sensors", sensors);
			sensorWraper.put("attributes", attributes);
			device.put(MainActivity.deviceId, sensorWraper);
			wrapper.put(MainActivity.deviceId, device);
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return wrapper.toString();
	}
	
	private static Object resolveValues (float[] _values, int _id) {
		
		if (!resolvePrimitive(_id).contentEquals("3DPoint")){
			return _values[0];
		} else {
			JSONArray values = new JSONArray();
			for(int i = 0; i < _values.length; i++) {
				try {
					values.put(_values[i]);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return values;
		}
	}
	
	@SuppressWarnings("deprecation")
	private static String resolvePrimitive(int _id) {
		String primitive = null;
		
		switch(_id){
			case Sensor.TYPE_ACCELEROMETER: primitive = "3DPoint";
				break;
			case Sensor.TYPE_AMBIENT_TEMPERATURE:primitive = "double";
				break;
			case Sensor.TYPE_GAME_ROTATION_VECTOR:primitive = "3DPoint";
				break;
			case Sensor.TYPE_GRAVITY:primitive = "3DPoint";
				break;
			case Sensor.TYPE_GYROSCOPE:primitive = "3DPoint";
				break;
			case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:primitive = "array";
				break;
			case Sensor.TYPE_LIGHT:primitive = "double";
				break;
			case Sensor.TYPE_LINEAR_ACCELERATION:primitive = "3DPoint";
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:primitive = "3DPoint";
				break;
			case Sensor.TYPE_PRESSURE:primitive = "double";
				break;
			case Sensor.TYPE_PROXIMITY:primitive = "double";
				break;
			case Sensor.TYPE_SIGNIFICANT_MOTION:primitive = "3DPoint";
				break;
			case Sensor.TYPE_ORIENTATION:primitive = "3DPoint";
				break;
			case Sensor.TYPE_RELATIVE_HUMIDITY: primitive = "double";
				break;
			
			}
		return primitive;
	}
	
	@SuppressLint("SimpleDateFormat")
	public static String getTimeStamp() {
		return  new SimpleDateFormat(DATE_FORMAT).format(new Date(System.currentTimeMillis()));
	}
	@SuppressWarnings("deprecation")
	public static String resolveSensorUnitById(int _id) {
		String unit = null;
		switch(_id){
			case Sensor.TYPE_ACCELEROMETER: unit = "m/s2";
				break;
			case Sensor.TYPE_AMBIENT_TEMPERATURE:unit = "celcius";
				break;
			case Sensor.TYPE_GAME_ROTATION_VECTOR:unit = "rad/s";
				break;
			case Sensor.TYPE_GRAVITY:unit = "m/s2";
				break;
			case Sensor.TYPE_GYROSCOPE:unit = "rad/s";
				break;
			case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:unit = "rad/s";
				break;
			case Sensor.TYPE_LIGHT:unit = "lx";
				break;
			case Sensor.TYPE_LINEAR_ACCELERATION:unit = "m/s2";
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:unit = "uT";
				break;
			case Sensor.TYPE_PRESSURE:unit = "hPa";
				break;
			case Sensor.TYPE_PROXIMITY:unit = "cm";
				break;
			case Sensor.TYPE_SIGNIFICANT_MOTION:unit = "significantmotion";
				break;
			case Sensor.TYPE_ORIENTATION:unit = "orientation";
				break;
			case Sensor.TYPE_RELATIVE_HUMIDITY: unit = "%";
				break;
			
			}
			
			return unit;
		}
	
	@SuppressWarnings("deprecation")
	public static String resolveSensorTypeById(int _id) {
		String name = null;
		switch(_id){
			case Sensor.TYPE_ACCELEROMETER: name = "accelerometer";
				break;
			case Sensor.TYPE_AMBIENT_TEMPERATURE:name = "temperature";
				break;
			case Sensor.TYPE_GAME_ROTATION_VECTOR:name = "gamerotationvector";
				break;
			case Sensor.TYPE_GRAVITY:name = "gravity";
				break;
			case Sensor.TYPE_GYROSCOPE:name = "gyroscope";
				break;
			case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:name = "gyroscopeuncalibrated";
				break;
			case Sensor.TYPE_LIGHT:name = "light";
				break;
			case Sensor.TYPE_LINEAR_ACCELERATION:name = "linearacceleration";
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:name = "magneticfield";
				break;
			case Sensor.TYPE_PRESSURE:name = "pressure";
				break;
			case Sensor.TYPE_PROXIMITY:name = "proximity";
				break;
			case Sensor.TYPE_SIGNIFICANT_MOTION:name = "significantmotion";
				break;
			case Sensor.TYPE_ORIENTATION:name = "orientation";
				break;
			case Sensor.TYPE_RELATIVE_HUMIDITY: name = "relativehumidity";
				break;
			
			}
			return name;
		}

}
