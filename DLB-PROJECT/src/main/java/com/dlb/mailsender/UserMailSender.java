package com.dlb.mailsender;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import javax.mail.internet.MimeMessage;

import com.dlb.controller.UserController;

@Component
public class UserMailSender {

	@Autowired
	JavaMailSender sender;
	MimeMessage message = null;
	MimeMessageHelper helper = null;

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	public String sendMail(String email, String otp) {
		logger.info("sending email to the user email id");

		try {
			logger.info("UserMailSender.sendMail() started");

			message = sender.createMimeMessage();
			helper = new MimeMessageHelper(message, true);
			helper.setTo(email);
			helper.setText("here is the link http://localhost:9092/updatePassword/"+email+" to update password with OTP"+otp);
			helper.setSubject("notification from Apna_Dukan_Social");
			helper.setSentDate(new Date());
			sender.send(message);

		} catch (Exception e) {
			logger.info("Problem occured while sendin email  {}:"+e);
		}
		return "success";

	}

}
