package com.cyberlightning.android.coap.sensor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;



import com.cyberlightning.android.coapclient.R;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;


public class CoAPClient extends Activity implements SensorEventListener{

  
    
    private static final String SERVER_ADDRESS = "10.0.2.2";
    private static final int PORT=44444;
    private static final String SERVER_ADDRESS_FULL = "http://10.0.2.2:44444";
    
    static int counter = 0;

    TextView sensorsList, headerView;
    private SensorManager mSensorManager;
    StringBuffer buffer = new StringBuffer();
    boolean success = false;
  
    Map<Long, String> dataStore = new HashMap<Long, String>();
    
    
    int samplingRunDuration = 0;
    int sendingRunDuration = 0;
    
    private Sensor mAccelerometer;
	private Sensor mAmbientTemprature;
	private Sensor mGravity;
	private Sensor mGyroscope;
	private Sensor mLight; 
	private Sensor mLinearAcceleration;
	private Sensor mMagneticField;
	private Sensor mPressure;
	private Sensor mProximity;
	private Sensor mHumidity;
	private Sensor mRotationVector;
	
	private String deviceID;
	
	public List<Sensor> deviceSensors;
	
	boolean accelerometerFlag = false;
	boolean ambientTempratureFlag = false;
	boolean gravityFlag = false;
	boolean gyroscopeFlag = false;
	boolean lightFlag = false;
	boolean linearAccelerationFlag = false;
	boolean magneticFlag = false;
	boolean pressureFlag = false;
	boolean proximityFlag = false;
	boolean humidityFlag = false;
	boolean rotationVectorFlag = false;
    
