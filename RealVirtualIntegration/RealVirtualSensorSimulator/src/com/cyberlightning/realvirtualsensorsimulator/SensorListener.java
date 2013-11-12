package com.cyberlightning.realvirtualsensorsimulator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.ConcurrentLinkedQueue;

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
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Message;
import android.provider.Settings.Secure;


public class SensorListener extends Observable implements SensorEventListener,ISensorListener,Runnable  {

	private Activity context;
	private ConcurrentLinkedQueue<JSONObject> highPriorityEvents = new ConcurrentLinkedQueue<JSONObject>();
	private ConcurrentLinkedQueue<JSONObject> lowPriorityEvents = new ConcurrentLinkedQueue<JSONObject>();
	private volatile HashMap<String,SensorEvent> sensorEventTable = new HashMap<String,SensorEvent>();
	private HashMap<String,Boolean> priorityList = new HashMap<String,Boolean>();
	private List<Sensor> deviceSensors;
	private String deviceID;
	
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";
	
	private volatile long broadcastInterval = 12000;
	private volatile boolean hasHighPriority = false;
	private volatile boolean isHandlingLowPriorityEvents = false;

	
	public SensorListener(Activity _parent) {
		this.context = _parent;
		this.deviceID = Secure.getString(this.context.getApplicationContext().getContentResolver(), Secure.ANDROID_ID);
		this.registerSensorListeners();
	}
	
	@Override
	public void run() {
		
		
		while(true) {
			
			while(!highPriorityEvents.isEmpty()) {
				JSONObject jsonObject  = highPriorityEvents.poll();
				if (jsonObject != null)sendMessage(jsonObject);
				
			}
			this.hasHighPriority = false;
			
			while(!this.lowPriorityEvents.isEmpty()) {
				if(this.hasHighPriority) continue;
				JSONObject j = this.lowPriorityEvents.poll();
				if(j != null) sendMessage(j);
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
	public static String getTimeStamp() {
		return  new SimpleDateFormat(DATE_FORMAT).format(new Date(System.currentTimeMillis()));
	}
	
	@Override
	public void onSensorChanged(SensorEvent _event) { 
		
		if(this.priorityList.containsKey(_event.sensor.getName())) {
			JSONObject jsonObject = createFromSensorEvent(_event);
			this.highPriorityEvents.offer(jsonObject);
			this.hasHighPriority = true;
			
		} else {
			this.sensorEventTable.put(_event.sensor.getName(), _event);
			if(!this.isHandlingLowPriorityEvents) this.startSensorEventUpdaterRoutine();
		}
	}
	
	/** This class is a local thread that compresses events of same sensor type to a single JSON object */
	private class SensorEventUpdaterRoutine implements Runnable {

		@Override
		public void run() {

			while(isHandlingLowPriorityEvents) {
				
				try {
					Thread.sleep(broadcastInterval);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				HashMap<String,SensorEvent> tempSensorEventTable = sensorEventTable;
				Iterator<String> i = tempSensorEventTable.keySet().iterator();
				while(i.hasNext()) {
					JSONObject j = createFromSensorEvent(tempSensorEventTable.get(i.next())); //TODO check type cast for Location objects
					lowPriorityEvents.offer(j);
				}
				
			}
			isHandlingLowPriorityEvents = false;
			return;
		}
		
	}
	
	private JSONObject createFromLocation(Location event) {
		
		JSONArray values = new JSONArray();
		JSONObject device = new JSONObject();
		JSONObject properties=  new JSONObject();
		
		try {
			
			properties.put("type", "TYPE_GPS");
			
			values.put(event.getLatitude());
			values.put(event.getLongitude());
			if (event.hasAltitude()) values.put(event.getAltitude());
			
			device.put("device_id", this.deviceID );
			device.put("device_properties", properties);
			device.put("event_timestamp", getTimeStamp());
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
			device.put("device_id", this.deviceID );
			device.put("device_properties", properties);
			device.put("device_uptime", event.timestamp);
			device.put("event_timestamp", getTimeStamp());
			device.put("event_accuracy", event.accuracy);
			device.put("event_values", values);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return device;
	}
	
	private void startSensorEventUpdaterRoutine() {
		Runnable compressor = new SensorEventUpdaterRoutine();
		Thread thread = new Thread(compressor);
		this.isHandlingLowPriorityEvents = true;
		thread.start();
	}
	
	private void sendMessage(JSONObject _payload) { //TODO just a stub
		setChanged();
		notifyObservers(Message.obtain(null, ClientSocket.MESSAGE_TYPE_OUTBOUND, _payload.toString()));
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
		
		private void togglePriority(String _sensorId) {
			
			if (this.priorityList.containsKey(_sensorId)) {
				this.priorityList.put(_sensorId, !this.priorityList.get(_sensorId)); //Toggles boolean value
			} else {
				this.priorityList.put(_sensorId, true); //low priority event is being changed to high priority by default
			}
		
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

		@Override
		public void changeBroadCastInterval(int _duration) {
			this.broadcastInterval = _duration*1000;
			
		}

		@Override
		public void changeSensorPriority(String _sensorId) {
			this.togglePriority(_sensorId);
			
		}

		@Override
		public void toggleGps(boolean _isHighPriority, boolean _hasGps) {
				LocationManager locM = (LocationManager)this.context.getSystemService(Context.LOCATION_SERVICE);
			if (_hasGps) {
				LocationListener locationListener = new GpsListener();  
				locM.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, locationListener);	
			} else {
				LocationListener locationListener = new GpsListener();  
				locM.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
				Criteria criteria = new Criteria();
				criteria.setAccuracy(0);
				String bestProvider = locM.getBestProvider(criteria, true);
				Location location = locM.getLastKnownLocation(bestProvider); 
				JSONObject j = createFromLocation(location); //TODO check type cast for Location objects
				
				highPriorityEvents.offer(j);
				hasHighPriority = true;
				
			}
			
			this.priorityList.put("TYPE_GPS", _isHighPriority);
		}
		
		/*----------Listener class to get coordinates ------------- */
		private class GpsListener implements LocationListener {

			
		    @Override
		    public void onProviderDisabled(String provider) {
		    	//TODO Auto-generated text block
		    }

		    @Override
		    public void onProviderEnabled(String provider) {
		    	//TODO Auto-generated text block
		    }

			@Override
			public void onLocationChanged(Location location) {
				
				if(priorityList.containsKey("TYPE_GPS")) {
					JSONObject j = createFromLocation(location); //TODO check type cast for Location objects
					
					highPriorityEvents.offer(j);
					hasHighPriority = true;
					
				} 
				
			}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
				
			}
		}

		


		
	
	
}
