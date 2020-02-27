package com.task.mail;

import javax.mail.internet.MimeMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.task.reader.MailTextReader;

@Component
public class UserMailSender {
	/**
	 * injecting javaMailSender object
	 */
	@Autowired
    private JavaMailSender javaMailSender;
	/**
	 * injecting MailTextReader object
	 */
	@Autowired
	MailTextReader reader;
	/**
	 * injecting AppProperties object 
	 */
/*	@Autowired
	AppProperties properties;*/
	
	MimeMessage message=null; 
	 MimeMessageHelper helper=null;
	 String msg=null;
	
	private static Logger LOGGER = LogManager.getLogger(UserMailSender.class);
	
    public String sendUserMail(String firstName,String email) {
    	
    try {
    	LOGGER.info("************try block of sendUserMail method of  UserMailSender execution started************");
	 message = javaMailSender.createMimeMessage();
    
    // Enable the multipart flag!
    helper = new MimeMessageHelper(message,true);
    
    helper.setTo(email);//"shrabaneebiswal100@gmail.com"
    helper.setText(reader.readFile(firstName));//newContent

    helper.setSubject("ATPS Notification");//(properties.getMessages().get(AppConstants.SUBJECT1));

    javaMailSender.send(message);
    msg="success";
    
    }
    catch(Exception e) {
    	LOGGER.info("************catch block of sendUserMail method of  UserMailSender execution started************");
    	e.printStackTrace();
    	msg="fail";
    }
    LOGGER.info("************sendUserMail method of  UserMailSender execution ended************");
    return msg;
    }
 
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
   /* 
    public void mailWithUpdatedPassword(String pswd,String email) {
    	 try {
    	 message = javaMailSender.createMimeMessage();
    	 helper = new MimeMessageHelper(message,true);
    	 helper.setTo(email);//"shrabaneebiswal100@gmail.com"
    	    helper.setText(properties.getMessages().get("body")+"  "+pswd);//newContent
    	    helper.setSubject(properties.getMessages().get("subject2"));
    	    javaMailSender.send(message);
    	 }
    	 catch(Exception e) {
    	    	LOGGER.info("************catch block of mailWithUpdatedPassword method of  UserMailSender execution started************");
    	    	e.printStackTrace();
    	    }
    }*/
}
