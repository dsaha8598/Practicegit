package com.ey.in.tds.web.rest.util;

import java.time.Instant;
import java.util.Date;

import org.springframework.http.HttpStatus;

import com.ey.in.tds.core.exceptions.CustomException;

public class CommonValidations {
	
	public static void validateApplicableFields(Instant applicableToDb, Instant applicableFrom) {
		if(applicableToDb == null) {
			throw new CustomException("Please update previous record Applicable To in order to create new.",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}else if(applicableToDb.isAfter(applicableFrom)) {
			throw new CustomException("Current Applicable From should be greater than previous record Applicable To.",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}else if (applicableToDb.equals(applicableFrom)) {
			throw new CustomException("Current Applicable From should not be equal to previous record Applicable To.",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	public static void validateApplicableFields(Date applicableToDb, Date applicableFrom) {
		if(applicableToDb == null) {
			throw new CustomException("Please update previous record Applicable To in order to create new.",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}else if(applicableToDb.after(applicableFrom)) {
			throw new CustomException("Current Applicable From should be greater than previous record Applicable To.",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}else if (applicableToDb.equals(applicableFrom)) {
			throw new CustomException("Current Applicable From should not be equal to previous record Applicable To.",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
