package com.ibm.replication.iidr.igcrest;

@SuppressWarnings("serial")
public class IGCRestException extends Exception {

	public int httpCode;
	public String message;
	
	public IGCRestException(int httpCode, String message) {
		this.httpCode = httpCode;
		this.message = message;
	}

}
