package com.my.twolevelcahce;

public class CahceOverfullException extends Exception {
	public CahceOverfullException() {
		super("CahceOverfullException: Cannot put object");
	}
}
