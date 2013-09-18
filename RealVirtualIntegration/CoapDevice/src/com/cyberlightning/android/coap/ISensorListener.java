package com.cyberlightning.android.coap;

public interface ISensorListener {
	public void pause();
	public void resume();
	public void stopListeningToSensor (int _sensorType);

}
