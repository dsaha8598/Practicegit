package com.dlb.service;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dlb.controller.UserController;
import com.dlb.entity.UserEntity;
import com.dlb.model.UserDomain;
import com.dlb.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {
	@Autowired
	private UserRepository userRepo;

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);
	UserEntity userid = null;

	public UserEntity createUserAccount(UserDomain domain) {
		logger.info("Processing to save user creation data {}");
		UserEntity userEntity = new UserEntity();
		String dateOfBirth = domain.getDateOfBirth();
		try {
			Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateOfBirth);
			System.out.println(dateOfBirth);
			userEntity.setFullName(domain.getFullName());
			userEntity.setUserName(domain.getUserName());
			userEntity.setPassword(domain.getPassword());
			userEntity.setPhNo(domain.getPhNo());
			userEntity.setEmail(domain.getEmail());
			userEntity.setGender(domain.getGender());
			userEntity.setCreatedDate(new Date());
			userEntity.setDateOfBirth(date);

			userid = userRepo.save(userEntity);
			logger.info("saved user account creation data {}");
		} catch (Exception exception) {
			logger.info("exception occured during saving user creation data {} " + exception);
		}
		return userid;
	}

}
