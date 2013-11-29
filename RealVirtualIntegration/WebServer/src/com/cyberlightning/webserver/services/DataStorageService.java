package com.cyberlightning.webserver.services;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
 * @author Cyberlightning Ltd. (tomi.sarni@cyberlightning.com)
 *
 */
public class DataStorageService implements Runnable {
	
	private static final DataStorageService _serilizationService = new DataStorageService();
	public Map<String, DatagramPacket> eventBuffer= new ConcurrentHashMap<String, DatagramPacket>(); 
	public Map<String, InetSocketAddress> baseStationReferences= new ConcurrentHashMap<String, InetSocketAddress>(); 
	private EntityTable entityTable = new EntityTable();
	
	private Thread saveFileRoutine;
	private boolean suspendFlag = true;
	private volatile boolean saveInProcessFlag = false;
	
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
	         
	        saveFileRoutine= new Thread((Runnable)(new SaveFileRoutine()));
	        saveFileRoutine.start();
	         
	      } catch (FileNotFoundException i) {
	         EntityTable e = entityTable;
	         Map<String, InetSocketAddress> b = baseStationReferences;
	         saveData(e,StaticResources.DATABASE_FILE_NAME);
	         saveData(b, StaticResources.REFERENCE_TABLE_FILE_NAME);//remove these four lines at some point
	         return;
	      }  catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException c) {
			 EntityTable e = entityTable;
	         Map<String, InetSocketAddress> b = baseStationReferences;
	         saveData(e,StaticResources.DATABASE_FILE_NAME);
	         saveData(b, StaticResources.REFERENCE_TABLE_FILE_NAME);//remove these four lines at some point
			c.printStackTrace();
		} 
	}
	
	/**
	 * 
	 * @param _data
	 * @throws IOException 
	 */
	public void addEntry (DatagramPacket _data) throws IOException {
		
		String data;
		if(Gzip.isCompressed(_data.getData())) data = Gzip.decompress(_data.getData());
		else data = new String(_data.getData(),"utf8");
		
		ArrayList<Entity> entities = TranslationService.decodeSensorJson(data);
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
	public String getEntryById(String _uuid, int _maxResults) {
		
		String jsonString = null;
		ArrayList<Entity> entities = new ArrayList<Entity>();
		EntityTable persistentEntityTable = this.loadData();
		Entity e = persistentEntityTable.getEntity(_uuid);
		if (e != null) {
			entities.add(e);
			jsonString = TranslationService.encodeJson(entities, _maxResults);
		} else {
			jsonString = StaticResources.ERROR_CODE_NOT_FOUND;
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
			entities = this.getEntitiesBySpatialCircle(_query.points[0], _query.points[1],_query.radius); 
			break;
		case StaticResources.QUERY_TYPE:
			break;
		}
		
		if (entities.size() == 0 )return StaticResources.ERROR_CODE_NOT_FOUND;
		else return TranslationService.encodeJson(entities,_query.maxResults);
		
	}

	private EntityTable loadData() {
		
		EntityTable dbFile = null;
		try {
			FileInputStream data = new FileInputStream(StaticResources.DATABASE_FILE_NAME);
			ObjectInputStream dataIn = new ObjectInputStream(data);
			dbFile= (EntityTable)dataIn.readObject();
			dataIn.close();
			data.close();
			} catch (IOException | ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        
        return dbFile; 
	}
	
	/**
	 * 
	 * @param _lat
	 * @param _lon
	 * @param _radius
	 * @return Return a list of entities within a circle of _radius from point (_lat,_lon) 
	 */
	public ArrayList<Entity> getEntitiesBySpatialCircle(Float _lat, Float _lon, int _radius) {
		
		ArrayList<Entity> includedEntities = new ArrayList<Entity>();
		EntityTable persistentEntityTable = this.loadData();
		Iterator<RowEntry> rows = persistentEntityTable.entities.keySet().iterator();
		while (rows.hasNext()) {
			RowEntry row = rows.next();
			if (row.location != null) {
				double x = row.location[0] - _lat;
				double y = row.location[1] - _lon;
				if ((Math.sqrt(Math.pow(x, 2)+Math.pow(y, 2))/ 0.000008998719243599958) < _radius) {
					includedEntities.add(persistentEntityTable.entities.get(row));
				}
			}
		}
		return includedEntities;
	}
	
	/**
	 * 
	 * @param _uuids
	 * @return
	 */
	public ArrayList<InetSocketAddress> resolveBaseStationAddresses(String[] _uuids) {
		ArrayList<InetSocketAddress> addresses = new ArrayList<InetSocketAddress>();
		EntityTable persistentEntityTable = this.loadData();
		for(String uuid : _uuids) {
			
			Entity e = persistentEntityTable.getEntity(uuid);
			
			if (e != null) {
				if (this.baseStationReferences.containsKey(e.contextUUID) && !addresses.contains(this.baseStationReferences.get(e.contextUUID))) {
					addresses.add(this.baseStationReferences.get(e.contextUUID));
					
				} 
			}
		}
		System.out.println("returned addresses " + addresses);
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
	public void addToBuffer(String s, DatagramPacket d){
		this.eventBuffer.put(s, d);
		this.wakeThread();
	}
	public void suspendThread() {
	      suspendFlag = true;
	}

	private synchronized void wakeThread() {
	      suspendFlag = false;
	       notify();
	}
	
	@Override
	public void run() {
		this.intializeData();
	
		
		while(true) {
			
			synchronized(this) {
	            while(suspendFlag) {
	             try {
					wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
	            }
	        }
			
			if (eventBuffer.isEmpty()) continue;
			Iterator<String> i = this.eventBuffer.keySet().iterator();
			while (i.hasNext()) {
				String key = i.next();
				try {
					if (this.saveInProcessFlag) {
						break;
					} else {
						this.addEntry(this.eventBuffer.get(key));
						this.eventBuffer.remove(key);
					}
		
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			suspendThread();
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
				if (entityTable.entities.isEmpty()) continue;
				if (!suspendFlag) continue;
				saveInProcessFlag = true;
				EntityTable oldEntities = loadData();
				entityTable.appendOldEntities(oldEntities.entities);
				saveData(entityTable,StaticResources.DATABASE_FILE_NAME);
				entityTable.clearAll();
				saveData(baseStationReferences, StaticResources.REFERENCE_TABLE_FILE_NAME);
				saveInProcessFlag = false;
				if (!eventBuffer.isEmpty()) wakeThread();
			}
			
		}

	}
	
	
}
