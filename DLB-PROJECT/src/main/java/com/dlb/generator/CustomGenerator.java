package com.dlb.generator;

import org.springframework.stereotype.Component;

@Component
public class CustomGenerator {

	public static String generateOTP() {
		int randomPin = (int) (Math.random() * 900000) + 1000;
		String otp = String.valueOf(randomPin);
		return otp; // returning value of otp
	}

}
