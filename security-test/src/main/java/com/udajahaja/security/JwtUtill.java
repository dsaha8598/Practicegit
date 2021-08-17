package com.udajahaja.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class JwtUtill {

	private String SECURITY_KEY = "SECURteb94C%*#@5VGRCCW$@!EITY865@5$%6&23$FFh";

	public String extraxtUserName(String token) {
		return extractClaim(token,Claims::getSubject);
	}
	
	public Date exctractExpiration(String token) {
		return extractClaim(token,Claims::getExpiration);
	}
	
	public <T> T extractClaim(String token,Function<Claims,T> claimResolver) {
		final Claims claims=extractAiiClaims(token);
		return claimResolver.apply(claims);
	}

	private Claims extractAiiClaims(String token) {
		return Jwts.parser().setSigningKey(SECURITY_KEY).parseClaimsJws(token).getBody();
	}

	private boolean isTokenExpired(String token) {
        return exctractExpiration(token).before(new Date());
	}

	private String createToken(Map<String, Object> claims, String subject) {
		return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + 1000000))
				.signWith(SignatureAlgorithm.HS256, SECURITY_KEY).compact();
	}
	
	public String generateToken(UserDetails userDetails) {
		Map<String,Object> claims=new HashMap<String,Object>();
		return createToken(claims,userDetails.getUsername());
	}
	
	public Boolean validateToken(String token,UserDetails userDetails) {
		final String userName=extraxtUserName(token);
		return (userName.equals(userDetails.getUsername())&& !isTokenExpired(token));
	}
}
