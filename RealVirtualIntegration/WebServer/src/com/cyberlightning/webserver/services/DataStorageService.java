package com.cyberlightning.webserver.services;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
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
			EntityTable e = entityTable;
			Map<String, InetSocketAddress> b = baseStationReferences;
			saveData(e,StaticResources.DATABASE_FILE_NAME);
			saveData(b, StaticResources.REFERENCE_TABLE_FILE_NAME);
		
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
	         
	         Thread t = new Thread((Runnable)(new SaveFileRoutine()));
	         t.start();
	         
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
			if (entity.location != null) entry.location = entity.location; 
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
		ArrayList<Entity> entities = new ArrayList<Entity>();
		Entity e = entityTable.getEntity(_uuid);
		if (e != null) {
			entities.add(e);
			jsonString = JsonTranslator.encodeJson(entities, 40);
		} else {
			jsonString = StaticResources.ERROR_404_MESSAGE;
		}

		return jsonString;
	}
	
	/**
	 * 
	 * @param _query
	 * @return
	 */
	public String getEntriesByParameter(SpatialQuery _query) {
		
		
		ArrayList<Entity> entities = null;
		
		switch (_query.queryType) {
		case StaticResources.QUERY_SPATIA_BOUNDING_BOX:
			break;
		case StaticResources.QUERY_SPATIA_SHAPE:
			break;
		case StaticResources.QUERY_SPATIAL_CIRCLE:
			entities = this.entityTable.getEntitiesBySpatialCircle(_query.points[0], _query.points[1],_query.radius); 
			break;
		case StaticResources.QUERY_TYPE:
			break;
		}
		
		return JsonTranslator.encodeJson(entities, 40);
		
	}
	/**
	 * 
	 * @param _uuids
	 * @return
	 */
	public ArrayList<InetSocketAddress> resolveBaseStationAddresses(String[] _uuids) {
		ArrayList<InetSocketAddress> addresses = new ArrayList<InetSocketAddress>();
		
		for(String uuid : _uuids) {
			
			Entity e = entityTable.getEntity(uuid);
			if (e != null) {
				if (this.baseStationReferences.containsKey(e.contextUUID) && !addresses.contains(this.baseStationReferences.get(e.contextUUID))) {
					addresses.add(this.baseStationReferences.get(e.contextUUID));
				} 
			}
		}
		return addresses;
	}
	
	public ArrayList<String> resolveBaseStationUuids(String[] _uuids) {
		ArrayList<String> baseUuids = new ArrayList<String>();
		
		for(String uuid : _uuids) {
			
			Entity e = entityTable.getEntity(uuid);
			if (e != null) {
				if (e.contextUUID != null && !baseUuids.contains(e.contextUUID)){
					baseUuids.add(e.contextUUID);
				}
			}
		}		
		return baseUuids;
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
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
	}
	
	private class SaveFileRoutine implements Runnable {

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(StaticResources.SAVE_TO_HD_INTERVAL);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				EntityTable e = entityTable;
				Map<String, InetSocketAddress> b = baseStationReferences;
				saveData(e,StaticResources.DATABASE_FILE_NAME);
				saveData(b, StaticResources.REFERENCE_TABLE_FILE_NAME);
			}
			
		}
		
	}
	
	
}
