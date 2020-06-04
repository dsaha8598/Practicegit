package com.dlb.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.dlb.entity.UserEntity;
import com.dlb.model.UserDomain;
import com.dlb.service.UserServiceImpl;

@Controller
public class UserController {
	/**
	 * this is controller classs
	 * 
	 */
	@Autowired
	private UserServiceImpl service;
	
	
	private static final Logger log = LoggerFactory.getLogger(UserController.class);
	
	/***
	 * this method is to show home page
	 * @return
	 */
	
	@RequestMapping(value = "/home",method = RequestMethod.GET)
	public String showHome() {
		log.info("*********************************showhome started***********************");
		
		return "HomePage";
		
	}
	/**
	 * this method is to show login page
	 * @return
	 */
	
	@RequestMapping(value = "/login",method = RequestMethod.GET)
	public String showLogin(Model model) {
		
		UserDomain domain=new UserDomain();
		model.addAttribute("domain", domain);
		
		return "Login";
	}
	
	
	/**
	 * this method is to show signup page
	 * @return
	 */
	
	@RequestMapping(value = "/SignUp",method = RequestMethod.GET)
	public String showSignUp(Model model) {
		
		UserDomain signUpdomain=new UserDomain();
		model.addAttribute("signUpdomain", signUpdomain);
		
		return "Signup";
	}
	
	
	@RequestMapping(value = "/signupPost",method = RequestMethod.POST)
	public String storeUserdata(Model model,@ModelAttribute(name = "signUpdomain") UserDomain domain) {
		System.out.println("UserController.storeUserdata()");
		UserEntity userid=service.saveUser(domain);
		model.addAttribute("obj", userid);
		
		return "Signup";
		
	}
	
	

}
