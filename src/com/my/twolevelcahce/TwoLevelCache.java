package com.my.twolevelcahce;

import java.io.Serializable;
import java.util.Map;

public class TwoLevelCache<KeyType,ValueType extends Serializable> {
	
	private FileCahce<KeyType, ValueType> fileCache;
	private MemoryCahce<KeyType, ValueType> memoryCache;
	private int memoryCacheMaxSize;
	private int fileCacheMaxSize;
	private int memoryCacheMisses;
	private int memoryCacheHits;
	
	private Strategy strategy;
	
	public TwoLevelCache(String fileCacheFolder, int memoryCacheMaxSize, int fileCacheMaxSize, Strategy strategy) {
		
		this.fileCache = new FileCahce<KeyType, ValueType>(fileCacheFolder);
		this.memoryCache = new MemoryCahce<KeyType, ValueType>();
		this.memoryCacheMaxSize = memoryCacheMaxSize;
		this.fileCacheMaxSize = fileCacheMaxSize;
		this.strategy = strategy;
		
		this.memoryCacheMisses = 0;
		this.memoryCacheHits = 0;
		
	}
	
	public void put(KeyType key, ValueType value) throws CahceOverfullException {
		strategy.putObject(key, value, this);
	}
	
	public ValueType get(KeyType key) {
		return (ValueType) strategy.getObject(key, this);
	}
	
	public void update(KeyType key, ValueType value) {
		strategy.updateObject(key, value, this);
	}
	
	public void delete(KeyType key) {
		strategy.removeObject(key, this);
	}
	
	protected FileCahce<KeyType, ValueType> getFileCache() {
		return fileCache;
	}
	
	protected MemoryCahce<KeyType, ValueType> getMemoryCache() {
		return memoryCache;
	}
	
	protected int getMemoryCacheMaxSize() {
		return memoryCacheMaxSize;
	}
	
	protected int getFileCacheMaxSize() {
		return fileCacheMaxSize;
	}
	
	public int getMemoryCacheMisses() {
		return memoryCacheMisses;
	}
	
	public int getMemoryCacheHits() {
		return memoryCacheHits;
	}
	
	public void incrementMemoryCacheMisses() {
		memoryCacheMisses++;
	}
	
	public void incrementMemoryCacheHits() {
		memoryCacheHits++;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("\nMemory cache:\n");
		if(memoryCache.getData().size() <= 0) {
			result.append("[]\n");
		} else {
			for(Map.Entry<KeyType,ValueType> e : memoryCache.getData().entrySet()) {
				result.append("[");
				result.append(e.getKey());
				result.append(", ");
				result.append(e.getValue());
				result.append("]\n");
			}
		}
		
		result.append("\nFile cache:\n");
		if(fileCache.getFilePaths().size() <= 0) {
			result.append("[]\n");
		} else {
			for(Map.Entry<KeyType, String> e : fileCache.getFilePaths().entrySet()) {
				result.append("[");
				result.append(e.getKey());
				result.append(", ");
				result.append(e.getValue());
				result.append("]\n");
			}
		}
		return result.toString();
	}
}
