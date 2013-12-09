package com.cyberlightning.realvirtualsensorsimulator;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Set;

import com.cyberlightning.realvirtualsensorsimulator.interfaces.IMainActivity;
import com.cyberlightning.realvirtualsensorsimulator.interfaces.ISensorListener;
import com.cyberlightning.realvirtualsensorsimulator.staticresources.JsonParser;
import com.cyberlightning.realvirtualsensorsimulator.views.SettingsViewFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Message;
import android.view.Display;
import android.view.WindowManager;


public class SensorListener extends Observable implements SensorEventListener,ISensorListener,Runnable  {


	private ArrayList<ActuatorComponent> actuators =  new  ArrayList<ActuatorComponent>();
	private ArrayList<SensorEventObject> events;
	private IMainActivity application;
	private List<Sensor> deviceSensors;
	private Location location;
	private LocationListener locationListener;
	
	private String contextualLocation;
	private long sensorEventInterval;
	
	private boolean suspendFlag = true;
	private boolean destroyFlag = false;
	
	private volatile boolean isBusy = false;
	

	public static final long SENSOR_EVENT_INTERVAL = 4000;
    
	public SensorListener(MainActivity _activity) {
		this.application = _activity;
		Thread thread= new Thread(this);
		thread.start();
	}
	
	@Override
	public void run() {
		
		this.initializeActuators(); //this needs to be done only once
		while(true) {
			
			synchronized(this) {
	            while(suspendFlag && !destroyFlag) {
					try {
						wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
						return;
					}
	            }
	            if (destroyFlag) break; 
	        }
			this.unregisterAllSensors();

			if (!events.isEmpty()) {
				sendMessageToServer(JsonParser.createFromSensorEvent(events, location, contextualLocation,actuators));
				for (SensorEventObject o : events) {
					sendMessageToUI( JsonParser.getTimeStamp() + ": " + o.type);
				}
			}
			events.clear();
			new Thread((Runnable)new IntervalTimer()).start();
			this.registerSensorListeners();
			this.suspendThread();

		}
	
	}
	
	public synchronized void suspendThread() {
	      suspendFlag = true;
	}

	private synchronized void wakeThread() {
	      suspendFlag = false;
	      notify();
	}
	
	private synchronized void destroy() {
	      this.destroyFlag = true;
	      notify();   
	}
	private Set<String> loadSettings() {
		Set<String> defaultValues =  new HashSet<String>( this.deviceSensors.size());
		for (Sensor sensor: this.deviceSensors) {
			defaultValues.add(Integer.toString(sensor.getType()));
		}
		SharedPreferences settings = this.application.getContext().getSharedPreferences(SettingsViewFragment.PREFS_NAME, 0);
		Set<String> sensors = settings.getStringSet(SettingsViewFragment.SHARED_SENSORS, defaultValues);
		this.sensorEventInterval = settings.getLong(SettingsViewFragment.SHARED_INTERVAL,SENSOR_EVENT_INTERVAL);
		this.contextualLocation = settings.getString(SettingsViewFragment.SHARED_LOCATION, null);
		
		return sensors;
	}
	
	private void initializeActuators() {
		
		WindowManager wm = (WindowManager) application.getContext().getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		ActuatorComponent actuator = new ActuatorComponent();
		actuator.attributes.put("dimensions", "[" + size.x+","+size.y+"]");
		
		String[] values = {"marker1","marker2","marker3","marker4","marker6","marker7","marker8","marker9","marker10","marker11","marker12","marker13","marker14","marker15","marker15","marker16","marker17","marker18","marker19"};
		
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		for(String s : values) {
		    builder.append(s + ",");
		}
		builder.replace(builder.length()-1, builder.length(), "");
		builder.append("]");
		
		HashMap<String,Object> action = new HashMap<String,Object>();
		action.put("parameter", "viewstate");
		action.put("primitive", "array");
		action.put("unit", "string");
		action.put("value",  builder.toString());
		action.put("state", null);
		
		actuator.actions.add(action);
		
		HashMap<String,Object> param = new HashMap<String,Object>();
		
		param.put("name", "viewsize");
		param.put("unit", "percent");
		param.put("value","100");
	
		actuator.configuration.add(param);

		HashMap<String,Object> callback = new HashMap<String,Object>();
		callback.put("target", "viewstate");
		callback.put("return_type", "boolean");
		
		actuator.callbacks.add(callback);
		
		this.actuators.add(actuator);
		
	}
	
