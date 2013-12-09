package com.cyberlightning.realvirtualsensorsimulator;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Observable;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.cyberlightning.realvirtualsensorsimulator.interfaces.IClientSocket;
import com.cyberlightning.realvirtualsensorsimulator.interfaces.IMainActivity;
import com.cyberlightning.realvirtualsensorsimulator.staticresources.Gzip;
import com.cyberlightning.realvirtualsensorsimulator.views.SettingsViewFragment;

import android.content.SharedPreferences;
import android.os.Message;

public class ClientSocket extends Observable implements Runnable, IClientSocket {
	
	private ConcurrentLinkedQueue <Message> outboundBuffer = new ConcurrentLinkedQueue <Message>();
	private DatagramSocket serverSocket;
	private IMainActivity application;
	private InetAddress serverAddress;
	private SocketSender senderWorker;
	private Thread thread;
	private int port;
	private int serverPort;

	
	public static final int DEFAULT_BUFFER_SIZE = 1024;
	public static final int DEFAULT_INBOUND_SOCKET = 55555;
	public static final int MESSAGE_TYPE_INBOUND = 1;
	public static final int MESSAGE_TYPE_OUTBOUND = 2;
	public static final int MESSAGE_TYPE_UNKNOWNHOST_ERROR= 3;
	
	public static final String SERVER_DEFAULT_ADDRESS = "dev.cyberlightning.com";
	public static final int SERVER_DEFAULT_PORT = 61616;

	
	public  ClientSocket(MainActivity _activity) {
		this(DEFAULT_INBOUND_SOCKET, _activity);
	}
	
	public  ClientSocket(int _port, MainActivity _activity) {
		this.port = _port;
		this.thread = new Thread(this);
		this.thread.start();
		this.application = _activity;
	}
	
	@Override
	public void run() {
		
		
		try {
			
			this.serverSocket = new DatagramSocket(this.port);
			//this.serverSocket.setReceiveBufferSize(DEFAULT_BUFFER_SIZE);
			
			byte[] receiveByte = new byte[DEFAULT_BUFFER_SIZE]; 
			DatagramPacket receivedPacket = new DatagramPacket(receiveByte, receiveByte.length);
			
			this.senderWorker = new SocketSender();
			
			
			while(true) {
				serverSocket.receive(receivedPacket);
				handleInboundMessage(receivedPacket);
			}

			
		} catch(IOException e) {
			e.printStackTrace();
		} 	
		return; //Exits thread

	}

