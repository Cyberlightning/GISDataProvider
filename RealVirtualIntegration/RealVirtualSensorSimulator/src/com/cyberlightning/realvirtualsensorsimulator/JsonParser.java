package com.cyberlightning.realvirtualsensorsimulator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.location.Location;
import android.provider.Settings.Secure;

public abstract class JsonParser {
	
	private final String deviceId = Secure.getString((MainActivity.getAppContext().getContentResolver()), Secure.ANDROID_ID);
	
	private JSONObject createFromLocation(Location event) {
		
		JSONArray values = new JSONArray();
		JSONObject device = new JSONObject();
		JSONObject properties=  new JSONObject();
		
		try {
			
			properties.put("type", "TYPE_GPS");
			
			values.put(event.getLatitude());
			values.put(event.getLongitude());
			if (event.hasAltitude()) values.put(event.getAltitude());
			
			device.put("device_id",  deviceId);
			device.put("device_properties", properties);
			//device.put("event_timestamp", getTimeStamp());
			if (event.hasAccuracy())device.put("event_accuracy", event.getAccuracy());
			device.put("event_values", values);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return device;
	}
	
	private JSONObject createFromSensorEvent(SensorEvent event) {
		
		JSONArray values = new JSONArray();
		JSONObject device = new JSONObject();
		JSONObject properties=  new JSONObject();
		
		try {
			
			properties.put("type", this.resolveDeviceById(event.sensor.getType()));
			properties.put("version", event.sensor.getVersion());
			properties.put("vendor", event.sensor.getVendor());
			properties.put("range", event.sensor.getMaximumRange());
			properties.put("delay", event.sensor.getMinDelay());
			properties.put("power", event.sensor.getPower());
			properties.put("resolution", event.sensor.getResolution());
			for (float value : event.values) {
				values.put(value);
			}
			device.put("device_id", deviceId );
			device.put("device_properties", properties);
			device.put("device_uptime", event.timestamp);
			//device.put("event_timestamp", getTimeStamp());
			device.put("event_accuracy", event.accuracy);
			device.put("event_values", values);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return device;
	}
	
	private String resolveDeviceById(int _id) {
		String name = null;
		switch(_id){
			case Sensor.TYPE_ACCELEROMETER: name = "TYPE_ACCELEROMETER";
				break;
			case Sensor.TYPE_AMBIENT_TEMPERATURE:name = "TYPE_AMBIENT_TEMPERATURE";
				break;
			case Sensor.TYPE_GAME_ROTATION_VECTOR:name = "TYPE_GAME_ROTATION_VECTOR";
				break;
			case Sensor.TYPE_GRAVITY:name = "TYPE_GRAVITY";
				break;
			case Sensor.TYPE_GYROSCOPE:name = "TYPE_GYROSCOPE";
				break;
			case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:name = "TYPE_GYROSCOPE_UNCALIBRATED";
				break;
			case Sensor.TYPE_LIGHT:name = "TYPE_LIGHT";
				break;
			case Sensor.TYPE_LINEAR_ACCELERATION:name = "TYPE_LINEAR_ACCELERATION";
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:name = "TYPE_MAGNETIC_FIELD";
				break;
			case Sensor.TYPE_PRESSURE:name = "TYPE_PRESSURE";
				break;
			case Sensor.TYPE_PROXIMITY:name = "TYPE_PROXIMITY";
				break;
			case Sensor.TYPE_SIGNIFICANT_MOTION:name = "TYPE_SIGNIFICANT_MOTION";
				break;
			case Sensor.TYPE_ORIENTATION:name = "TYPE_ORIENTATION";
				break;
			
			}
			
			return name;
		}

}
