package com.my.twolevelcahce;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CallsFrequensyStrategy<KeyType, ValueType extends Serializable> implements Strategy<KeyType, ValueType> {
	/*private Map<KeyType, Integer> callFrequensyes;
	private FileCahce<KeyType, ValueType> fileCache;
	private MemoryCahce<KeyType,ValueType> memoryCache;
	private int memoryCacheMaxSize;
	private int fileCacheMaxSize;
	private int memoryCacheMisses;
	private int memoryCacheHits;*/
	
	
	
	/*public CallsFrequensyStrategy(FileCahce<KeyType, ValueType> fileCache, MemoryCahce<KeyType, ValueType> memoryCache, int memoryCacheMaxSize, int fileCacheMaxSize) {
		this.fileCache = fileCache;
		this.memoryCache = memoryCache;
		this.memoryCacheMaxSize = memoryCacheMaxSize;
		this.fileCacheMaxSize = fileCacheMaxSize;
		
		this.callFrequensyes = new HashMap<KeyType, Integer>();
		this.memoryCacheMisses = 0;
		this.memoryCacheHits = 0;
	}*/
	
	@Override
	public ValueType getObject(KeyType key, TwoLevelCache twoLevelCache) {
		updateCallFrequensyes(key);
		
		if(memoryCache.containsKey(key)) {
			//memoryCacheHits++;
			return memoryCache.get(key);
		}
		
		//memoryCacheMisses++;
		
		if(memoryCache.getDataVolume() < memoryCacheMaxSize) {
			moveObjectToMemoryCache(key);
			return memoryCache.get(key);
		}
		
		KeyType disObjKey = findDisplaycedObject();
		
		memoryCache.delete(disObjKey);
		moveObjectToMemoryCache(key);
		
		return memoryCache.get(key);
	}
	
	private void updateCallFrequensyes(KeyType key) {
		if(callFrequensyes.containsKey(key)) {
			callFrequensyes.put(key, callFrequensyes.get(key).intValue()+1);
		}
	}
	
	private void moveObjectToMemoryCache(KeyType key) {
		if(fileCache.containsKey(key)) {
			memoryCache.put(key, fileCache.get(key));
		}
	}
	
	private KeyType findDisplaycedObject() {
		KeyType minFreqKey = memoryCache.getData().entrySet().iterator().next().getKey();
		Integer minFreqValue = callFrequensyes.get(minFreqKey);
		
		for(Map.Entry<KeyType, ValueType> entry : memoryCache.getData().entrySet()) {
			if(callFrequensyes.get(entry.getKey()) < minFreqValue) {
				minFreqKey = entry.getKey();
				minFreqValue = callFrequensyes.get(minFreqKey);
			}
		}
		
		return minFreqKey;
	}

	@Override
	public void putObject(KeyType key, ValueType value) throws CahceOverfullException {
		if(fileCache.getDataVolume() >= fileCacheMaxSize) {
			throw new CahceOverfullException();
		}
		fileCache.put(key, value);
		callFrequensyes.put(key, new Integer(0));
	}

	@Override
	public void updateObject(KeyType key, ValueType value) {
		fileCache.put(key, value);
	}

	@Override
	public void removeObject(KeyType key) {
		memoryCache.delete(key);
		fileCache.delete(key);
		if(callFrequensyes.containsKey(key)) {
			callFrequensyes.remove(key);
		}
	}

}
