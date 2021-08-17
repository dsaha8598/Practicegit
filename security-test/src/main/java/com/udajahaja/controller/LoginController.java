package com.udajahaja.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.udajahaja.security.JwtUtill;
import com.udajahaja.security.UserDetailsSerrviceImpl;

@RestController 
public class LoginController {
	
	@Autowired
	private UserDetailsSerrviceImpl userDetailsSerrviceImpl;
	
	@Autowired
	private JwtUtill jwtutil;
	
	@GetMapping("/login")
	public ResponseEntity<String> login() {
		String usename="dipak@gmail.com";
		String password="dipak678";
		userDetailsSerrviceImpl.setUserNameAndPassword(usename, password);
		UserDetails userDetails = userDetailsSerrviceImpl.loadUserByUsername(usename);
		String jwtToken = jwtutil.generateToken(userDetails);
		String sessionValue = "Bearer " + jwtToken;
		return new ResponseEntity<String>(sessionValue,HttpStatus.OK);
	}
}
