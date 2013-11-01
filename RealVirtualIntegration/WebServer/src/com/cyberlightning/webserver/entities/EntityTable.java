package com.cyberlightning.webserver.entities;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class EntityTable implements java.io.Serializable {
	

	private static final long serialVersionUID = -8536303237731902808L;
	private Map<RowEntry, Entity> entities = new ConcurrentHashMap<RowEntry, Entity>(); 
	
	public boolean hasEntity(String _uuid) {
		
		if (this.entities.containsKey(_uuid)) {
			return true;	
		} else {
			return false;
		}
	}
	
	public void addEntity(RowEntry _entry, Entity _entity) {
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
		
	}
	
	private Entity updateValues(Entity _old, Entity _new) {
		for (Sensor sensor: _new.sensors) {
			for (int i = 0; i < sensor.values.size(); i++) {
				_old.sensors.get(_old.sensors.indexOf(sensor)).values.add(sensor.values.get(i));
				String key = sensor.values.get(i).keySet().iterator().next();
				_old.history.put(key,sensor.values.get(i).get(key));
			}
			
		}
		return _old;
	}
	
	public void removeEntity(Entity _entity) {
		this.entities.remove(_entity.uuid);
	}
	
	public Entity getEntity(String _uuid) {
		return this.entities.get(_uuid);
	}
	

}
