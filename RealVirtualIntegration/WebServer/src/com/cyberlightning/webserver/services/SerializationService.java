package com.cyberlightning.webserver.services;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


import com.cyberlightning.webserver.StaticResources;
import com.cyberlightning.webserver.entities.DeviceTable;
import com.cyberlightning.webserver.entities.Entity;


public class SerializationService {
	
	private static final SerializationService _serilizationService = new SerializationService();
	private DeviceTable db;
	
	private SerializationService() {
		
	}
	
	public static SerializationService getInstance () {
		return _serilizationService;
	}
	
	public void intializeDataBase() {
		
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
	
	
	public void saveDataBase () {
		 
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
	
	public void addNewDevice(Entity _entity) {
		this.db.addEntity(_entity);
		this.saveDataBase();
	}
	
	public void removeExistingDevice(Entity _entity) {
		this.db.removeEntity(_entity);
		this.saveDataBase();
	}
	
	
	
}
