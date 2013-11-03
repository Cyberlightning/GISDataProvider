package com.cyberlightning.webserver.services;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.cyberlightning.webserver.StaticResources;
import com.cyberlightning.webserver.entities.Entity;
import com.cyberlightning.webserver.entities.EntityTable;
import com.cyberlightning.webserver.entities.RowEntry;
import com.cyberlightning.webserver.entities.SpatialQuery;

/**
 * 
 * @author Tomi
 *
 */
public class DataStorageService implements Runnable {
	
	private static final DataStorageService _serilizationService = new DataStorageService();
	public Map<String, DatagramPacket> eventBuffer= new ConcurrentHashMap<String, DatagramPacket>(); 
	public Map<String, InetSocketAddress> baseStationReferences= new ConcurrentHashMap<String, InetSocketAddress>(); 
	private EntityTable entityTable = new EntityTable();
	
	private DataStorageService() {
		
	}
	
	public static DataStorageService getInstance () {
		return _serilizationService;
	}
	
	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void intializeData() {
		
		try {
	    	 FileInputStream data = new FileInputStream(StaticResources.DATABASE_FILE_NAME);
	         ObjectInputStream dataIn = new ObjectInputStream(data);
	         this.entityTable = (EntityTable)dataIn.readObject();
	         dataIn.close();
	         data.close();
	        
	         FileInputStream ref = new FileInputStream(StaticResources.REFERENCE_TABLE_FILE_NAME);
	         ObjectInputStream refIn = new ObjectInputStream(ref);
	         this.baseStationReferences = (Map<String, InetSocketAddress>) refIn.readObject();
	         refIn.close();
	         ref.close();
	         
	      } catch (IOException i) {
	         i.printStackTrace();
	         return;
	      } catch(ClassNotFoundException c) {
	         System.out.println("Database not found! ");
	         c.printStackTrace();
	         return;
	      } 
	}
	
	/**
	 * 
	 * @param _data
	 * @throws UnsupportedEncodingException
	 */
	public void addEntry (DatagramPacket _data) throws UnsupportedEncodingException {
		ArrayList<Entity> entities = JsonTranslator.decodeSensorJson(new String(_data.getData(),"utf8"));
		String contextUUID = null;
		for (Entity entity : entities) {
			RowEntry entry = new RowEntry(StaticResources.getTimeStamp());
			if (entity.uuid != null) entry.entityUUID = entity.uuid;
			if (entity.attributes.containsKey("address")) entry.address = (String) entity.attributes.get("address");
			entry.contextUUID = entity.contextUUID;
			this.entityTable.addEntity(entry,entity);
			
			if (contextUUID !=null) continue;
			contextUUID = entity.contextUUID;
			this.baseStationReferences.put(contextUUID, (InetSocketAddress)_data.getSocketAddress());
		}
		
	}
	/**
	 * 
	 * @param _uuid
	 * @return
	 */
	public String getEntryById(String _uuid) {
		
		String jsonString = null;
		
		if (entityTable.hasEntity(_uuid)) {
			ArrayList<Entity> entities = new ArrayList<Entity>();
			entities.add(entityTable.getEntity(_uuid));
			jsonString = JsonTranslator.encodeJson(entities, 40);
		}
		
		return jsonString;
	}
	
	/**
	 * 
	 * @param _query
	 * @return
	 */
	public String getEntriesByParameter(SpatialQuery _query) {
		
		
		return null;
		
	}
	/**
	 * 
	 * @param _uuids
	 * @return
	 */
	public ArrayList<InetSocketAddress> resolveBaseStationAddresses(String[] _uuids) {
		ArrayList<InetSocketAddress> addresses = new ArrayList<InetSocketAddress>();
		
		for(String uuid : _uuids) {
			if (entityTable.hasEntity(uuid)) {
				Entity e = entityTable.getEntity(uuid);
				if (this.baseStationReferences.containsKey(e.contextUUID) && !addresses.contains(this.baseStationReferences.get(e.contextUUID))) {
					addresses.add(this.baseStationReferences.get(e.contextUUID));
				} 
			}
		}
		return addresses;
	}
	

	private void saveData (Object _object, String _fileName) {
		 
		try {
	         FileOutputStream fileOut =  new FileOutputStream(_fileName);
	         ObjectOutputStream out = new ObjectOutputStream(fileOut);
	         out.writeObject(_object);
	         out.close();
	         fileOut.close();
	         System.out.println("Serialized data is saved in " + _fileName);
	      } catch(IOException i) {
	          i.printStackTrace();
	      }
	}
	
	private void testMethod() {
		String s = "{\"550e8400-e29b-41d4-a716-446655440111\":{\"550e8400-e29b-41d4-a716-446655440000\":{\"attributes\":{\"name\":\"Power wall outlet\",\"address\":null},\"actuators\":[{\"uuid\":null,\"attributes\":{\"type\":\"power_switch\"},\"parameters\":{\"callback\":false},\"variables\": [{\"relay\":false, \"type\": \"boolean\" }]}],\"sensors\":[{\"uuid\":null,\"attributes\":{\"type\":\"Power sensor\"},\"parameters\":{\"options\":null},\"values\": [{\"value\": 13,\"time\":\"YY-MM-DD HH:MM\",\"unit\" : \"Celcius\"}]}]}}}";
		byte[] b = s.getBytes();
		DatagramPacket d = new DatagramPacket(b, b.length);
		this.eventBuffer.put("test", d);
	}
	

	
	@Override
	public void run() {
		this.intializeData();
	
		
		while(true) {
			if (eventBuffer.isEmpty()) continue;
			Iterator<String> i = this.eventBuffer.keySet().iterator();
			while (i.hasNext()) {
				String key = i.next();
				try {
					this.addEntry(this.eventBuffer.get(key));
					this.eventBuffer.remove(key);
					this.saveData(this.entityTable,StaticResources.DATABASE_FILE_NAME);
					this.saveData(this.baseStationReferences, StaticResources.REFERENCE_TABLE_FILE_NAME);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
	}
	
	
	
}
