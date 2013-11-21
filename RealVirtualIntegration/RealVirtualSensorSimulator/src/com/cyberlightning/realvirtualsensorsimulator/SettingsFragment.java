package com.cyberlightning.realvirtualsensorsimulator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.example.realvirtualsensorsimulator.R;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

public class SettingsFragment extends Fragment {
	
	public LinearLayout sensorList;
	public static final String PREFS_NAME = "RealVirtualInteraction";
	public static final String SHARED_SENSORS = "Sensors";

    
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
        this.populateSensorList();
        return inflater.inflate(R.layout.settings, container, false);
    }
	
	private void populateSensorList() {
		View view = (View)getActivity().findViewById(R.id.settings_sensorlist);
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);              

		LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.settings , null);
		this.sensorList = (LinearLayout) layout.findViewById(R.id.settings_sensorlist);
		
		
		SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
		List<Sensor> availableSensors = ((SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE)).getSensorList(Sensor.TYPE_ALL);
		Set<String> defVals =  new HashSet<String>(availableSensors.size());
		for (Sensor sensor: availableSensors) {
			defVals.add(Integer.toString(sensor.getType()));
		}
		
	    Set<String> sensors = settings.getStringSet(SHARED_SENSORS, defVals);
	    for (String sensor : sensors) {
	    	int id = Integer.parseInt(sensor);
	    	CheckBox cb = new CheckBox(getActivity());
	    	cb.setText(JsonParser.resolveSensorTypeById(id));
	        cb.setId(id);
	        this.sensorList.addView(cb);
	    }
	
	}
	
}