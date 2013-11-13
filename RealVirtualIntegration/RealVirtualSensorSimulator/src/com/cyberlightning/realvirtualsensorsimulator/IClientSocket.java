package com.cyberlightning.realvirtualsensorsimulator;

import android.os.Message;

public interface IClientSocket {
	public void pause();
	public void resume();
	public void end();
	public void sendMessage(Message _msg);
}
