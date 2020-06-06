package com.dlb.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

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
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	/***
	 * this method is to show start page having login and create button
	 * 
	 * @return
	 */

	@RequestMapping(value = "/home", method = RequestMethod.GET)
	public String showHome() {
		logger.info(" Executing home page {}");
		return "HomePage";

	}

	/**
	 * this method is to show login page
	 * 
	 * @return
	 */

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String showLogin(Model model) {
		logger.info(" displaying login page {}");
		UserDomain domain = new UserDomain();
		model.addAttribute("domain", domain);
		return "Login";
	}

	/**
	 * this method is to show signup page
	 * 
	 * @return
	 */

	@RequestMapping(value = "/SignUp", method = RequestMethod.GET)
	public String showSignUp(Model model) {
		logger.info(" displaying sign up page {}");
		UserDomain signUpdomain = new UserDomain();
		model.addAttribute("signUpdomain", signUpdomain);

		return "Signup";
	}

	/**
	 * to recieve the user data from UI
	 * 
	 * @param model
	 * @param domain
	 * @return
	 */
	@RequestMapping(value = "/signupPost", method = RequestMethod.POST)
	public String storeUserdata(Model model, @RequestParam("file") MultipartFile imageFile,
			@ModelAttribute(name = "signUpdomain") UserDomain domain) {
		System.out.println("UserController.storeUserdata()");
		UserEntity userid = service.createUserAccount(domain, imageFile);
		model.addAttribute("obj", userid);

		return "forward:/homepage";

	}

	@RequestMapping(value = "/homepage", method = RequestMethod.POST)
	public String showHomeAppPage() {
		logger.info(" Executing home page of Application{}");
		return "AppHomePage";
	}

	/**
	 * this method is used to show the user profile by username by queryparameter
	 * 
	 * @param name
	 * @return
	 */

	@RequestMapping(value = "/showProfileByName")
	public String showProfile(@RequestParam String name) {

		String userName = name;

		UserEntity user = service.showUserProfile(userName);
		return "AppHomePage";

	}

	@RequestMapping(value = "/newPassword", method = RequestMethod.GET)
	public String newPasswordPage() {
		logger.info("Displaying Generating new password page");
		return "NewPwd";
	}

}
