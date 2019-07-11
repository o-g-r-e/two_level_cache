package com.my.twolevelcahce;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CallFrequencyStrategy<KeyType, ValueType extends Serializable> implements Strategy<KeyType, ValueType> {
	
	private Map<KeyType, Integer> callFrequencies; // Количество вызовов Объекта
	
	public CallFrequencyStrategy() {
		callFrequencies = new HashMap<KeyType, Integer>();
	}
	
	@Override
	public ValueType getObject(KeyType key, TwoLevelCache<KeyType, ValueType> twoLevelCache) {
		
		MemoryCahce<KeyType, ValueType> memoryCache = twoLevelCache.getMemoryCache();
		int memoryCacheMaxSize = twoLevelCache.getMemoryCacheMaxSize();
		
		incrementCallFrequency(key);
		
		if(memoryCache.containsKey(key)) {
			twoLevelCache.incrementMemoryCacheHits();
			return memoryCache.get(key);
		}
		
		twoLevelCache.incrementMemoryCacheMisses();
		
		if(memoryCache.getDataVolume() < memoryCacheMaxSize) {
			putObjectToMemoryCache(key, twoLevelCache);
			return memoryCache.get(key);
		}
		
		displaceObject(key, twoLevelCache);
		
		return memoryCache.get(key);
	}
	
	private void incrementCallFrequency(KeyType key) {
		if(callFrequencies.containsKey(key)) {
			callFrequencies.put(key, callFrequencies.get(key).intValue()+1);
		}
	}
	
	@Override
	public void putObjectToMemoryCache(KeyType key, TwoLevelCache<KeyType, ValueType> twoLevelCache) {
		if(twoLevelCache.getFileCache().containsKey(key)) {
			twoLevelCache.getMemoryCache().put(key, twoLevelCache.getFileCache().get(key));
		}
	}
	
	@Override
	public KeyType findDisplacedObject(MemoryCahce<KeyType, ValueType> memoryCache) {
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
	public void displaceObject(KeyType displacingObjKey, TwoLevelCache<KeyType, ValueType> twoLevelCache) {
		KeyType displacedObjKey = findDisplacedObject(twoLevelCache.getMemoryCache());
		twoLevelCache.getMemoryCache().delete(displacedObjKey);
		putObjectToMemoryCache(displacingObjKey, twoLevelCache);
	}

	@Override
	public void putObject(KeyType key, ValueType value, TwoLevelCache<KeyType, ValueType> twoLevelCache) throws CahceOverfullException {
		if(twoLevelCache.getFileCache().getDataVolume() >= twoLevelCache.getFileCacheMaxSize()) {
			throw new CahceOverfullException();
		}
		twoLevelCache.getFileCache().put(key, value);
		callFrequencies.put(key, new Integer(0));
	}

	@Override
	public void updateObject(KeyType key, ValueType value, TwoLevelCache<KeyType, ValueType> twoLevelCache) {
		twoLevelCache.getFileCache().put(key, value);
	}

	@Override
	public void removeObject(KeyType key, TwoLevelCache<KeyType, ValueType> twoLevelCache) {
		twoLevelCache.getMemoryCache().delete(key);
		twoLevelCache.getFileCache().delete(key);
		if(callFrequencies.containsKey(key)) {
			callFrequencies.remove(key);
		}
	}
}
