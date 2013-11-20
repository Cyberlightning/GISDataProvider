package com.cyberlightning.webserver.sockets;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.UUID;

import com.cyberlightning.webserver.StaticResources;
import com.cyberlightning.webserver.entities.SpatialQuery;
import com.cyberlightning.webserver.interfaces.IMessageEvent;
import com.cyberlightning.webserver.services.DataStorageService;
import com.cyberlightning.webserver.services.MessageService;

public class HttpSocketWorker implements Runnable,IMessageEvent {

	
	private DataOutputStream output;
	private InputStream input;
	private MessageObject messageObject;
	private Socket clientSocket;
	
	private boolean suspendFlag = true;
	private boolean destroyFlag = false;
	
	public final String uuid = UUID.randomUUID().toString();
	public final int type = StaticResources.HTTP_CLIENT;
	
	/**
	 * 
	 * @param _socket
	 */
	public HttpSocketWorker(Socket _socket) {
		this.clientSocket = _socket;
		MessageService.getInstance().registerReceiver(this,this.uuid);
	}
	
	@Override
	public void run() {
		
		try {
			this.input = clientSocket.getInputStream();
			this.output = new DataOutputStream(clientSocket.getOutputStream());
			byte[] buffer = new byte[4096]; //TODO dynamic size?
			int len = input.read(buffer);
			
			if (len <= 0) {
				//TODO error check
			}
			
			String request = new String(buffer,"utf8");
			System.out.print(request);
			String[] result = request.split("\n");
			int fromIndex =  result[0].indexOf("?");
			int toIndex = result[0].indexOf("HTTP");
				
			/* Passes the urlencoded query string to appropriate http method handlers*/
			if (result[0].trim().toUpperCase().contains("GET")) {
				this.handleGETMethod(result[0].substring(fromIndex + 1, toIndex).trim());	
			} else if (result[0].trim().toUpperCase().contains("POST")) {
				fromIndex =  result[0].indexOf("/");
				String content = result[0].substring(fromIndex, toIndex);
				if (content.trim().contentEquals("/")) {
					this.handlePOSTMethod(result[result.length-1].toString(), false);
				} else {
					this.handlePOSTMethod(content, true);
				}
					
			} else if (result[0].trim().toUpperCase().contains("PUT")) {
				this.handlePUTMethod(result[result.length-1].toString());
			} else if (result[0].trim().toUpperCase().contains("DELETE")) {
				this.handleDELETEMethod(result[result.length-1].toString());
			} else System.out.println(result[0].trim().toUpperCase());
				
			synchronized(this) {
		        while(suspendFlag && !destroyFlag) {
					try {
						wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						break;
					}
		        }
		    }
			
			this.sendResponse(this.messageObject.payload.toString().trim());
			
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();	
			} 	
		
		return; //Exit thread
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
	
	/**
	 * 
	 * @param _content
	 */
	private void sendResponse(String _content) {
		
		String statusLine = "HTTP/1.1 200 OK" + "\r\n";
		String contentTypeLine = "Content-Type: text/plain; charset=utf-8" + "\r\n";
		String connectionLine = "Connection: close\r\n";
		String allowAllConnection = " Access-Control-Allow-Origin: * "+ "\r\n";
		String contentLengthLine = "Content-Length: " + _content.length() + "\r\n" + "\r\n";
		String contentLine = _content;
		
		try {	
			this.output.writeBytes(statusLine);;
			this.output.writeBytes(connectionLine);
			this.output.writeBytes(allowAllConnection);
			this.output.writeBytes(contentTypeLine);
			this.output.writeBytes(contentLengthLine);
			this.output.writeBytes(contentLine);
			
			System.out.print(statusLine);
			System.out.print(allowAllConnection);
			System.out.print(contentTypeLine);
			System.out.print(contentLengthLine);
			System.out.print(connectionLine);
			System.out.print("\r\n");
			System.out.print(contentLine);
			
			this.close();
		} catch (IOException e) {
			e.printStackTrace();
			this.close();
		}
		
	}
	private void close() {
		
		this.destroy();
		MessageService.getInstance().unregisterReceiver(this.uuid);
		try {
			this.output.close();
			this.input.close();
			this.clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param _request
	 */
	private void handlePUTMethod(String _request) {
		//TODO handPUTMethod
	}
	
	/**
	 * 
	 * @param _request
	 */
	private void handleDELETEMethod(String _request) {
		//TODO handDELETEMethod
	}
	
	/**
	 * 
	 * @param _content
	 */
	private void handleGETMethod(String _content) {
		
		String[] queries = _content.split("&");
		String[] maxResults;
		int max = 0;
		
		for (int i = 0; i < queries.length; i++) {
			if(queries[i].contains("action")) {
				String[] action = queries[i].split("=");
				if (action[1].contentEquals("loadById")) {
					String[] device = null;
					for (int j = 0; j < queries.length;j++) {
						if (queries[j].contains("device_id")) {
							device = queries[j].split("=");
							
						} if (queries[j].contains("maxResults")) {
							maxResults = queries[j].split("=");
							try {
								max = Integer.parseInt(maxResults[1].trim()); 
							} catch (NumberFormatException e) {
								max = 1;
							}
						}
					}
					if (device[1] == null || max < 0) {
						sendResponse(StaticResources.ERROR_CODE_BAD_REQUEST);
					} else {
						sendResponse(DataStorageService.getInstance().getEntryById(device[1], max));
					}
					
				} else if (action[1].contentEquals("loadBySpatial")) {
					String lat = "";
					String lon = "";
					int radius = 0;
					
					for (int j = 0; j < queries.length;j++) {
						
						if (queries[j].contains("lat")) {
							String[] la = queries[j].split("=");
							lat = la[1].trim();
						}
						if (queries[j].contains("lon")) {
							String[] lo = queries[j].split("=");
							lon = lo[1].trim();
						}
						if (queries[j].contains("radius")) {
							String[] rad = queries[j].split("=");
							try {
								radius = Integer.parseInt(rad[1].trim()); 
							} catch (NumberFormatException e) {
								radius = 0;
							}
						}
						if (queries[j].contains("maxResults")) {
							maxResults = queries[j].split("=");
							try {
								max = Integer.parseInt(maxResults[1].trim()); 
							} catch (NumberFormatException e) {
								max = 1;
							}
						}
					}
					if (lat == null || lon == null || radius < 1 || max < 0) {
						sendResponse(StaticResources.ERROR_CODE_BAD_REQUEST);
					} else {
						sendResponse(DataStorageService.getInstance().getEntriesByParameter(new SpatialQuery(Float.parseFloat(lat),Float.parseFloat(lon),radius,max)));
					}
					
				} 
			}
		}
	}

	/**
	 * 
	 * @param _content
	 * @param _isFile
	 */
	private void handlePOSTMethod(String _content, boolean _isFile) {
		
		String[] queries = _content.split("&");
		String[] targetUUIDs = null;
		
		for (int i = 0; i < queries.length; i++) {
			
			if(queries[i].contains("action")) {
				String[] action = queries[i].split("=");
				
				if (action[1].contentEquals("update")) {
				
					for (int j = 0; j < queries.length; j++) {
						
						if (queries[j].contains("device_id")) {
							String[] s = queries.clone()[j].trim().split("=");
							targetUUIDs = s[1].split(","); //check correct regex
						}
					}

				}else if (action[1].contentEquals("upload")) {
					
					//File file = new File("marker.bmp");
					//sendResponse(SimulateSensorResponse.uploadFile(file));
				} 
			}
			
		}
		if (targetUUIDs == null) {
			sendResponse(StaticResources.ERROR_CODE_BAD_REQUEST);
		} else {
			MessageService.getInstance().addToMessageBuffer(new MessageObject(this.uuid,StaticResources.HTTP_CLIENT,DataStorageService.getInstance().resolveBaseStationAddresses(targetUUIDs),_content));
			MessageService.getInstance().wakeThread();
		}
	}
	
	@Override
	public void onMessageReceived(MessageObject msg) {
		this.messageObject = msg;
		this.wakeThread();
	}
}

