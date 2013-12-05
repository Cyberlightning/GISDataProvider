package com.cyberlightning.realvirtualsensorsimulator.views;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.cyberlightning.realvirtualsensorsimulator.ClientSocket;
import com.cyberlightning.realvirtualsensorsimulator.MainActivity;
import com.cyberlightning.realvirtualsensorsimulator.SensorListener;
import com.cyberlightning.realvirtualsensorsimulator.staticresources.JsonParser;
import com.example.realvirtualsensorsimulator.R;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

public class SettingsViewFragment extends Fragment implements OnClickListener {
	
	private Button saveButton;
	private Button exitButton;
	private Button resetButton;
	private CheckBox gpsCheckBox;
	private EditText addressTextField;
	private EditText intervalTextField;
	private EditText portTextField;
	private EditText locationTextField;
	private LinearLayout sensorListLeft;
	private LinearLayout sensorListRight;
	private LinearLayout settingsFragmentView;
	private Set<String> defaultValues;
	
	private String address;
	private String location;
	private int port;
	private long interval;
	
	public static final String PREFS_NAME = "RealVirtualInteraction";
	public static final String SHARED_SENSORS = "Sensors";
	public static final String SHARED_ADDRESS = "ServerAddress";
	public static final String SHARED_PORT = "ServerPort";
	public static final String SHARED_INTERVAL = "EventInterval";
	public static final String SHARED_LOCATION = "ContextualLocation";
	public static final String SHARED_GPS = "GpsLocation";

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (((MainActivity)getActivity()).isLandScape)this.settingsFragmentView = (LinearLayout) inflater.inflate(R.layout.settings_landscape , null);
		else this.settingsFragmentView = (LinearLayout) inflater.inflate(R.layout.settings , null);
		
