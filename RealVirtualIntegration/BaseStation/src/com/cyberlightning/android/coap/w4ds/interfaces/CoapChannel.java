
package com.cyberlightning.android.coap.w4ds.interfaces;
/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */

import java.net.InetAddress;

import com.cyberlightning.android.coap.w4ds.messages.CoapBlockOption.CoapBlockSize;

public interface CoapChannel {

	public void sendMessage(CoapMessage msg);

	/*TODO: close when finished, & abort()*/
    public void close();
    
    public InetAddress getRemoteAddress();

    public int getRemotePort();
    
    /* handles an incomming message */
    public void handleMessage(CoapMessage message);
    
    /*TODO: implement Error Type*/
	public void lostConnection(boolean notReachable, boolean resetByServer);
	
	
	public CoapBlockSize getMaxReceiveBlocksize();

	public void setMaxReceiveBlocksize(CoapBlockSize maxReceiveBlocksize);

	public CoapBlockSize getMaxSendBlocksize();

	public void setMaxSendBlocksize(CoapBlockSize maxSendBlocksize);
}
