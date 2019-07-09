package com.my.twolevelcahce;

import java.io.Serializable;

public class TwoLevelCache<KeyType,ValueType extends Serializable> {
	
	/*private FileCahce<KeyType,ValueType> fileCache;
	private MemoryCahce<KeyType,ValueType> memoryCache;
	private int memoryCacheMaxSize;
	private int fileCacheMaxSize;*/
	
	private Strategy strategy;
	
	public TwoLevelCache(/*FileCahce<KeyType, ValueType> fileCache, MemoryCahce<KeyType, ValueType> memoryCache, int memoryCacheMaxSize, int fileCacheMaxSize,*/ Strategy strategy) {
		
		/*this.fileCache = fileCache;
		this.memoryCache = memoryCache;
		this.memoryCacheMaxSize = memoryCacheMaxSize;
		this.fileCacheMaxSize = fileCacheMaxSize;*/
		this.strategy = strategy;
		
	}
	
	public void put(KeyType key, ValueType value) throws CahceOverfullException {
		strategy.putObject(key, value);
	}
	
	public ValueType get(KeyType key) {
		return (ValueType) strategy.getObject(key);
	}
	
	public void update(KeyType key, ValueType value) {
		strategy.updateObject(key, value);
	}
	
	public void delete(KeyType key) {
		strategy.removeObject(key);
	}
	
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}
}
