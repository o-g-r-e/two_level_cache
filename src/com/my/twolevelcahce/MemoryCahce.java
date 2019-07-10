package com.my.twolevelcahce;

import java.util.HashMap;
import java.util.Map;

public class MemoryCahce<KeyType, ValueType> implements Cache<KeyType, ValueType> {
	private Map<KeyType,ValueType> data;
	
	public MemoryCahce() {
		data = new HashMap<KeyType, ValueType>();
	}
	
	@Override
	public void put(KeyType key, ValueType value) {
		data.put(key, value);
	}

	@Override
	public ValueType get(KeyType key) {
		
		if(!data.containsKey(key)) {
			return null;
		}
		
		return data.get(key);
	}

	@Override
	public void delete(KeyType key) {
		if(data.containsKey(key)) {
			data.remove(key);
		}
	}

	@Override
	public boolean containsKey(KeyType key) {
		return data.containsKey(key);
	}

	@Override
	public int getDataVolume() {
		return data.size();
	}

	public Map<KeyType, ValueType> getData() {
		return data;
	}

}
