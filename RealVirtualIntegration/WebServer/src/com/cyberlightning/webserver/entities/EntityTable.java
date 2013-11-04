package com.cyberlightning.webserver.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class EntityTable implements java.io.Serializable {
	
	
	private static final long serialVersionUID = -8536303237731902808L;
	private Map<RowEntry, Entity> entities = new ConcurrentHashMap<RowEntry, Entity>(); 
	
	/**
	 * 
	 * @param _uuid
	 * @return
	 */
	public boolean hasEntity(String _uuid) {
		
		if (this.entities.containsKey(_uuid)) {
			return true;	
		} else {
			return false;
		}
	}
	/**
	 * 
	 * @param _entry
	 * @param _entity
	 */
	public void addEntity(RowEntry _entry, Entity _entity) {
		
		if (!this.entities.isEmpty()) {
			Iterator<RowEntry> rows = this.entities.keySet().iterator();
			while (rows.hasNext()) {
				RowEntry row = rows.next();
				if (_entry.entityUUID.contentEquals(row.entityUUID)) {
					_entity = updateValues(this.entities.get(row), _entity);
					this.entities.remove(row);
					this.entities.put(_entry, _entity);
				} else {
					this.entities.put(_entry, _entity);
				}
			}
		} else {
			this.entities.put(_entry, _entity);
		}
	}
	/**
	 * 
	 * @param _old
	 * @param _new
	 * @return
	 */
	private Entity updateValues(Entity _old, Entity _new) {
		for (Sensor sensor: _new.sensors) {
			if(sensor.values != null) {
				ArrayList<HashMap<String,Object>> values = sensor.values;
				for(Sensor oldSensor : _old.sensors) {
					if (oldSensor.uuid != null || sensor.uuid != null) {
						if (oldSensor.uuid.contentEquals(sensor.uuid)) {
							for (HashMap<String,Object> value: values) {
								oldSensor.values.add(value);
							}
						}
					} else {
						if (((String)oldSensor.attributes.get("type")).contentEquals((String)sensor.attributes.get("type"))) {
							for (HashMap<String,Object> value: values) {
								oldSensor.values.add(value);
							}
						} else {
							// store in general entity history?
						}
					}
					
				}	
			}
		}
		return _old;
	}
	
	/**
	 * 
	 * @param _entity
	 */
	public void removeEntity(Entity _entity) {
		this.entities.remove(_entity.uuid);
	}
	
	/**
	 * 
	 * @param _uuid
	 * @return
	 */
	public Entity getEntity(String _uuid) {
		return this.entities.get(_uuid);
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
		Iterator<RowEntry> rows = this.entities.keySet().iterator();
		while (rows.hasNext()) {
			RowEntry row = rows.next();
			if (row.location != null) {
				double x = row.location[0] - _lat;
				double y = row.location[1] - _lon;
				int cal = (int) (Math.sqrt(Math.pow(x, 2)+Math.pow(y, 2))/ 0.000008998719243599958); //for debugging
				if ((Math.sqrt(Math.pow(x, 2)+Math.pow(y, 2))/ 0.000008998719243599958) < _radius) {
					includedEntities.add(this.entities.get(row));
				}
			}
		}
		return includedEntities;
	}
	

}
