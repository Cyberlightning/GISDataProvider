package com.cyberlightning.realvirtualsensorsimulator;


import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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


public class SensorListener extends Observable implements SensorEventListener,ISensorListener,Runnable  {


	private HashMap<String,SensorEvent> copy = new HashMap<String,SensorEvent>();
	private ConcurrentLinkedQueue<SensorEvent> eventBuffer = new ConcurrentLinkedQueue<SensorEvent>();
	private IMainActivity application;
	private List<Sensor> deviceSensors;
	private Location location;
	private Thread thread;
	
	private boolean suspendFlag = true;
	private boolean destroyFlag = false;
	private volatile boolean isBusy = false;

	public static final long SENSOR_EVENT_INTERVAL = 2000;
    
	public SensorListener(MainActivity _activity) {
		this.application = _activity;
		this.registerSensorListeners();
		this.thread = new Thread(this);
		this.thread.start();
	}
	
	@Override
	public void run() {
		
		
		while(true) {
			
			synchronized(this) {
	            while(suspendFlag && !destroyFlag) {
					try {
						wait();
						isBusy = true;
						Thread.sleep(SENSOR_EVENT_INTERVAL); //TODO check whether can be done better
						
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return;
					}
	            }
	            if (destroyFlag) break; 
	        }
			
		
			
			while(!this.eventBuffer.isEmpty()) {
				String key = JsonParser.resolveSensorTypeById(this.eventBuffer.peek().sensor.getType());
				if (key != null){
					copy.put(key, this.eventBuffer.poll());
				}else {
					this.eventBuffer.poll();
				}
			}
			this.sendMessage(JsonParser.createFromSensorEvent(copy, location));
			this.suspendThread();
			isBusy = false;
			
		}
		return;
	}
	
	public void suspendThread() {
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
		SharedPreferences settings = this.application.getContext().getSharedPreferences(SettingsFragment.PREFS_NAME, 0);
		Set<String> sensors = settings.getStringSet(SettingsFragment.SHARED_SENSORS, defaultValues);
		
		return sensors;
	}
	
	private void registerSensorListeners(){
			
		this.deviceSensors = ((SensorManager) this.application.getContext().getApplicationContext().getSystemService(Context.SENSOR_SERVICE)).getSensorList(Sensor.TYPE_ALL);
		Set<String> selectedSensors = this.loadSettings();
		
		for (Sensor sensor : this.deviceSensors) {
			if (selectedSensors.contains(JsonParser.resolveSensorTypeById(sensor.getType()))) {
				((SensorManager) this.application.getContext().getApplicationContext().getSystemService(Context.SENSOR_SERVICE)).registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
				
			}
		}
		
		LocationManager locationManager = (LocationManager)this.application.getContext().getSystemService(Context.LOCATION_SERVICE);
		if (this.application.getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)) {
			LocationListener locationListener = new GpsListener();  
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, locationListener);	
		} else {
			LocationListener locationListener = new GpsListener();  
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
			Criteria criteria = new Criteria();
			criteria.setAccuracy(0);
			String bestProvider = locationManager.getBestProvider(criteria, true);
			this.location= locationManager.getLastKnownLocation(bestProvider); 
			//TODO handle location if gotten
		}
	}
	private void unregisterSpecificSensor(int _type) {
		for (Sensor sensor : this.deviceSensors) {
			if (sensor.getType() ==_type) ((SensorManager) this.application.getContext().getApplicationContext().getSystemService(Context.SENSOR_SERVICE)).unregisterListener(this, sensor);
		}	
	}
	private void unregisterAllSensors() {
		((SensorManager) this.application.getContext().getApplicationContext().getSystemService(Context.SENSOR_SERVICE)).unregisterListener(this);
	}
	
	private void sendMessage(String _payload) {
		Message msg = Message.obtain(null, MainActivity.MESSAGE_FROM_SENSOR_LISTENER, _payload.toString());
		setChanged();
		notifyObservers(Message.obtain(null,  MainActivity.MESSAGE_FROM_SENSOR_LISTENER, _payload.toString()));
		msg.setTarget(this.application.getTarget());
		msg.sendToTarget();
	}
		
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

	
	@Override
	public void onSensorChanged(SensorEvent _event) { 
		if(!isBusy) {
			this.eventBuffer.offer(_event);
			if(this.eventBuffer.size() > 200) {
				this.wakeThread();
			}
			
		}
	}
	
	@Override
	public void pause() {
		this.unregisterAllSensors();
		this.suspendThread();
	}


	@Override
	public void resume() {
		this.registerSensorListeners();
		this.wakeThread();
	}
	
	@Override
	public void end() {
		this.unregisterAllSensors();
		this.destroy();
	}


	@Override
	public void toggleSensor(int _type) {
		this.unregisterSpecificSensor(_type);	
	}

	@Override
	public void changeBroadCastInterval(int _duration) {
		  //TODO Auto-generated text block
	}

		
	/**
	 * 
	 * @author tomi
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

}
