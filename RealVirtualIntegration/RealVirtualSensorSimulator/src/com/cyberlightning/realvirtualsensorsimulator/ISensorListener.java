package com.cyberlightning.realvirtualsensorsimulator;

public interface ISensorListener {
	public void pause();
	public Integer resume();
	public void end();
	public void toggleSensor (int _sensorType);
	public void changeEventInterval(int _duration);
}
