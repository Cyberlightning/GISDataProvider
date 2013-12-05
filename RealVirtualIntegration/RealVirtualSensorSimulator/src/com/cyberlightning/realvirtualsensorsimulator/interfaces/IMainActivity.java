package com.cyberlightning.realvirtualsensorsimulator.interfaces;

import android.content.Context;
import android.os.Handler;

public interface IMainActivity {
	
	public Context getContext();

	public Handler getTarget();
	
	public void showNoGpsAlert();
}