    float[] accelerometerReading;
    float ambientTempratureReading;
    float[] gravityReading;
    float[] gyroscopeReading;
    float lightReading;
    float[] linearAccelerationReading;
    float[] magneticFieldReading;
    float pressureReading;
    float proximityReading;
    float relativeHumidityReading;
    float[] rotationVectorReading;
    
    
    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_coapclient);
		
		this.detectSensors();
		this.deviceID = Secure.getString(getBaseContext().getContentResolver(), Secure.ANDROID_ID);
		

	    headerView = (TextView) findViewById(R.id.displayHeader);
		sensorsList = (TextView) findViewById(R.id.sensorsList);
	    sensorsList.setText(buffer.toString());

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.co_apclient, menu);
		return true;
	}
	
	
	
	private void detectSensors() {
		
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		this.deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
		
		if(mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
			accelerometerFlag = true;
			mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		} if(mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null) {
			ambientTempratureFlag = true;
			mAmbientTemprature = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
	    } if(mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) {
			gravityFlag = true;
			mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
	    } if(mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
			gyroscopeFlag = true;
			mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
	    } if(mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
			lightFlag = true;
			mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
	    } if(mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
			linearAccelerationFlag = true;
			mLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
	    } if(mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
			magneticFlag = true;
			mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	    } if(mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) != null) {
			pressureFlag = true;
			mPressure= mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);	
		} if(mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null) {
		  proximityFlag = true;
		  mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
	    } if(mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY) != null) {
			humidityFlag = true;
			mHumidity = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
	    } if(mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null) {
			rotationVectorFlag = true;
			mRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
	    } 
	}
	
	public void sendMessage(View view) throws InterruptedException {
		headerView.setText("Seding message....");
		sensorsList.setText("");
		headerView.setText("");
		view.setEnabled(false);
		
		String[] s = new String[3];
		s[0] = this.SERVER_ADDRESS_FULL;
		//new SimpleHttpRequestTask().execute(s,null,null);
		
	}

	public String getSensorType(int typeNum) {
		String sensorType;
		
		switch(typeNum) {
		  case 1 : sensorType = "TYPE_ACCELEROMETER";
		           break;
		  case 13 : sensorType = "TYPE_AMBIENT_TEMPRATURE";
                   break;
		  case 9 : sensorType = "TYPE_GRAVITY";
                   break;
		  case 4 : sensorType = "TYPE_GYROSCOPE";
                   break;
		  case 5 : sensorType = "TYPE_LIGHT";
                   break;
		  case 10 :sensorType = "TYPE_LINEAR_ACCELERATION";
                   break;
		  case 2 : sensorType = "TYPE_MAGNETIC_FIELD";
                   break;
		  case 3 : sensorType = "TYPE_ORIENTATION";
                   break;
		  case 6 : sensorType = "TYPE_PRESSURE";
                   break;
		  case 8 : sensorType = "TYPE_PROXIMITY";
                   break;
		  case 12 :sensorType = "TYPE_RELATIVE_HUMIDITY";
                   break;
		  case 11 :sensorType = "TYPE_ROTATION_VECTOR";
                  break;
		  default: sensorType = "TYPE_INVALID";
		} 
		
	  return sensorType;
	}
	
	public void getSensorValues(String sensorName) {
		
		// Getting the appropriate values of the sensors.
		// Switch on strings not supported. Hence doing the not so pretty way.
		
		// Some sensors reading are three dimensional, hence a uniform format.
		// If readings are one dimensional, other elements of the vectors set to 0.
		float[] sensorReadings = {0, 0, 0};
		
		if(sensorName.compareTo("ACCELEROMETER".toLowerCase()) == 0) {
			sensorReadings = accelerometerReading;
			
		} else if (sensorName.compareTo("AMBIENT_TEMPRATURE".toLowerCase()) == 0) {
			sensorReadings[0] = ambientTempratureReading;
			
		} else if (sensorName.compareTo("GRAVITY".toLowerCase()) == 0) {
			sensorReadings = gravityReading;
			
		} else if (sensorName.compareTo("GYROSCOPE".toLowerCase()) == 0) {
			sensorReadings = gyroscopeReading;
			
		} else if (sensorName.compareTo("LIGHT".toLowerCase()) == 0) {
			sensorReadings[0] = lightReading;
			
		} else if (sensorName.compareTo("LINEAR_ACCELERATION".toLowerCase()) == 0 ) {
			sensorReadings = linearAccelerationReading;
			
		} else if (sensorName.compareTo("MAGNETIC_FIELD".toLowerCase()) == 0) {
			sensorReadings = magneticFieldReading;
			
		} else if (sensorName.compareTo("ORIENTATION".toLowerCase()) == 0) {
			
			
		} else if (sensorName.compareTo("PRESSURE".toLowerCase()) == 0) {
			sensorReadings[0] = pressureReading;
			
		} else if (sensorName.compareTo("PROXIMITY".toLowerCase()) == 0) {
			sensorReadings[0] = proximityReading;
			
		} else if (sensorName.compareTo("RELATIVE_HUMIDITY".toLowerCase()) == 0) {
			sensorReadings[0] = relativeHumidityReading;
			
		} else if (sensorName.compareTo("ROTATION_VECTOR".toLowerCase()) == 0) {
			sensorReadings = rotationVectorReading;
			
		}  
		
		String valueToPut = "X:"+ sensorReadings[0] + " Y:"+ sensorReadings[1] + " Z:"+ sensorReadings[2];
		
		Long timeStamp = System.currentTimeMillis();
		dataStore.put(timeStamp, valueToPut);
	}
	
	
	
	

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			accelerometerReading = event.values;
		}

		if(event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
			ambientTempratureReading = event.values[0];
		}

		if(event.sensor.getType() == Sensor.TYPE_GRAVITY) {
			gravityReading = event.values;
		}

		if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
			gyroscopeReading = event.values;
		}

		if(event.sensor.getType() == Sensor.TYPE_LIGHT) {
			lightReading = event.values[0];
		}

		if(event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
			linearAccelerationReading = event.values;
		}

		if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			magneticFieldReading = event.values;
		}

		if(event.sensor.getType() == Sensor.TYPE_PRESSURE) {
			pressureReading = event.values[0];
		}

		if(event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
			proximityReading = event.values[0];
		}

		if(event.sensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY) {
			relativeHumidityReading = event.values[0];
		}

		if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
			rotationVectorReading = event.values;
		}
		
	}
	
	protected void onResume() {
		super.onResume();
		
		if(accelerometerFlag) {
			mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
		}
		
		if(ambientTempratureFlag) {
			mSensorManager.registerListener(this, mAmbientTemprature, SensorManager.SENSOR_DELAY_UI);
		}
		
		if(gravityFlag) {
			mSensorManager.registerListener(this, mGravity, SensorManager.SENSOR_DELAY_UI);
		}
		
		if(gyroscopeFlag) {
			mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_UI);
		}
		
		if(lightFlag) {
			mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_UI);
		}
		
		if(linearAccelerationFlag) {
			mSensorManager.registerListener(this, mLinearAcceleration, SensorManager.SENSOR_DELAY_UI);
		}
		
		if(magneticFlag) {
			mSensorManager.registerListener(this, mMagneticField, SensorManager.SENSOR_DELAY_UI);
		}
		
		if(pressureFlag) {
			mSensorManager.registerListener(this, mPressure, SensorManager.SENSOR_DELAY_UI);	
		} 
		
		if(proximityFlag) {
			mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
		}
		
		if(humidityFlag) {
			mSensorManager.registerListener(this, mHumidity, SensorManager.SENSOR_DELAY_UI);
		}
		
		if(rotationVectorFlag) {
			mSensorManager.registerListener(this, mRotationVector, SensorManager.SENSOR_DELAY_UI);
		}
		
	}
	
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}
    
}
