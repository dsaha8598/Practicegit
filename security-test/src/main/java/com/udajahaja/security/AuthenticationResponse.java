package com.udajahaja.security;

public class AuthenticationResponse {

	private String jwtToken;
	
	public AuthenticationResponse(String jwtToken){
		this.jwtToken=jwtToken;
	}
	
	public AuthenticationResponse() {};
	
	public String getJwtToken() {
		return jwtToken;
	}
}
