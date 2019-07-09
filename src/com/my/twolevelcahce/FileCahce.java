package com.my.twolevelcahce;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FileCahce<KeyType, ValueType extends Serializable> implements Cache<KeyType, ValueType> {
	private String cacheDirectory;
	private Map<KeyType, String> filePaths;
	
	public FileCahce(String workDirectory) {
		this.cacheDirectory = workDirectory;
		this.filePaths = new HashMap<KeyType, String>();
	}
	
	public FileCahce() {
		this.cacheDirectory = new File(".").getAbsolutePath()+File.separator+"cache";
	}
	
	@Override
	public void put(KeyType key, ValueType value) {
		String objectFilePath = cacheDirectory + File.separator + UUID.randomUUID().toString() + ".obj";
	    
		filePaths.put(key, objectFilePath);

        FileOutputStream fileOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        
		try {
			
			fileOutputStream = new FileOutputStream(objectFilePath);
			objectOutputStream = new ObjectOutputStream(fileOutputStream);
			objectOutputStream.writeObject(value);
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				
				if(objectOutputStream != null) {
					objectOutputStream.close();
				}
				
				if(fileOutputStream != null) {
					fileOutputStream.close();
				}
				
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	@Override
	public ValueType get(KeyType key) {
		ValueType object = null;
		String objectFilePath = filePaths.get(key);
		
		FileInputStream fileInputStream = null;
		ObjectInputStream objectInputStream = null;
		try {
			
			fileInputStream = new FileInputStream(objectFilePath);
			objectInputStream = new ObjectInputStream(fileInputStream);
			object = (ValueType)objectInputStream.readObject();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			
            try {
            	if(objectInputStream != null) {
            		objectInputStream.close();
				}
				
				if(fileInputStream != null) {
					fileInputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
        return object;
	}

	@Override
	public void delete(KeyType key) {
		String objectFilePath = filePaths.get(key);
		File objectFile = new File(objectFilePath);
        objectFile.delete();
        filePaths.remove(key);
	}

	@Override
	public boolean containsKey(KeyType key) {
		return filePaths.containsKey(key);
	}

	@Override
	public int getDataVolume() {
		return filePaths.size();
	}

	public Map<KeyType, String> getFilePaths() {
		return filePaths;
	}

}
