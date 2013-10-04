package com.cyberlightning.android.coap.application;

public class MessageEvent {
	
	private String senderAddress;
	private String targetAddress;
	private String content;
	private boolean isNewSender;
	
	
	public MessageEvent (String _address, boolean _isnew) {
		
		this.isNewSender =  _isnew;
		this.setSenderAddress(_address);
	}
	
	/** Constructor for MessageEvent
	 * @param _content String content of the message event
	 * @param _receivedAddress String representation of IP address of the sender of this message
	 * @param _isnew boolean type to determine whether the sender address is a new previously unknown address
	 * @param _targetAddress String representation of IP address for the intended target of this message */
	public MessageEvent (String _content, String _receiveAddress, boolean _isnew, String _targetAddress) {
		this.content = _content;
		this.isNewSender =  _isnew;
		this.senderAddress = _receiveAddress;
		this.targetAddress = _targetAddress;
		
	}
	
	

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean isNewSender() {
		return isNewSender;
	}

	public void setNewSender(boolean isNewSender) {
		this.isNewSender = isNewSender;
	}

	public String getSenderAddress() {
		return senderAddress;
	}

	public void setSenderAddress(String senderAddress) {
		this.senderAddress = senderAddress;
	}

	public String getTargetAddress() {
		return targetAddress;
	}

	public void setTargetAddress(String targetAddress) {
		this.targetAddress = targetAddress;
	}

}
