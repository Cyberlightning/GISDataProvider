package com.cyberlightning.android.coap.sensor;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cyberlightning.android.coap.StaticResources;
import com.cyberlightning.android.coap.service.CoapService;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Settings.Secure;

public class SensorListener extends Thread implements SensorEventListener  {

	private Context context;
	private List<Sensor> deviceSensors;
	private String deviceID;
	private static Messenger messenger;
	
	public SensorListener(Context _context, Messenger _messenger) {
		this.context = _context;
		messenger = _messenger;
	}
	
	@Override 
	public void run() {
		this.deviceID = Secure.getString(this.context.getApplicationContext().getContentResolver(), Secure.ANDROID_ID);
		this.registerSensorListeners();
		
	}
	

	
	private void registerSensorListeners(){
			
		this.deviceSensors = ((SensorManager) this.context.getApplicationContext().getSystemService(Context.SENSOR_SERVICE)).getSensorList(Sensor.TYPE_ALL);

		for (Sensor sensor : this.deviceSensors) {
			((SensorManager) this.context.getApplicationContext().getSystemService(Context.SENSOR_SERVICE)).registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
		}	
	}
		
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSensorChanged(SensorEvent event) { //TODO split to multiple methods
			
		JSONObject device = new JSONObject();
		JSONObject properties = new JSONObject();			
		JSONArray values = new JSONArray();

		try {
				
			properties.put("type", event.sensor.getType());
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
			device.put("event_time_stamp", event.timestamp);
			device.put("event_accuracy", event.accuracy);
			device.put("event_values", values);
				
			} catch (JSONException e) {
				//TODO auto-generated method stub
			}
			
			Message message = new Message();
			message.what = CoapService.SEND_TO_WEBSERVER;
			message.obj = (Object) device;
			
			try {
				messenger.send(message);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			
		}
}
