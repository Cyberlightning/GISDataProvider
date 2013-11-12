package com.cyberlightning.realvirtualsensorsimulator;

public interface ISensorListener {
	public void pause();
	public void resume();
	public void stopListeningToSensor (int _sensorType);
	public void changeBroadCastInterval(int _duration);
	public void changeSensorPriority(String _sensorId);
	public void toggleGps(boolean _isHighPriority, boolean _hasGps);

}
