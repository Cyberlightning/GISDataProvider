package com.cyberlightning.webserver.services;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.cyberlightning.webserver.StaticResources;
import com.cyberlightning.webserver.entities.Entity;
import com.cyberlightning.webserver.entities.EntityTable;
import com.cyberlightning.webserver.entities.RowEntry;
import com.cyberlightning.webserver.entities.SpatialQuery;


public class DataStorageService implements Runnable {
	
	private static final DataStorageService _serilizationService = new DataStorageService();
	public  Map<String, DatagramPacket> eventBuffer= new ConcurrentHashMap<String, DatagramPacket>(); 
	private EntityTable entityTable = new EntityTable();
	
	private DataStorageService() {
		
	}
	
	public static DataStorageService getInstance () {
		return _serilizationService;
	}
	
	public void intializeData() {
		
		try {
	    	 FileInputStream fileIn = new FileInputStream(StaticResources.DATABASE_FILE_PATH);
	         ObjectInputStream in = new ObjectInputStream(fileIn);
	         this.entityTable = (EntityTable) in.readObject();
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
	
	public void addEntry (DatagramPacket _data) throws UnsupportedEncodingException {
		ArrayList<Entity> entities = JsonTranslator.decodeSensorJson(new String(_data.getData(),"utf8"));
		for (Entity entity : entities) {
			RowEntry entry = new RowEntry(StaticResources.getTimeStamp());
			if (entity.uuid != null) entry.entityUUID = entity.uuid;
			if (entity.attributes.containsKey("address")) entry.address = (String) entity.attributes.get("address");
			entry.contextUUID = entity.contextUUID;
			this.entityTable.addEntity(entry,entity);
		}
	}
	
	public String getEntryById(String _uuid) {
		
		String jsonString = null;
		
		if (entityTable.hasEntity(_uuid)) {
			ArrayList<Entity> entities = new ArrayList<Entity>();
			entities.add(entityTable.getEntity(_uuid));
			jsonString = JsonTranslator.encodeJson(entities, 40);
		}
		
		return jsonString;
	}
	
	public String getEntriesByParameter(SpatialQuery _query) {
		
		
		return null;
		
	}
	
	public void saveData () {
		 
		try {
	         FileOutputStream fileOut =  new FileOutputStream(StaticResources.DATABASE_FILE_PATH);
	         ObjectOutputStream out = new ObjectOutputStream(fileOut);
	         out.writeObject(this.entityTable);
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
		String s = "{\"550e8400-e29b-41d4-a716-446655440111\":{\"550e8400-e29b-41d4-a716-446655440000\":{\"attributes\":{\"name\":\"Power wall outlet\",\"address\":null},\"actuators\":[{\"uuid\":null,\"attributes\":{\"type\":\"power_switch\"},\"parameters\":{\"callback\":false},\"variables\": [{\"relay\":false, \"type\": \"boolean\" }]}],\"sensors\":[{\"uuid\":null,\"attributes\":{\"type\":\"Power sensor\"},\"parameters\":{\"options\":null},\"values\": [{\"value\": 13,\"time\":\"YY-MM-DD HH:MM\",\"unit\" : \"Celcius\"}]}]}}}";
		byte[] b = s.getBytes();
		DatagramPacket d = new DatagramPacket(b, b.length);
		this.eventBuffer.put("test", d);
		
		while(true) {
			if (eventBuffer.isEmpty()) continue;
			Iterator<String> i = this.eventBuffer.keySet().iterator();
			while (i.hasNext()) {
				String key = i.next();
				try {
					this.addEntry(this.eventBuffer.get(key));
					this.eventBuffer.remove(key);
					this.saveData();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
	}
	
	
	
}
