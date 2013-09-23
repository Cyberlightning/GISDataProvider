
package com.cyberlightning.android.coap.w4ds.interfaces;

import com.cyberlightning.android.coap.w4ds.messages.CoapRequestCode;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */

public interface CoapClientChannel extends CoapChannel {
    public CoapRequest createRequest(boolean reliable, CoapRequestCode requestCode);
    public void setTrigger(Object o);
    public Object getTrigger();
}