		this.sensorListLeft = (LinearLayout) this.settingsFragmentView.findViewById(R.id.settings_sensorlist_left);
		this.sensorListRight = (LinearLayout) this.settingsFragmentView.findViewById(R.id.settings_sensorlist_right);
		this.addressTextField = (EditText) this.settingsFragmentView.findViewById(R.id.settings_address);
		this.portTextField = (EditText) this.settingsFragmentView.findViewById(R.id.settings_port);
		this.intervalTextField = (EditText) this.settingsFragmentView.findViewById(R.id.settings_interval);
		this.resetButton = (Button) this.settingsFragmentView.findViewById(R.id.settings_button_reset);
		this.resetButton.setOnClickListener(this);
		this.exitButton = (Button) this.settingsFragmentView.findViewById(R.id.settings_button_exit);
		this.exitButton.setOnClickListener(this);
		this.saveButton = (Button) this.settingsFragmentView.findViewById(R.id.settings_button_save);
		this.saveButton.setOnClickListener(this);
		this.gpsCheckBox = (CheckBox) this.settingsFragmentView.findViewById(R.id.settings_gps_location);
		this.gpsCheckBox.setOnClickListener(this);
		this.locationTextField = (EditText) this.settingsFragmentView.findViewById(R.id.settings_contextual_locations);
		setHasOptionsMenu(true);
        this.loadSettings();
        return this.settingsFragmentView;
    }
	
	private void loadSettings() {
	
		SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
		List<Sensor> availableSensors = ((SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE)).getSensorList(Sensor.TYPE_ALL);
		this.defaultValues =  new HashSet<String>(availableSensors.size());
		
		for (Sensor sensor: availableSensors) {
			this.defaultValues.add(Integer.toString(sensor.getType()));
		}
		
		boolean isLeft = true;
	    Set<String> sensors = settings.getStringSet(SHARED_SENSORS, this.defaultValues);
	 
	    for (String sensor : this.defaultValues) {
	    	
	    	int id = Integer.parseInt(sensor);
	    	
	    	CheckBox cb = new CheckBox(getActivity());
	    	cb.setTextColor(Color.BLACK);
	    	cb.setTextSize(11);
	    	cb.setText(JsonParser.resolveSensorTypeById(id));
	        cb.setId(id);
	        
	        if (sensors.contains(JsonParser.resolveSensorTypeById(id)))cb.setChecked(true);
	        if (isLeft) {
	        	this.sensorListLeft.addView(cb);
	        	isLeft = false;
	        } else {
	        	this.sensorListRight.addView(cb);
	        	isLeft = true;
	        }
	    }
	    
	    this.address= settings.getString(SHARED_ADDRESS, ClientSocket.SERVER_DEFAULT_ADDRESS);
	    this.addressTextField.setText(this.address);
	    
	    this.port = settings.getInt(SHARED_PORT, ClientSocket.SERVER_DEFAULT_PORT);
	    this.portTextField.setText(Integer.toString(this.port));
	    
	    this.interval = settings.getLong(SHARED_INTERVAL, SensorListener.SENSOR_EVENT_INTERVAL);
	    this.intervalTextField.setText(Long.toString(this.interval));
	    
	    this.location = settings.getString(SHARED_LOCATION, "");
	    if (this.location.length() > 0) this.locationTextField.setText(this.location);
	    
	   
	    if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)){
	    	this.gpsCheckBox.setEnabled(false);
	    } else {
	    	this.gpsCheckBox.setChecked(settings.getBoolean(SHARED_GPS, false));
	    }
	}


	@Override
	public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {
		
		((MenuItem)menu.findItem(R.id.menu_main)).setVisible(true);
		((MenuItem)menu.findItem(R.id.menu_settings)).setVisible(false);
		((MenuItem)menu.findItem(R.id.menu_settings)).setEnabled(false);
		//getActivity().invalidateOptionsMenu(); //cause stack overflow in Samsung galaxy 4.1.2
    	super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig); 

        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
        	this.saveState(false);
        	if(((MainActivity)getActivity()).isLandScape)((MainActivity)getActivity()).onSettingsItemClicked(false);
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        	this.saveState(false);
        	if(!((MainActivity)getActivity()).isLandScape)((MainActivity)getActivity()).onSettingsItemClicked(true);
        }
    }
	
	private void saveState(Boolean _showToast) {
		
		Set<String> sensors = new HashSet<String>(this.defaultValues.size());
		
		for (int i = 0; i < this.sensorListLeft.getChildCount(); i ++) {
			if (this.sensorListLeft.getChildAt(i) instanceof CheckBox) {
				if (((CheckBox)this.sensorListLeft.getChildAt(i)).isChecked()) {
					sensors.add(JsonParser.resolveSensorTypeById(this.sensorListLeft.getChildAt(i).getId()));
				}
			}
		} 
		
		for (int i = 0; i < this.sensorListRight.getChildCount(); i ++) {
			if (this.sensorListRight.getChildAt(i) instanceof CheckBox) {
				if (((CheckBox)this.sensorListRight.getChildAt(i)).isChecked()) {
					sensors.add(JsonParser.resolveSensorTypeById(this.sensorListRight.getChildAt(i).getId()));
				}
			}
		} 
		
		SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putStringSet(SHARED_SENSORS, sensors);
	    editor.commit();
	    
	    if (!this.addressTextField.getText().toString().contentEquals(this.address)) {
	    	editor.putString(SHARED_ADDRESS, this.addressTextField.getText().toString());
	    	editor.commit();
	    } if (Integer.parseInt(this.portTextField.getText().toString()) != this.port) {
	    	editor.putInt(SHARED_PORT, Integer.parseInt(this.portTextField.getText().toString()));
	    	editor.commit();
	    } if (Long.parseLong(this.intervalTextField.getText().toString()) != this.interval) {
	    	editor.putLong(SHARED_INTERVAL, Long.parseLong(this.intervalTextField.getText().toString()));
	    	editor.commit();
	    } if (!this.locationTextField.getText().toString().contentEquals(this.location)) {
	    	editor.putString(SHARED_LOCATION, this.locationTextField.getText().toString());
	    	editor.commit();
	    }if (this.gpsCheckBox.isEnabled()) {
	    	editor.putBoolean(SHARED_GPS, this.gpsCheckBox.isChecked());
	    	editor.commit();
	    }
	    
		if (_showToast) ((MainActivity)getActivity()).showToast("Settings saved!");
		
	}
	
	public void onBackPressed() {
		this.saveState(true);
	}
	
	private void toggleGPS(Boolean _turnOn) {
		
		Intent intent=new Intent("android.location.GPS_ENABLED_CHANGE");
		intent.putExtra("enabled", _turnOn);
		getActivity().sendBroadcast(intent);
	}

	@Override
	public void onClick(View _view) {
		switch (_view.getId()) {
		
			case R.id.settings_button_exit: getActivity().onBackPressed();
				break;
			case R.id.settings_button_reset: 
				this.sensorListLeft.removeAllViews();
			    this.sensorListRight.removeAllViews();
				this.loadSettings();
				break;
			case R.id.settings_button_save: this.saveState(true);
				break;
			case R.id.settings_gps_location: if (this.gpsCheckBox.isEnabled()) this.toggleGPS(this.gpsCheckBox.isChecked());
		}		
	}
}