package com.my.twolevelcahce;

public interface Cache<KeyType, ValueType> {
	void put(KeyType key, ValueType value);
	ValueType get(KeyType key);
	void delete(KeyType key);
	boolean containsKey(KeyType key);
	int getDataVolume();
}
