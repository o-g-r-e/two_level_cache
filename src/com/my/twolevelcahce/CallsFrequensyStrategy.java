package com.my.twolevelcahce;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CallsFrequensyStrategy<KeyType, ValueType extends Serializable> implements Strategy<KeyType, ValueType> {
	
	private Map<KeyType, Integer> callFrequencies;
	
	public CallsFrequensyStrategy() {
		callFrequencies = new HashMap<KeyType, Integer>();
	}
	
	@Override
	public ValueType getObject(KeyType key, TwoLevelCache twoLevelCache) {
		
		MemoryCahce<KeyType, ValueType> memoryCache = twoLevelCache.getMemoryCache();
		int memoryCacheMaxSize = twoLevelCache.getMemoryCacheMaxSize();
		
		incrementCallFrequency(key);
		
		if(memoryCache.containsKey(key)) {
			twoLevelCache.incrementMemoryCacheHits();
			return memoryCache.get(key);
		}
		
		twoLevelCache.incrementMemoryCacheMisses();
		
		if(memoryCache.getDataVolume() < memoryCacheMaxSize) {
			moveObjectToMemoryCache(key, twoLevelCache);
			return memoryCache.get(key);
		}
		
		KeyType disObjKey = findDisplaycedObject(memoryCache);
		
		memoryCache.delete(disObjKey);
		moveObjectToMemoryCache(key, twoLevelCache);
		
		return memoryCache.get(key);
	}
	
	private void incrementCallFrequency(KeyType key) {
		if(callFrequencies.containsKey(key)) {
			callFrequencies.put(key, callFrequencies.get(key).intValue()+1);
		}
	}
	
	private void moveObjectToMemoryCache(KeyType key, TwoLevelCache twoLevelCache) {
		if(twoLevelCache.getFileCache().containsKey(key)) {
			twoLevelCache.getMemoryCache().put(key, twoLevelCache.getFileCache().get(key));
		}
	}
	
	private KeyType findDisplaycedObject(MemoryCahce<KeyType, ValueType> memoryCache) {
		KeyType minFreqKey = memoryCache.getData().entrySet().iterator().next().getKey();
		Integer minFreqValue = callFrequencies.get(minFreqKey);
		
		for(Map.Entry<KeyType, ValueType> entry : memoryCache.getData().entrySet()) {
			if(callFrequencies.get(entry.getKey()) < minFreqValue) {
				minFreqKey = entry.getKey();
				minFreqValue = callFrequencies.get(minFreqKey);
			}
		}
		
		return minFreqKey;
	}

	@Override
	public void putObject(KeyType key, ValueType value, TwoLevelCache twoLevelCache) throws CahceOverfullException {
		if(twoLevelCache.getFileCache().getDataVolume() >= twoLevelCache.getFileCacheMaxSize()) {
			throw new CahceOverfullException();
		}
		twoLevelCache.getFileCache().put(key, value);
		callFrequencies.put(key, new Integer(0));
	}

	@Override
	public void updateObject(KeyType key, ValueType value, TwoLevelCache twoLevelCache) {
		twoLevelCache.getFileCache().put(key, value);
	}

	@Override
	public void removeObject(KeyType key, TwoLevelCache twoLevelCache) {
		twoLevelCache.getMemoryCache().delete(key);
		twoLevelCache.getFileCache().delete(key);
		if(callFrequencies.containsKey(key)) {
			callFrequencies.remove(key);
		}
	}

}
