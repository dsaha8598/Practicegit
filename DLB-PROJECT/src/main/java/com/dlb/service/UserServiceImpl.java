package com.dlb.service;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dlb.entity.UserEntity;
import com.dlb.model.UserDomain;
import com.dlb.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {
	@Autowired
	private UserRepository userRepo; 
	
	UserEntity userid=null;
	
	public UserEntity saveUser(UserDomain domain) {
		UserEntity userEntity=new UserEntity();
		
		String dateOfBirth = domain.getDateOfBirth();
		
		
		try {
	    Date date=new SimpleDateFormat("yyyy-MM-dd").parse(dateOfBirth);
	    System.out.println(dateOfBirth);
		userEntity.setFullName(domain.getFullName());
		userEntity.setDateOfBirth(date);
		
		
		userid=userRepo.save(userEntity);
		
		
		}catch(Exception e) {
			e.printStackTrace();
		}
		return userid;
	}

}
