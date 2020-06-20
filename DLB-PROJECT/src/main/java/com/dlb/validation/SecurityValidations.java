package com.dlb.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.dlb.model.UserDomain;
import com.dlb.repository.UserRepository;

@Service
public class SecurityValidations {

	@Autowired
	private UserRepository repo;

	private static final Logger logger = LoggerFactory.getLogger(SecurityValidations.class);

	/**
	 * to validate the user data during sign up
	 * @param imageFile
	 * @param domain
	 */
	public void validateSignUpUser(MultipartFile imageFile, UserDomain domain) {
		logger.info("validating the user data while signing up");
		//valodating image
		if (imageFile == null) {
			throw new RuntimeException("Image file not present {}");
		}
        //validating email
		if (domain.getEmail() != null) {
			if (!domain.getEmail().contains("@gmail.com") || !domain.getEmail().contains("@GMAIL.COM")) {
				throw new RuntimeException("validation failed {}: Email Id is not valid");
			}
			if (repo.getCountByEmail(domain.getEmail()) != 0) {
				throw new RuntimeException("validation failed {}: Email Id is already present with another account");
			}
		} else {
			throw new RuntimeException("Email is not present {}:");
		}
        //validating email
		if (!domain.getFullName().isEmpty()) {
			
		} else {
			throw new RuntimeException("Full name can not be empty{}:");
		}
		//validating full name
		if (domain.getFullName().isEmpty()) {
			throw new RuntimeException("User name can not be empty{}:");
		}
		else {
			Pattern pattern = Pattern.compile("[a-zA-Z0-9]*");
			Matcher matcher = pattern.matcher(domain.getFullName());
			if (!matcher.matches()) {
				throw new RuntimeException("validation failed {}: Special characters not allowed in Full name");
			}
		}
		//validating phone no
		if (domain.getPhNo() == null) {
			throw new RuntimeException("Phone number can not be empty {}:");
		} else {
			if (Long.toString(domain.getPhNo()).length() != 10) {
				throw new RuntimeException("Validation failed  {}: Phone number should be 10 digits");
			}
		}
		//validating passwords
		if (domain.getPassword() != null && domain.getConfirmPassword() != null
				&& domain.getPassword().equals(domain.getConfirmPassword())) {
			throw new RuntimeException("Validation failed  {}: Passwords doesnot match");
		} else {
			throw new RuntimeException("Validation failed  {}: passwords can not be empty");
		}
	}
	
	/**
	 * to validate log in credentials
	 * @param email
	 * @param password
	 * @return
	 */
	public String validateLoginData(String email,String password) {
		String message="";
	   if(!email.isEmpty() && repo.getCountByEmail(email) == 0) {
		   message="Invalid UserName or Password";
	   }
	   if(email.isEmpty()) {
		   message="Please enter email id";
	   }
	   if(password==null) {
		   message="Please enter Password";
	   }
	   return message;
	}
	
	public Integer validationForForgotPassword(String email) {
		return repo.getCountByEmail(email);
	}
}
