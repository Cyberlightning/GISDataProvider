package com.cyberlightning.android.coap.memory;

/** @author Tomi Sarni 
 *  email: tomi.sarni@cyberlightning.com
 */

import java.util.HashMap;

/** This class simulates FLASH memory of low power devices. Class is of a singleton type and can be accessed by FlashMemory.getInstance() **/
public class FlashMemory {

	private static final FlashMemory _flashMemory = new FlashMemory();
	private HashMap<String,Object> _storage = new HashMap<String,Object>();
	
	private FlashMemory() {
		
	}
	
	public static FlashMemory getInstance () {
		return _flashMemory;
	}
	
	public void saveToFlash(String _key, Object _value) {
		this._storage.put(_key, _value);
	}
	
	public Object loadFromFlash (String _key) {
		
		if(this._storage.containsValue(_key)) {
			return this._storage.get(_key);
		}else {
			return null;
		}
	}
	
	
}
