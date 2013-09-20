package com.cyberlightning.android.coap.interfaces;

import java.util.Vector;

import com.cyberlightning.android.coap.message.CoapRequestCode;


/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */

public interface CoapRequest extends CoapMessage{

    public void setUriHost(String host);
    public void setUriPort(int port);
    public void setUriPath(String path);
    public void setUriQuery(String query);
    public void setProxyUri(String proxyUri);
    public void setToken(byte[] token);
    
    //public void addAccept(CoapMediaType mediaType);
    
    //public Vector<CoapMediaType> getAccept(CoapMediaType mediaType);
    public String getUriHost();
    public int getUriPort();
    public String getUriPath();
    public Vector<String> getUriQuery();
    public String getProxyUri();
    
    public void addETag(byte[] etag);
    public Vector<byte[]> getETag();

    public CoapRequestCode getRequestCode();
    public void setRequestCode(CoapRequestCode requestCode);
}
