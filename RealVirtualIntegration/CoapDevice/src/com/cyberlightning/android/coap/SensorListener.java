package com.cyberlightning.android.coap;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Message;
import android.provider.Settings.Secure;


public class SensorListener implements SensorEventListener,ISensorListener  {

	private Activity context;
	private List<Sensor> deviceSensors;
	private String deviceID;
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";

	
	public SensorListener(Activity _parent) {
		this.context = _parent;
		this.deviceID = Secure.getString(this.context.getApplicationContext().getContentResolver(), Secure.ANDROID_ID);
		this.registerSensorListeners();
	}
	
	
	private void registerSensorListeners(){
			
		this.deviceSensors = ((SensorManager) this.context.getApplicationContext().getSystemService(Context.SENSOR_SERVICE)).getSensorList(Sensor.TYPE_ALL);

		for (Sensor sensor : this.deviceSensors) {
			((SensorManager) this.context.getApplicationContext().getSystemService(Context.SENSOR_SERVICE)).registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
		}	
	}
	private void unregisterSpecificSensor(int _type) {
		for (Sensor sensor : this.deviceSensors) {
			if (sensor.getType() ==_type) ((SensorManager) this.context.getApplicationContext().getSystemService(Context.SENSOR_SERVICE)).unregisterListener(this, sensor);
		}	
	}
	private void unregisterAllSensors() {
		((SensorManager) this.context.getApplicationContext().getSystemService(Context.SENSOR_SERVICE)).unregisterListener(this);
	}
		
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}
	
	@SuppressLint("SimpleDateFormat")
	private String getTimeStamp() {
		return  new SimpleDateFormat(DATE_FORMAT).format(new Date(System.currentTimeMillis()));
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) { //TODO split to multiple methods
			
		JSONObject device = new JSONObject();
		JSONObject properties = new JSONObject();			
		JSONArray values = new JSONArray();
		
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
			
			
			device.put("device_id", this.deviceID );
			device.put("device_properties", properties);
			device.put("device_uptime", event.timestamp);
			device.put("event_timestamp", this.getTimeStamp());
			device.put("event_accuracy", event.accuracy);
			device.put("event_values", values);
				
			} catch (JSONException e) {
				//TODO auto-generated method stub
			}
			
			Message message = new Message();
			message.what = 4;
			message.arg2 = event.sensor.getType(); //for UI
			message.obj = device.toString();
			
			

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


		@Override
		public void pause() {
			this.unregisterAllSensors();
			
		}


		@Override
		public void resume() {
			this.registerSensorListeners();
			
		}


		@Override
		public void stopListeningToSensor(int _type) {
			this.unregisterSpecificSensor(_type);
			
		}
	
	
}
