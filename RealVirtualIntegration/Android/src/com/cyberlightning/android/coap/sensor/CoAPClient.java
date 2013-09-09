package com.cyberlightning.android.coap.sensor;




import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.cyberlightning.android.coapclient.R;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.widget.TextView;


public class CoAPClient extends Activity implements SensorEventListener {

    private TextView sensorsList, headerView;
    private String deviceID;
	private List<Sensor> deviceSensors;
	private JSONObject devices = new JSONObject();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_coapclient);
		
		this.deviceID = Secure.getString(getBaseContext().getContentResolver(), Secure.ANDROID_ID);
		this.detectSensors();

	    headerView = (TextView) findViewById(R.id.displayHeader);
		sensorsList = (TextView) findViewById(R.id.sensorsList);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.co_apclient, menu);
		return true;
	}
	
	private void registerSensorListeners(){
		
		this.deviceSensors = ((SensorManager) getSystemService(Context.SENSOR_SERVICE)).getSensorList(Sensor.TYPE_ALL);

		for (Sensor sensor : this.deviceSensors) {
			((SensorManager) getSystemService(Context.SENSOR_SERVICE)).registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

		}	
	}
	
	private void detectSensors () {
		
		for (Sensor sensor : this.deviceSensors) {
			
			JSONObject properties = new JSONObject();
			
			try {
				
				properties.put("type", sensor.getType());
				properties.put("version", sensor.getVersion());
				properties.put("vendor", sensor.getVendor());
				properties.put("range", sensor.getMaximumRange());
				properties.put("delay", sensor.getMinDelay());
				properties.put("power", sensor.getPower());
				properties.put("resolution", sensor.getResolution());
				
				this.devices.put(sensor.getName(), properties); 
				
			} catch (JSONException e) {
				//TODO auto-generated method stub
			}
		}
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		
	}
	
	protected void onResume() {
		super.onResume();
		this.registerSensorListeners();
	}
	
	protected void onPause() {
		super.onPause();
		((SensorManager) getSystemService(Context.SENSOR_SERVICE)).unregisterListener(this);
	}
    
}