    private void handleInboundMessage(DatagramPacket _packet) {
    	
    	String request = null;
		try {
			request = new String (_packet.getData(), "utf8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		String[] result = request.split("\n");
		int fromIndex =  result[0].indexOf("?");
		int toIndex = result[0].indexOf("HTTP");
		
		/* Passes the urlencoded query string to appropriate http method handlers*/
		String method = result[0].trim().toUpperCase(Locale.ROOT);
		if (method.contains("POST")) {
			
			fromIndex =  result[0].indexOf("/");
			String content = result[0].substring(fromIndex, toIndex);
			if (content.trim().contentEquals("/")) {
				this.handlePOSTMethod(result[result.length-1].toString(), false);
			} else {
				this.handlePOSTMethod(content, true);
			}
			
		}
		else if (method.contains("PUT")) {
			//this.handlePUTMethod(result[result.length-1].toString());
		}
		else if (method.contains("DELETE")) {
			//this.handleDELETEMethod(result[result.length-1].toString());
		} else {
			this.handlePOSTMethod(request, false);
		}
		
		
		
	}
    
    private void propagateMessage(String _msg, Boolean _sendToUi) {
    	
    	Message msg = Message.obtain(null, MainActivity.MESSAGE_FROM_SERVER, _msg);
    	setChanged();
    	notifyObservers(msg);
    	
    	if (_sendToUi) {
    		msg.setTarget(this.application.getTarget());
        	msg.sendToTarget();
    	}
	}
    
	/**
	 * 
	 * @param _content
	 * @param _isFile
	 */
	private void handlePOSTMethod(String _content, boolean _isFile) {
		
		String[] queries = _content.split("&");
		
	
		for (String attr : queries) {
			if(attr.contains("action")) {
				String[] action = attr.split("=");
				if (action[1].contentEquals("update")) {
					for (int j = 0; j < queries.length; j++) {
						if (queries[j].contains("device_id")) {
							String[] device= queries.clone()[j].trim().split("=");
							if (device[1].trim().contentEquals(MainActivity.deviceId)) {
								for (int k = 0; k < queries.length; k++) {
									if (queries[k].contains("sensor_id")){
										String[] sensor = queries.clone()[k].trim().split("=");
										if (sensor[1].trim().contentEquals(MainActivity.displayActuator)) {
											for (int l = 0; l < queries.length; l++) {
												if (queries[l].contains("parameter")) {
													String[] params = queries.clone()[l].trim().split("=");
													String param = params[1].trim();
													if (param.contentEquals("viewstate")) {
														for (int m = 0; m < queries.length;m++) {
															if (queries[m].contains("value")) {
																String[] values = queries.clone()[m].trim().split("=");
																String value = values[1].trim();
																Message msg = Message.obtain(null, MainActivity.MESSAGE_TYPE_SET_MARKER, value);
														    	msg.setTarget(this.application.getTarget());
														        msg.sendToTarget();
														        break;
															}
														}
														break;
													}
													
												}
												
										    	
											}
											break;
										} 
									}
									
								}
								break;
							} else {
								break;
							}	
						}
						
					}
					break;
				}else if (action[1].contentEquals("upload")) {
					//TODO
				} 
				
			} else {
				this.propagateMessage(_content, true); 
			}
			break;
		}
	}

	public class SocketSender implements Runnable {
		private boolean suspendFlag = true;
		private boolean destroyFlag = false;
		private Thread thread;
		
		public SocketSender() {
			this.thread = new Thread(this);
			this.thread.start();
		}
		
		@Override
		public void run() {
			
			if(!this.resolveServerAddress()) return;
			
			while(true) {
				
				synchronized(this) {
		            while(suspendFlag && !destroyFlag) {
						try {
							wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							return;
						}
		            }
		            if (destroyFlag) break; 
		        }
				
				if(outboundBuffer.isEmpty()) continue;
				
				Message msg = outboundBuffer.poll();
				String payload = (String)msg.obj;
				byte[] payloadBuffer = null;
				try {
					payloadBuffer = Gzip.compress(payload);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				DatagramPacket packet = new DatagramPacket(payloadBuffer, payloadBuffer.length,serverAddress,serverPort);
				try {
					serverSocket.send(packet);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				this.suspendThread();
			}
			
			return;
			
		}
		
		private boolean resolveServerAddress() {
			SharedPreferences settings = application.getContext().getSharedPreferences(SettingsViewFragment.PREFS_NAME, 0);
			String address = "";
			try {
				address = settings.getString(SettingsViewFragment.SHARED_ADDRESS, SERVER_DEFAULT_ADDRESS);
				serverAddress = InetAddress.getByName(address);
				serverPort = settings.getInt(SettingsViewFragment.SHARED_PORT, SERVER_DEFAULT_PORT);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Message msg = Message.obtain(null, MESSAGE_TYPE_UNKNOWNHOST_ERROR, address);
				msg.setTarget(application.getTarget());
	        	msg.sendToTarget();
				
				this.destroy();
				return false;
			}
	    	return true;
		}
		
		public void suspendThread() {
		      suspendFlag = true;
		}

		private synchronized void wakeThread() {
		      suspendFlag = false;
		      notify();
		}
		
		private synchronized void destroy() {
		      this.destroyFlag = true;
		      notify();
		}
	}
	
	
	
	@Override
	public void sendMessage(Message _msg) {
		this.outboundBuffer.offer(_msg);
		this.senderWorker.wakeThread();
	}

	@Override
	public void pause() {
		this.senderWorker.suspendThread();
		
	}

	@Override
	public void resume() {
		this.senderWorker.wakeThread();
		
	}

	@Override
	public void end() {
		this.senderWorker.destroy();
	}
	
}
