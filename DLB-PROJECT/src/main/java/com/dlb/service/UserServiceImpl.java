package com.dlb.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.dlb.controller.UserController;
import com.dlb.entity.UserEntity;
import com.dlb.generator.CustomGenerator;
import com.dlb.mailsender.UserMailSender;
import com.dlb.model.UserDomain;
import com.dlb.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private UserMailSender sender;

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);
	UserEntity userid = null;

	public UserEntity createUserAccount(UserDomain domain,MultipartFile imageFile) {
		logger.info("Processing to save user creation data {}");
		UserEntity userEntity = new UserEntity();
		String dateOfBirth = domain.getDateOfBirth();
		
		try {
			
			 byte[] bytes=imageFile.getBytes();
						 ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
					    BufferedImage image=ImageIO.read(bis);
					    ByteArrayOutputStream pngContent = new ByteArrayOutputStream();
				         ImageIO.write(image, "png", pngContent);
					   byte[] encodeBase64 = Base64.encodeBase64(pngContent.toByteArray());//.encode(null);   
						   String base64Encoded = new String(encodeBase64, "UTF-8");
			
			Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateOfBirth);
			System.out.println(dateOfBirth);
			userEntity.setFullName(domain.getFullName());
			userEntity.setUserName(domain.getUserName());
			userEntity.setPassword(domain.getConfirmPassword());
			userEntity.setPhNo(domain.getPhNo());
			userEntity.setEmail(domain.getEmail());
			userEntity.setGender(domain.getGender());
			
			
			userEntity.setDateOfBirth(date);
			userEntity.setProfilePic(base64Encoded);

			userid = userRepo.save(userEntity);
			logger.info("saved user account creation data {}");
		} catch (Exception exception) {
			throw new RuntimeException(""+exception);
		}
		return userid;
	}
	
	/**
	 * this mwhtod is used to get userobject by username
	 * @param userName
	 * @return
	 */
	

	public UserEntity showUserProfile(String userName) {
		// TODO Auto-generated method stub
		
		int uidbyUsername = userRepo.getUidbyUsername(userName);
		Optional<UserEntity> optional = userRepo.findById(uidbyUsername);
		UserEntity userEntity = optional.get();
	
		return userEntity;
	}
	
	public UserEntity checkPassword(String email,String password) {
		
		UserEntity entity=userRepo.getByUserName(email);
		if(entity.getPassword().equals(password)) {
			return entity;
		}
		else {
			return null;
		}
	}
		
		public UserEntity checkEmail(String email) {
			
			UserEntity entity=userRepo.getByUserName(email);
			if(entity.getEmail().equals(email)) {
				return entity;
			}
			else {
				return null;
			}
		
	}
	
	public UserEntity getByEmailAndPassword(String email,String password) {
		UserEntity entity=userRepo.getByEmailAndPassword(email, password);
		return entity;
	}
	
	/**
	 * this method is used to send email with otp
	 * @param email
	 * @return
	 */
	
	
	public String sendEmailtoUser(UserDomain domain) {
		System.out.println("UserServiceImpl.sendEmailtoUser() start");
		String otp=CustomGenerator.generateOTP();
		String email=domain.getEmail();
	
		
		String sendMail = sender.sendMail(email, otp);
		
		Integer uid= userRepo.getUidbyEmail(email);
		Optional<UserEntity> findById = userRepo.findById(uid);
		UserEntity entity=findById.get();
		entity.setOtp(otp);
		UserEntity save = userRepo.save(entity);
		
		System.out.println("UserServiceImpl.sendEmailtoUser() ended");
		
		return sendMail;
		
	}
	
	
	
	
	public UserEntity saveUpdatedPassword(UserDomain domain) {
		
		UserEntity userEntity=new UserEntity();
		
		
		
		return null;
		
	}

}
