package com.cyberlightning.realvirtualsensorsimulator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.example.realvirtualsensorsimulator.R;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

public class SettingsFragment extends Fragment  {
	
	private LinearLayout sensorList;
	private LinearLayout settingsFragmentView;
	private Set<String> defaultValues;
	public static final String PREFS_NAME = "RealVirtualInteraction";
	public static final String SHARED_SENSORS = "Sensors";

    
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.settingsFragmentView = (LinearLayout) inflater.inflate(R.layout.settings , null);
		this.sensorList = (LinearLayout) this.settingsFragmentView.findViewById(R.id.settings_sensorlist);
		setHasOptionsMenu(true);
        this.loadSensorList();
        return this.settingsFragmentView;
    }
	
	private void loadSensorList() {
	
		SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
		List<Sensor> availableSensors = ((SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE)).getSensorList(Sensor.TYPE_ALL);
		this.defaultValues =  new HashSet<String>(availableSensors.size());
		
		for (Sensor sensor: availableSensors) {
			this.defaultValues.add(Integer.toString(sensor.getType()));
		}
		
	    Set<String> sensors = settings.getStringSet(SHARED_SENSORS, this.defaultValues);
	    for (String sensor : this.defaultValues) {
	    	int id = Integer.parseInt(sensor);
	    	CheckBox cb = new CheckBox(getActivity());
	    	cb.setText(JsonParser.resolveSensorTypeById(id));
	        cb.setId(id);
	        if (sensors.contains(JsonParser.resolveSensorTypeById(id)))cb.setChecked(true);
	        this.sensorList.addView(cb);
	    }
	}


	@Override
	public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {
		
		((MenuItem)menu.findItem(R.id.menu_main)).setVisible(true);
		((MenuItem)menu.findItem(R.id.menu_settings)).setVisible(false);
		((MenuItem)menu.findItem(R.id.menu_settings)).setEnabled(false);
		getActivity().invalidateOptionsMenu();
    	super.onCreateOptionsMenu(menu, inflater);
	}
	
	private void saveState() {
		
		Set<String> sensors = new HashSet<String>(this.defaultValues.size());
		
		for (int i = 0; i < this.sensorList.getChildCount(); i ++) {
			if (this.sensorList.getChildAt(i) instanceof CheckBox) {
				if (((CheckBox)this.sensorList.getChildAt(i)).isChecked()) {
					sensors.add(JsonParser.resolveSensorTypeById(this.sensorList.getChildAt(i).getId()));
				}
			}
		} 
		
		SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putStringSet(SHARED_SENSORS, sensors);
	    editor.commit();

		if (getActivity() instanceof MainActivity) ((MainActivity)getActivity()).showToast("Settings saved!");
		
	}
	
	public void onBackPressed() {
		this.saveState();
	}

}