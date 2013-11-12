package com.cyberlightning.realvirtualsensorsimulator;

public interface ISensorListener {
	public void pause();
	public void resume();
	public void toggleSensor (int _sensorType);
	public void changeBroadCastInterval(int _duration);
}
