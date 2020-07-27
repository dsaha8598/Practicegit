package com.dlb.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class DLBSsecurity extends WebSecurityConfigurerAdapter {

	@Autowired
	private UserDetailsSerrviceImpl userDetailsSerrviceImpl;
	
	@Autowired
	AuthenticationManager authManager;
	
	@Autowired
	private JwtUtill jwtutil;
	
	@Autowired
	private RequestFilter requestFilter;
	
	@Override
	public void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsSerrviceImpl);
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().authorizeRequests().antMatchers("/home","/loginPostCredentials","/login","/signupNew","/signupPost").permitAll().anyRequest().authenticated();
		http.addFilterBefore(requestFilter, UsernamePasswordAuthenticationFilter.class);
	}

	
	@SuppressWarnings("deprecation")
	@Bean
	public PasswordEncoder passwordEncoder() {
		return NoOpPasswordEncoder.getInstance();
	}
	
	@Override
	@Bean
	public AuthenticationManager authenticationManagerBean()throws Exception{
		return super.authenticationManagerBean();
	}
	
	public String generateAuthenticationToken(AuthenticationRequest request) throws Exception {
		try {
		authManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUserName(), request.getPassword()));	
		}
		catch(BadCredentialsException e) {
			throw new Exception("bad credentiasl");
		}
		final UserDetails userDetails=userDetailsSerrviceImpl.loadUserByUsername(request.getUserName());
		String jwtToken=jwtutil.generateToken(userDetails);
		return jwtToken;
	}
	
}
	