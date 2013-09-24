package com.cyberlightning.android.coap;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

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


public class SensorListener extends Observable implements SensorEventListener,ISensorListener,Runnable  {

	private Activity context;
	private CopyOnWriteArrayList<SensorEvent> highPriorityEvents = new CopyOnWriteArrayList<SensorEvent>();
	private CopyOnWriteArrayList<SensorEvent> lowPriorityEvents = new CopyOnWriteArrayList<SensorEvent>();
	private ConcurrentLinkedQueue<JSONObject> compressedLowPriority = new ConcurrentLinkedQueue<JSONObject>();
	private HashMap<String,Boolean> priorityList = new HashMap<String,Boolean>();
	private List<Sensor> deviceSensors;
	private String deviceID;
	
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";
	private long broadcastInterval = 12000;
	private static final int COMPRESSION_THRESHOLD = 200;
	
	private volatile boolean hasHighPriority = false;
	private volatile boolean isRunningCompression = false;

	
	public SensorListener(Activity _parent) {
		this.context = _parent;
		this.deviceID = Secure.getString(this.context.getApplicationContext().getContentResolver(), Secure.ANDROID_ID);
		this.registerSensorListeners();
	}
	
	@Override
	public void run() {
		
		long lastRun = 0;
		
		while(true) {
			Iterator<SensorEvent> i = highPriorityEvents.iterator();
			while(i.hasNext()) {
				JSONObject jsonObject = createNewJSONObject(i.next());
				sendMessage(jsonObject);
				i.remove();
			}
			this.hasHighPriority = false;
			
			while(!this.compressedLowPriority.isEmpty()) {
				if(this.hasHighPriority) continue;
				sendMessage(this.compressedLowPriority.poll());
				
			}
			if(!((System.currentTimeMillis() - lastRun) < this.broadcastInterval)) continue;
			lastRun = System.currentTimeMillis();
			Iterator<SensorEvent> j = lowPriorityEvents.iterator();
			while(j.hasNext()) {
				if(this.hasHighPriority) continue;
				JSONObject jsonObject = createNewJSONObject(i.next());
				sendMessage(jsonObject);
			}
		}
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
	public void onSensorChanged(SensorEvent _event) { 
			
		if(this.priorityList != null) {
			if(this.priorityList.get(_event.sensor.getName()) && !this.priorityList.isEmpty()) {
				this.highPriorityEvents.add(_event);
				this.hasHighPriority = true;
			} else {
				this.lowPriorityEvents.add(_event);
				if (this.lowPriorityEvents.size() > COMPRESSION_THRESHOLD && !this.isRunningCompression) {
					compressSensorEvents();
				}
			}
		} else {
			this.lowPriorityEvents.add(_event);
			if (this.lowPriorityEvents.size() > COMPRESSION_THRESHOLD && !this.isRunningCompression) {
				compressSensorEvents();
			}
		}
		
	}
	
	/** This class is a local thread that compresses events of same sensor type to a single JSON object */
	private class CompressionRoutine implements Runnable {

		@Override
		public void run() {
			
			HashMap<String,JSONObject> tempHolder = new HashMap<String,JSONObject>();
			Iterator<SensorEvent> i = lowPriorityEvents.iterator();
			JSONObject jsonObject;
			JSONArray jsonArray;
			
			while (i.hasNext()) {
				SensorEvent event = i.next();
				
				if(tempHolder.containsKey(event.sensor.getName())){
					jsonObject = tempHolder.get(event.sensor.getType());
					
					try {
						jsonArray = jsonObject.getJSONArray("event_values");
						jsonObject.remove("event_values");
						
						for (float value : event.values) {
							jsonArray.put(value);
						}
						jsonObject.put("event_values", jsonArray);
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
				}else {
					tempHolder.put(event.sensor.getName(), createNewJSONObject(event));	
				}
			
			i.remove();	
			}

			Iterator<String> k = (tempHolder.keySet()).iterator();
			while (k.hasNext()) {
				compressedLowPriority.add(tempHolder.get(k.next()));
			}
			isRunningCompression = false;
			return;
		}
		
	}
	
	private JSONObject createNewJSONObject(SensorEvent event) {
		
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
			device.put("device_id", this.deviceID );
			device.put("device_properties", properties);
			device.put("device_uptime", event.timestamp);
			device.put("event_timestamp", this.getTimeStamp());
			device.put("event_accuracy", event.accuracy);
			device.put("event_values", values);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return device;
	}
	
	private void compressSensorEvents() {
		Runnable compressor = new CompressionRoutine();
		Thread thread = new Thread(compressor);
		this.isRunningCompression = true;
		thread.start();
	}
	
	private void sendMessage(JSONObject _payload) { //TODO just a stub
		Message message = new Message();
		message.what = 4;
		//message.arg2 = event.sensor.getType(); //for UI
		message.obj = _payload.toString();
		notifyObservers(message);
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
