package com.cyberlightning.android.coap.service;

import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Messenger;
import android.widget.ArrayAdapter;

public class BluetoothListener implements Runnable {
	
	private Context context;
	private Messenger messenger;
	private BluetoothAdapter bluetoothAdapter;
	private ArrayAdapter arrayAdapter;

	public BluetoothListener(Context _context, Messenger _messenger) {
		this.context = _context;
		messenger = _messenger;
		//this.initialize();
	}
	
	private void initialize() {
	
		if (this.bluetoothAdapter.isEnabled()) {
			this.discoverAlreadyPairedDevices();
		} else {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			this.context.getApplicationContext().startActivity(enableBtIntent);
		}
		
	}
	private void discoverAlreadyPairedDevices() {
	Set<BluetoothDevice> pairedDevices = this.bluetoothAdapter.getBondedDevices();
	// If there are paired devices
	if (pairedDevices.size() > 0) {
	    // Loop through paired devices
	    	for (BluetoothDevice device : pairedDevices) {
	        // Add the name and address to an array adapter to show in a ListView
	    		arrayAdapter.add(device.getName() + "\n" + device.getAddress());
	    	}
		}
	}
	
	@Override
	public void run() {
		
		
	}

}
