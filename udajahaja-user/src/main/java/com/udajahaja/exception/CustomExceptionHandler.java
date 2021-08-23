package com.udajahaja.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CustomExceptionHandler {

	@ExceptionHandler(value = CustomException.class)
	   public ResponseEntity<Object> exception(CustomException exception) {
	      return new ResponseEntity<>("No tickets found", HttpStatus.NOT_FOUND);
	   }
}
