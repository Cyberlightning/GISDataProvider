package com.cyberlightning.webserver.services;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.cyberlightning.webserver.StaticResources;
import com.cyberlightning.webserver.entities.Client;
import com.cyberlightning.webserver.entities.DeviceTable;


public class DataStorageService implements Runnable {
	
	private static final DataStorageService _serilizationService = new DataStorageService();
	public  Map<String, DatagramPacket> eventBuffer= new ConcurrentHashMap<String, DatagramPacket>(); 
	private DeviceTable db;
	
	private DataStorageService() {
		
	}
	
	public static DataStorageService getInstance () {
		return _serilizationService;
	}
	
	public void intializeData() {
		
		try {
			
	    	 FileInputStream fileIn = new FileInputStream(StaticResources.DATABASE_FILE_PATH);
	         ObjectInputStream in = new ObjectInputStream(fileIn);
	         this.db = (DeviceTable) in.readObject();
	         in.close();
	         fileIn.close();
	         
	      } catch (IOException i) {
	         i.printStackTrace();
	         return;
	      } catch(ClassNotFoundException c) {
	         System.out.println("Database not found! ");
	         c.printStackTrace();
	         return;
	      } 
	}
	
	public void addEntry (String _uuid, DatagramPacket _data) {
		this.eventBuffer.putIfAbsent(_uuid, _data);
	}
	
	public Client getAddressByUuid(String _uuid) {
		return null;
		
	}
	
	public void saveData () {
		 
		try {
	         FileOutputStream fileOut =  new FileOutputStream(StaticResources.DATABASE_FILE_PATH);
	         ObjectOutputStream out = new ObjectOutputStream(fileOut);
	         out.writeObject(this.db);
	         out.close();
	         fileOut.close();
	         System.out.println("Serialized data is saved in " + StaticResources.DATABASE_FILE_PATH);
	      } catch(IOException i) {
	          i.printStackTrace();
	      }
	}
	
	
	@Override
	public void run() {
		this.intializeData();
		
		while(true) {
			if (eventBuffer.isEmpty()) continue;
			
			
		}
	}
	
	
	
}
