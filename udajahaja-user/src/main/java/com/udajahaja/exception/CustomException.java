package com.udajahaja.exception;


public class CustomException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2849121787125894131L;

	public CustomException(String message) {
		super(message);
	}
	
	public CustomException(Exception e) {
		super(e);
	}
}
