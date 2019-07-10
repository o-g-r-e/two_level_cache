package com.my.twolevelcahce;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UpdateTimeStrategy<KeyType, ValueType extends Serializable> implements Strategy<KeyType, ValueType> {

	private Map<KeyType, Long> fileUpdateTime;
	private Map<KeyType, Long> memoryUpdateTime;
	private Map<KeyType, Long> lastCallTime;

	public UpdateTimeStrategy() {
		this.fileUpdateTime = new HashMap<KeyType, Long>();
		this.memoryUpdateTime = new HashMap<KeyType, Long>();
		this.lastCallTime = new HashMap<KeyType, Long>();
	}

	@Override
	public void putObject(KeyType key, ValueType value, TwoLevelCache twoLevelCache) throws CahceOverfullException {
		if(twoLevelCache.getFileCache().getDataVolume() >= twoLevelCache.getFileCacheMaxSize()) {
			throw new CahceOverfullException();
		}
		twoLevelCache.getFileCache().put(key, value);
		fileUpdateTime.put(key, new Date().getTime());
	}

	@Override
	public ValueType getObject(KeyType key, TwoLevelCache twoLevelCache) {
		
		MemoryCahce<KeyType, ValueType> memoryCache = twoLevelCache.getMemoryCache();
		
		if(memoryCache.containsKey(key)) {
			if(isMemoryObjectOutdated(key)) {
				moveObjectToMemoryCache(key, twoLevelCache);
				twoLevelCache.incrementMemoryCacheMisses();
			} else {
				twoLevelCache.incrementMemoryCacheHits();
			}
			
		} else {
			if(memoryCache.getDataVolume() < twoLevelCache.getMemoryCacheMaxSize()) {
				moveObjectToMemoryCache(key, twoLevelCache);
				twoLevelCache.incrementMemoryCacheMisses();
			} else {
				KeyType disKey = (KeyType) findDisplaycedObject(twoLevelCache.getMemoryCache());
				memoryCache.delete(disKey);
				moveObjectToMemoryCache(key, twoLevelCache);
			}
		}
		
		lastCallTime.put(key, new Date().getTime());
		
		return memoryCache.get(key);
	}
	
	private KeyType findDisplaycedObject(MemoryCahce<KeyType, ValueType> memoryCache) {
		KeyType minTimeKey = memoryCache.getData().entrySet().iterator().next().getKey();
		Long minTimeValue = lastCallTime.get(minTimeKey);
		
		for(Map.Entry<KeyType, ValueType> entry : memoryCache.getData().entrySet()) {
			if(lastCallTime.get(entry.getKey()) < minTimeValue) {
				minTimeKey = entry.getKey();
				minTimeValue = lastCallTime.get(minTimeKey);
			}
		}
		
		return minTimeKey;
	}
	
	private boolean isMemoryObjectOutdated(KeyType key) {
		if(!memoryUpdateTime.containsKey(key) || !fileUpdateTime.containsKey(key) || memoryUpdateTime.get(key) == null || fileUpdateTime.get(key) == null) {
			return false;
		}
		
		if(memoryUpdateTime.get(key) < fileUpdateTime.get(key)) {
			return true;
		}
		
		return false;
	}
	
	private void moveObjectToMemoryCache(KeyType key, TwoLevelCache twoLevelCache) {
		if(twoLevelCache.getFileCache().containsKey(key)) {
			twoLevelCache.getMemoryCache().put(key, twoLevelCache.getFileCache().get(key));
			long time = new Date().getTime();
			if(fileUpdateTime.containsKey(key)) {
				time = fileUpdateTime.get(key);
			}
			memoryUpdateTime.put(key, time);
		}
	}
	
	@Override
	public void updateObject(KeyType key, ValueType value, TwoLevelCache twoLevelCache) {
		twoLevelCache.getFileCache().put(key, value);
		fileUpdateTime.put(key, new Date().getTime());
	}

	@Override
	public void removeObject(KeyType key, TwoLevelCache twoLevelCache) {
		twoLevelCache.getMemoryCache().delete(key);
		twoLevelCache.getFileCache().delete(key);
		if(lastCallTime.containsKey(key)) {
			lastCallTime.remove(key);
		}
		if(memoryUpdateTime.containsKey(key)) {
			memoryUpdateTime.remove(key);
		}
		if(fileUpdateTime.containsKey(key)) {
			fileUpdateTime.remove(key);
		}
	}

}
