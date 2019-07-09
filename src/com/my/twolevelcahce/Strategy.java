package com.my.twolevelcahce;

import java.io.Serializable;

public interface Strategy<KeyType, ValueType extends Serializable> {
	void putObject(KeyType key, ValueType value)  throws CahceOverfullException;
	ValueType getObject(KeyType key, TwoLevelCache twoLevelCache);
	void updateObject(KeyType key, ValueType value);
	void removeObject(KeyType key);
}
