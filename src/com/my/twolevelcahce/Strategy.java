package com.my.twolevelcahce;

import java.io.Serializable;

public interface Strategy<KeyType, ValueType extends Serializable> {
	void putObject(KeyType key, ValueType value, TwoLevelCache<KeyType, ValueType> twoLevelCache)  throws CahceOverfullException;
	ValueType getObject(KeyType key, TwoLevelCache<KeyType, ValueType> twoLevelCache);
	void updateObject(KeyType key, ValueType value, TwoLevelCache<KeyType, ValueType> twoLevelCache);
	void removeObject(KeyType key, TwoLevelCache<KeyType, ValueType> twoLevelCache);
	KeyType findDisplacedObject(MemoryCahce<KeyType, ValueType> memoryCache); // Найти Объект в RAM кэше, который подлежит замещению
	void displaceObject(KeyType displacingObjKey, TwoLevelCache<KeyType, ValueType> twoLevelCache); // Заместить Объект в RAM кэше Объектом из файлового кэша
	void putObjectToMemoryCache(KeyType key, TwoLevelCache<KeyType, ValueType> twoLevelCache); // Скопировать Объект из файлового кэша в RAM кэш
}
