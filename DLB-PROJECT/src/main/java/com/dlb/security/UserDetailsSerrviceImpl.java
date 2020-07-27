package com.dlb.security;

import java.util.ArrayList;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsSerrviceImpl implements UserDetailsService{
	User user=null;
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// TODO Auto-generated method stub
		return user;
	}
    public void setUserNameAndPassword(String userName,String password) {
    	 user=new User(userName,password,new ArrayList<>());
    }
}
