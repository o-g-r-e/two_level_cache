package com.my.twolevelcahce;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UpdateTimeStrategy<KeyType, ValueType extends Serializable> implements Strategy<KeyType, ValueType> {

	private Map<KeyType, Long> fileUpdateTime; // Время обновления Объекта в файловом кэше
	private Map<KeyType, Long> memoryUpdateTime; // Время обновления Объекта в RAM кэше
	private Map<KeyType, Long> lastCallTime; // Последнее время вызова Объекта

	public UpdateTimeStrategy() {
		this.fileUpdateTime = new HashMap<KeyType, Long>();
		this.memoryUpdateTime = new HashMap<KeyType, Long>();
		this.lastCallTime = new HashMap<KeyType, Long>();
	}

	@Override
	public void putObject(KeyType key, ValueType value, TwoLevelCache<KeyType, ValueType> twoLevelCache) throws CahceOverfullException {
		if(twoLevelCache.getFileCache().getDataVolume() >= twoLevelCache.getFileCacheMaxSize()) {
			throw new CahceOverfullException();
		}
		twoLevelCache.getFileCache().put(key, value);
		fileUpdateTime.put(key, new Date().getTime());
	}

	@Override
	public ValueType getObject(KeyType key, TwoLevelCache<KeyType, ValueType> twoLevelCache) {
		
		MemoryCahce<KeyType, ValueType> memoryCache = twoLevelCache.getMemoryCache();
		
		if(memoryCache.containsKey(key)) {
			if(isMemoryObjectOutdated(key)) {
				putObjectToMemoryCache(key, twoLevelCache);
				twoLevelCache.incrementMemoryCacheMisses();
			} else {
				twoLevelCache.incrementMemoryCacheHits();
			}
		} else {
			twoLevelCache.incrementMemoryCacheMisses();
			if(memoryCache.getDataVolume() < twoLevelCache.getMemoryCacheMaxSize()) {
				putObjectToMemoryCache(key, twoLevelCache);
			} else {
				displaceObject(key, twoLevelCache);
			}
		}
		
		lastCallTime.put(key, new Date().getTime());
		
		return memoryCache.get(key);
	}
	
	@Override
	public void displaceObject(KeyType displacingObjKey, TwoLevelCache<KeyType, ValueType> twoLevelCache) {
		KeyType displacedObjKey = findDisplacedObject(twoLevelCache.getMemoryCache());
		twoLevelCache.getMemoryCache().delete(displacedObjKey);
		putObjectToMemoryCache(displacingObjKey, twoLevelCache);
	}
	
	@Override
	public KeyType findDisplacedObject(MemoryCahce<KeyType, ValueType> memoryCache) {
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
	
	private boolean isMemoryObjectOutdated(KeyType key) { // Возвращает true если Объект в файловом кэше обновился
		
		if(!memoryUpdateTime.containsKey(key) || !fileUpdateTime.containsKey(key) || memoryUpdateTime.get(key) == null || fileUpdateTime.get(key) == null) {
			return false;
		}
		
		if(memoryUpdateTime.get(key) < fileUpdateTime.get(key)) {
			return true;
		}
		
		return false;
	}
	
	@Override
	public void putObjectToMemoryCache(KeyType key, TwoLevelCache<KeyType, ValueType> twoLevelCache) {
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
	public void updateObject(KeyType key, ValueType value, TwoLevelCache<KeyType, ValueType> twoLevelCache) {
		twoLevelCache.getFileCache().put(key, value);
		fileUpdateTime.put(key, new Date().getTime());
	}

	@Override
	public void removeObject(KeyType key, TwoLevelCache<KeyType, ValueType> twoLevelCache) {
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
