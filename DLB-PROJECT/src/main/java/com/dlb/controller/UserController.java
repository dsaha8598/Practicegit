package com.dlb.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.dlb.entity.UserEntity;
import com.dlb.model.UserDomain;
import com.dlb.security.JwtUtill;
import com.dlb.security.UserDetailsSerrviceImpl;
import com.dlb.service.UserServiceImpl;
import com.dlb.validation.SecurityValidations;

@Controller
public class UserController {
	/**
	 * this is controller classs
	 * 
	 */
	@Autowired
	private UserServiceImpl service;

	@Autowired
	private SecurityValidations validations;

	@Autowired
	UserDetailsSerrviceImpl userDetailsSerrviceImpl;

	@Autowired
	private JwtUtill jwtutil;

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	/***
	 * this method is to show start page having login and create button
	 * 
	 * @return
	 */

	@RequestMapping(value = "/home", method = RequestMethod.GET)
	public String showHome() throws Exception {
		logger.info(" Executing home page {}");
		return "HomePage";
	}

	/**
	 * this method is to show login page
	 * @return
	 */

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String showLogin(Model model) {
		logger.info(" displaying login page {}");
		return "Login";
	}

	/**
	 * this method is to show signup page
	 * @return
	 */

	@RequestMapping(value = "/signupNew", method = RequestMethod.GET)
	public String showSignUp(Model model) {
		logger.info(" displaying sign up page {}");
		UserDomain signUpdomain = new UserDomain();
		model.addAttribute("signUpdomain", signUpdomain);

		return "signupNew";
	}

	/**
	 * to recieve the user data from UI
	 * @param model
	 * @param domain
	 * @return
	 */
	@RequestMapping(value = "/signupPost", method = RequestMethod.POST)
	public String storeUserdata(Model model, @RequestParam("file") MultipartFile imageFile,
			@Valid @ModelAttribute(name = "signUpdomain") UserDomain domain, BindingResult result) {
		System.out.println("UserController.storeUserdata()");
		validations.validateSignUpUser(imageFile, domain);
		UserEntity userid = service.createUserAccount(domain, imageFile);
		if (userid != null) {
			model.addAttribute("userMessage", "Account created Successfuly, Login to continue");
		}
		model.addAttribute("obj", userid);

		return "Login";

	}

	/**
	 * to capture the login credentials and validate user login
	 * @param response
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/loginPostCredentials", method = { RequestMethod.GET, RequestMethod.POST })
	public String showHomeAppPage(Model model, HttpServletResponse response, HttpServletRequest request,
			@RequestParam(value = "email", required = true) String email,
			@RequestParam(value = "password", required = true) String pwd) {

		HttpSession oldsession = request.getSession(false); // getting existing session
		if (oldsession != null && oldsession.getAttribute("email") != null) {
			oldsession.invalidate();
		}
		String msg = "";
		msg = validations.validateLoginData(email, pwd);	
		if (!msg.isEmpty()) {
			model.addAttribute("msg", msg);
			return "Login";
		}
		UserEntity entity = service.checkPassword(email, pwd);// email,email);
		if (entity != null) {
			userDetailsSerrviceImpl.setUserNameAndPassword(email, pwd);
			UserDetails userDetails = userDetailsSerrviceImpl.loadUserByUsername(email);
			String jwtToken = jwtutil.generateToken(userDetails);
			String sessionValue = "Bearer " + jwtToken;
			HttpSession newsession = request.getSession(true);
			newsession.setAttribute("Bearer", sessionValue);
			newsession.setAttribute("email", email);
			model.addAttribute("domain", entity);
		}
		return "AppHomePage";
	}

	/**
	 * this method is used to show the user profile by user name by query parameter
	 * @param name
	 * @return
	 */

	@RequestMapping(value = "/showProfileByName")
	public String showProfile(@RequestParam String name) {
		logger.info("controller method executing to display user {}");
		String userName = name;
		UserEntity user = service.showUserProfile(userName);
		logger.info("controller method executed to display the  user successfully {}");
		return "AppHomePage";

	}

	/**
	 * this method is used to show forgotpassword page to insert email
	 * @return
	 */

	@RequestMapping(value = "/forgotpassword", method = RequestMethod.GET)

	public String forgotPasswordPage(Model model) {
		logger.info("Displaying Generating new password page {}");
		UserDomain forgotpsdomain = new UserDomain();
		model.addAttribute("forgotpsdomain", forgotpsdomain);
		return "forgotpassword";
	}

	/**
	 * this method is used to send otp to email and to show enter otp page
	 * @param model
	 * @param domain
	 * @return
	 */

	@RequestMapping(value = "/forgotpasswordpost", method = RequestMethod.POST)
	public String forgotPasswordPost(Model model, @ModelAttribute(value = "forgotpsdomain") UserDomain domain) {

		System.out.println("UserController.forgotPasswordPost()");
		int count = validations.validationForForgotPassword(domain.getEmail());
		if (count == 0) {
			model.addAttribute("msg", "No account found with this emil id");
			model.addAttribute("forgotpsdomain", domain);
			return "forgotpassword";
		}
		service.sendEmailtoUser(domain);
		model.addAttribute("msg", "password update link has been sent to your email id");
		return "forgotpassword";

	}

	/**
	 * to logout the user and to invalidate the session
	 * @param request
	 * @param response
	 * @param userName
	 * @return
	 */
	@RequestMapping("/logOut")
	public String userLogOut(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("email") String email) {
		logger.info(" started user logging out {}");

		HttpSession existingSession = request.getSession(false); // getting existing session if present
		if (existingSession == null) {
			throw new RuntimeException("Unautherized acess,Access Denied");
		}
		if (existingSession != null) {
			String sessionEmail = (String) existingSession.getAttribute("email");
			if (sessionEmail.equalsIgnoreCase(email)) {
				existingSession.invalidate(); // destroying the session
			} else {
				throw new RuntimeException("Unautherized acess,Access Denied");
			}

		}
		logger.info("User logged out successfuly");
		return "Login";
	}

	/**
	 * this method is to show password updation page
	 * @return
	 */
	@RequestMapping(value = "/PasswordUpdation", method = RequestMethod.GET)
	public String showUpdatePwd(Model model,@RequestParam("email")String email) {
		logger.info(" displaying password updation  page {}");
		UserDomain domain = new UserDomain();
		domain.setEmail(email);
		model.addAttribute("domain", domain);

		return "PasswordUpdation";
	}

	@RequestMapping(value = "/updatePwd", method = RequestMethod.POST)
	public String setUpdatedPassword(Model model, @ModelAttribute(value = "domain") UserDomain domain) {
		UserEntity saveUpdatedPassword = service.saveUpdatedPassword(domain);
		return "AppHomePage";

	}
}