	private void registerGpsListener() {
		
		SharedPreferences settings = this.application.getContext().getSharedPreferences(SettingsViewFragment.PREFS_NAME, 0);
		boolean useGPS = settings.getBoolean(SettingsViewFragment.SHARED_GPS, false);
		
		if (useGPS) {
			LocationManager locationManager = (LocationManager)this.application.getContext().getSystemService(Context.LOCATION_SERVICE);
			if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
				this.application.showNoGpsAlert();
		    }else{
		    	this.locationListener = new GpsListener();  
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, this.locationListener);	
		    }
		}
	}
	
	private void unregisterGpsListener() {
		LocationManager locationManager = (LocationManager)this.application.getContext().getSystemService(Context.LOCATION_SERVICE);
		if (this.locationListener != null)locationManager.removeUpdates(this.locationListener);
	}
	
	private Integer registerSensorListeners(){
			
		this.deviceSensors = ((SensorManager) this.application.getContext().getApplicationContext().getSystemService(Context.SENSOR_SERVICE)).getSensorList(Sensor.TYPE_ALL);
		Set<String> selectedSensors = this.loadSettings();
		
		for (Sensor sensor : this.deviceSensors) {
			if (selectedSensors.contains(JsonParser.resolveSensorTypeById(sensor.getType()))) {
				((SensorManager) this.application.getContext().getApplicationContext().getSystemService(Context.SENSOR_SERVICE)).registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
				
			}
		}
		this.events = new ArrayList<SensorEventObject>(selectedSensors.size());
		return selectedSensors.size();
	}
		
	private void unregisterSpecificSensor(int _type) {
		for (Sensor sensor : this.deviceSensors) {
			if (sensor.getType() ==_type) ((SensorManager) this.application.getContext().getApplicationContext().getSystemService(Context.SENSOR_SERVICE)).unregisterListener(this, sensor);
		}	
	}
	private void unregisterAllSensors() {
		((SensorManager) this.application.getContext().getApplicationContext().getSystemService(Context.SENSOR_SERVICE)).unregisterListener(this);
	}
	
	private void sendMessageToServer(String _payload) {
		setChanged();
		notifyObservers(Message.obtain(null,  MainActivity.MESSAGE_FROM_SENSOR_LISTENER, _payload));	
	}
	
	private void sendMessageToUI(String _payload) {
		Message msg = Message.obtain(null, MainActivity.MESSAGE_FROM_SENSOR_LISTENER, _payload);
		msg.setTarget(this.application.getTarget());
		msg.sendToTarget();
	}
		
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

	
	@Override
	public void onSensorChanged(SensorEvent _event) { 
		if(!this.isBusy) {
			String type = JsonParser.resolveSensorTypeById(_event.sensor.getType());
			boolean contains = false;
			for (int i = 0 ; i < this.events.size(); i++) {
				if (this.events.get(i).type.contentEquals(type)){
					contains = true;
				}
			}
			if (!contains) this.events.add(new SensorEventObject(_event,type));
		}
	
	}
	
	@Override
	public void pause() {
		this.isBusy = true;
		this.suspendThread();
		this.unregisterAllSensors();
		this.unregisterGpsListener();
	}


	@Override
	public Integer resume() {
		this.isBusy = false;
		int numOfSensors = this.registerSensorListeners();
		if ( numOfSensors > 0) {
			this.wakeThread();
			this.registerGpsListener();
		}
		return numOfSensors;
	}
	
	@Override
	public void end() {
		this.isBusy = true;
		this.unregisterAllSensors();
		this.unregisterGpsListener();
		this.destroy();
	}


	@Override
	public void toggleSensor(int _type) {
		this.unregisterSpecificSensor(_type);	
	}

	@Override
	public void changeEventInterval(int _duration) {
		  //TODO Auto-generated text block
	}

		
	/**
	 * 
	 * @author Cyberlightning Ltd. <tomi.sarni@cyberlightning.com>
	 *
	 */
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
		public void onLocationChanged(Location _location) {
			location = _location;
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			//TODO Auto-generated text block
		}
	}
	
	private class IntervalTimer implements Runnable {

		@Override
		public void run() {
			
	
			try {
				Thread.sleep(sensorEventInterval);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (!isBusy)wakeThread();
			return;
		}
		
	}
	
	public class SensorEventObject {
		public SensorEvent event;
		public String type;
		
		public SensorEventObject(SensorEvent _event, String _type) {
			this.event = _event;
			this.type = _type;
		}
	}
	
	public class ActuatorComponent {
		public HashMap<String, Object> attributes = new HashMap<String,Object>();
		public ArrayList<HashMap<String, Object>> configuration = new  ArrayList<HashMap<String, Object>>();
		public ArrayList<HashMap<String, Object>> actions = new  ArrayList<HashMap<String, Object>>();
		public ArrayList<HashMap<String, Object>> callbacks = new  ArrayList<HashMap<String, Object>>();
	}

}
