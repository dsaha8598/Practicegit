package com.ey.in.tds.returns.dto;

public class FormGenerationException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public FormGenerationException(Throwable rootException, String errorMessage) {
		super(errorMessage, rootException);
	}
	
	public FormGenerationException(Throwable rootException) {
		super(rootException);
	}
	
	public FormGenerationException(String errorMessage) {
		super(errorMessage);
	}
}
