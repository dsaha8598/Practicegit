package com.dlb.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class UserController {
	/**
	 * this is controller classs
	 */
	
	
	private static final Logger log = LoggerFactory.getLogger(UserController.class);
	
	@RequestMapping(value = "/home",method = RequestMethod.GET)
	public String showHome() {
		log.info("*********************************showhome started***********************");
		
		return "HomePage";
		
	}

}
