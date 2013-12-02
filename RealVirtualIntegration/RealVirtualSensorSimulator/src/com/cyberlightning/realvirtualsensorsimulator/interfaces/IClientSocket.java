package com.cyberlightning.realvirtualsensorsimulator.interfaces;

import android.os.Message;

public interface IClientSocket {
	public void pause();
	public void resume();
	public void end();
	public void sendMessage(Message _msg);
}
