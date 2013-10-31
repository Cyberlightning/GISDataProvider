package com.cyberlightning.webserver.entities;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntityTable implements java.io.Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8536303237731902808L;
	//private HashMap<String,Entity> entities = new HashMap<Integer,Entity>();
	private Map<String, Entity> entities = new ConcurrentHashMap<String, Entity>(); 
	
	
	public boolean hasEntity(String _uuid) {
		
		if (this.entities.containsKey(_uuid)) {
			return true;	
		} else {
			return false;
		}
		
	}
	
	public void addEntity(Entity _entity) {
		//if (this.entities == null) this.entities = new ConcurrentHashMap<String, Entity>();
		this.entities.put(_entity.uuid, _entity);
	}
	
	public void removeEntity(Entity _entity) {
		this.entities.remove(_entity.uuid);
	}
	
	public Entity getEntity(String _uuid) {
		return this.entities.get(_uuid);
	}
	

}
