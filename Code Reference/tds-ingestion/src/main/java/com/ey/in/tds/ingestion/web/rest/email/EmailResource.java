package com.ey.in.tds.ingestion.web.rest.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ey.in.tds.common.model.emailnotification.Email;
import com.ey.in.tds.common.service.EmailService;
import com.ey.in.tds.core.util.ApiStatus;

@RestController
@RequestMapping("/api")
public class EmailResource {

	@Autowired
	private EmailService emailService;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@PostMapping("/mailsend")
	public ResponseEntity<ApiStatus<String>> sendMail(@RequestBody Email mail) {
		emailService.sendMessageWithAttachment(mail.getTo(), mail.getSubject(), mail.getContent(), null);
		String mailResponse = "Mail Sent successfully";
		logger.info("Mail Sent successfully");
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS", "MAIL SENDING", mailResponse);
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

}
