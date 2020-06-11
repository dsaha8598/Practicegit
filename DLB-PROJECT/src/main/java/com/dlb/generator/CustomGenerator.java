package com.dlb.generator;

import org.springframework.stereotype.Component;

@Component
public class CustomGenerator {

	 public static String generateOTP()  
	    {  //int randomPin declared to store the otp 
	        //since we using Math.random() hence we have to type cast it int 
	        //because Math.random() returns decimal value 
	        int randomPin   =(int) (Math.random()*9000)+1000; 
	        String otp  = String.valueOf(randomPin); 
	        return otp; //returning value of otp 
	    } 
}
