package com.my.twolevelcahce;

public interface Strategy<KeyType, ValueType> {
	void putObject(KeyType key, ValueType value)  throws CahceOverfullException;
	ValueType getObject(KeyType key);
	void updateObject(KeyType key, ValueType value);
	void removeObject(KeyType key);
}
