package com.my.twolevelcahce;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UpdateTimeStrategy<KeyType, ValueType extends Serializable> implements Strategy<KeyType, ValueType> {

	private Map<KeyType, Long> fileUpdateTime;
	private Map<KeyType, Long> memoryUpdateTime;
	private Map<KeyType, Long> lastCallTime;
	private FileCahce<KeyType, ValueType> fileCache;
	private MemoryCahce<KeyType,ValueType> memoryCache;
	private int memoryCacheMaxSize;
	private int fileCacheMaxSize;
	private int memoryCacheMisses;
	private int memoryCacheHits;

	public UpdateTimeStrategy(FileCahce<KeyType, ValueType> fileCache, MemoryCahce<KeyType, ValueType> memoryCache, int memoryCacheMaxSize, int fileCacheMaxSize) {
		this.fileCache = fileCache;
		this.memoryCache = memoryCache;
		this.memoryCacheMaxSize = memoryCacheMaxSize;
		this.fileCacheMaxSize = fileCacheMaxSize;
		
		this.fileUpdateTime = new HashMap<KeyType, Long>();
		this.memoryUpdateTime = new HashMap<KeyType, Long>();
		this.memoryCacheMisses = 0;
		this.memoryCacheHits = 0;
		this.lastCallTime = new HashMap<KeyType, Long>();
	}

	@Override
	public void putObject(KeyType key, ValueType value) throws CahceOverfullException {
		if(fileCache.getDataVolume() >= fileCacheMaxSize) {
			throw new CahceOverfullException();
		}
		fileCache.put(key, value);
		fileUpdateTime.put(key, new Date().getTime());
	}

	@Override
	public ValueType getObject(KeyType key) {
		if(!memoryCache.containsKey(key)) {
			if(memoryCache.getDataVolume() < memoryCacheMaxSize) {
				moveObjectToMemoryCache(key);
				memoryCacheMisses++;
				return memoryCache.get(key);
			} else {
				KeyType disKey = findDisplaycedObject();
				memoryCache.delete(disKey);
				moveObjectToMemoryCache(key);
			}
		}
		
		if(isMemoryObjectOutdated(key)) {
			moveObjectToMemoryCache(key);
			memoryCacheMisses++;
		} else {
			memoryCacheHits++;
		}
		
		lastCallTime.put(key, new Date().getTime());
		
		return memoryCache.get(key);
	}
	
	private KeyType findDisplaycedObject() {
		KeyType minTimeKey = lastCallTime.entrySet().iterator().next().getKey();
		Long minTimeValue = lastCallTime.entrySet().iterator().next().getValue();
		
		for(Map.Entry<KeyType, Long> entry : lastCallTime.entrySet()) {
			if(entry.getValue() < minTimeValue) {
				minTimeKey = entry.getKey();
				minTimeValue = entry.getValue();
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
	
	private void moveObjectToMemoryCache(KeyType key) {
		if(fileCache.containsKey(key)) {
			memoryCache.put(key, fileCache.get(key));
			long time = new Date().getTime();
			if(fileUpdateTime.containsKey(key)) {
				time = fileUpdateTime.get(key);
			}
			memoryUpdateTime.put(key, time);
		}
	}
	
	@Override
	public void updateObject(KeyType key, ValueType value) {
		fileCache.put(key, value);
		fileUpdateTime.put(key, new Date().getTime());
	}

	@Override
	public void removeObject(KeyType key) {
		memoryCache.delete(key);
		fileCache.delete(key);
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
