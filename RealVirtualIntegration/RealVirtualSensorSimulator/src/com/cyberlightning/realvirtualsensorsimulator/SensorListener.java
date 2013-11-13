package com.cyberlightning.realvirtualsensorsimulator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
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


	private ConcurrentLinkedQueue<SensorEvent> sensorEventQueue= new ConcurrentLinkedQueue<SensorEvent>();
	private IMainActivity application;
	private List<Sensor> deviceSensors;
	private Location location;
	private Thread thread;
	
	private boolean suspendFlag = true;
	private boolean destroyFlag = false;

	
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
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return;
					}
	            }
	            if (destroyFlag) break; 
	        }
			if (this.sensorEventQueue.isEmpty()) continue;
			this.sendMessage(JsonParser.createFromSensorEvent(this.sensorEventQueue.poll(), location));
			
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
	
	private void registerSensorListeners(){
			
		this.deviceSensors = ((SensorManager) this.application.getContext().getApplicationContext().getSystemService(Context.SENSOR_SERVICE)).getSensorList(Sensor.TYPE_ALL);

		for (Sensor sensor : this.deviceSensors) {
			((SensorManager) this.application.getContext().getApplicationContext().getSystemService(Context.SENSOR_SERVICE)).registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
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
	
	private void sendMessage(String _payload) { //TODO just a stub
		setChanged();
		notifyObservers(Message.obtain(null, ClientSocket.MESSAGE_TYPE_OUTBOUND, _payload.toString()));
	}
		
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

	
	@Override
	public void onSensorChanged(SensorEvent _event) { 
		this.sensorEventQueue.offer(_event);
		this.wakeThread();
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
