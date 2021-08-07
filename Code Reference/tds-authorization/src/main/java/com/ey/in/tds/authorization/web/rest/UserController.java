package com.ey.in.tds.authorization.web.rest;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ey.in.tds.common.admin.config.JwtTokenValidator;
import com.ey.in.tds.common.admin.domain.User;
import com.ey.in.tds.common.admin.service.UserService;
import com.ey.in.tds.core.dto.UserInfoDTO;
import com.ey.in.tds.core.dto.UserRoleDeductorCreationDTO;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.feign.client.OnboardingClient;

import io.micrometer.core.annotation.Timed;

@RestController
@RequestMapping("/api/authorization")
public class UserController {
	
	public static final String AUTHORIZATION_HEADER = "Authorization";

	@Autowired
	private UserService userService;
	
	@Autowired
	private JwtTokenValidator jwtTokenValidator;
	
	@Autowired
	private OnboardingClient onboardingClient;

	@PostMapping("/user")
	@Timed
	public ResponseEntity<User> createUser(@Valid @RequestBody User user,
			@RequestHeader("X-USER-EMAIL") String userName) {
		User userResponse = userService.save(user, userName);
		return new ResponseEntity<>(userResponse, HttpStatus.OK);
	}

	@PutMapping("/user")
	@Timed
	public ResponseEntity<User> updateUser(@Valid @RequestBody User user,
			@RequestHeader("X-USER-EMAIL") String userName) {
		User roleResponse = userService.save(user, userName);
		return new ResponseEntity<>(roleResponse, HttpStatus.OK);
	}

	@GetMapping("/user")
	@Timed
	public ResponseEntity<List<User>> getAllUsers() {
		List<User> listOfUser = userService.findAll();
		return new ResponseEntity<>(listOfUser, HttpStatus.OK);
	}

	@GetMapping("/user/{id}")
	@Timed
	public ResponseEntity<?> getUser(@PathVariable Long id) {
		User user = userService.findOne(id);
		return new ResponseEntity<>(user, HttpStatus.OK);
	}

	@PostMapping("/createuser")
	@Timed
	public ResponseEntity<ApiStatus<User>> createUserDedutor(
			@Valid @RequestBody UserRoleDeductorCreationDTO userCreateRoleDeductorDTO,
			@RequestHeader("X-USER-EMAIL") String userName) {

		if (userCreateRoleDeductorDTO.getDeductorId() == null) {
			throw new CustomException("Deductor Id cannot be Null", HttpStatus.BAD_REQUEST);
		}

		if (userCreateRoleDeductorDTO.getEmail() == null) {
			throw new CustomException("Email cannot be Null", HttpStatus.BAD_REQUEST);
		}

		if (userCreateRoleDeductorDTO.getUsername() == null) {
			throw new CustomException("User name cannot be Null", HttpStatus.BAD_REQUEST);
		}
		User userResponse = userService.saveUser(userCreateRoleDeductorDTO, userName);
		ApiStatus<User> apiStatus = new ApiStatus<User>(HttpStatus.OK, "Request to create a User Record",
				"User Record Created", userResponse);
		return new ResponseEntity<ApiStatus<User>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping("/users/{deductorId}")
	@Timed
	public ResponseEntity<ApiStatus<List<UserInfoDTO>>> getUserBasedOnDeductorId(@PathVariable Long deductorId) {
		List<UserInfoDTO> userInfo = userService.findUserBasedOnDeductorId(deductorId);
		ApiStatus<List<UserInfoDTO>> apiStatus = new ApiStatus<List<UserInfoDTO>>(HttpStatus.OK,
				"Request to get a list of User and Role Info", "List OF Users and Role", userInfo);
		return new ResponseEntity<ApiStatus<List<UserInfoDTO>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param email
	 * @param pan
	 * @return
	 * @throws ParseException 
	 */
	@PostMapping("/getusertansbyuseremail")
	public ResponseEntity<ApiStatus<Map<String, Map<String, Integer>>>> getUserTansByUserEmail(
			@RequestHeader(value = AUTHORIZATION_HEADER) String authorizationHeader,
			@RequestBody Map<String, String> requestParams) throws ParseException {//@RequestBody List<Map<String, String>> requestParams
		String pan = requestParams.get("pan");
		String moduleScope=requestParams.get("moduleType");
		
		String idToken = authorizationHeader.replaceFirst("Bearer ", StringUtils.EMPTY);

		String uniqueUserName = jwtTokenValidator.getUsername(idToken);
		
		ResponseEntity<ApiStatus<Map<String, Map<String, Integer>>>> response = onboardingClient
				.getUserTansByUserEmail(pan, uniqueUserName, uniqueUserName,moduleScope);
		Map<String, Map<String, Integer>> tans = response.getBody().getData();
		ApiStatus<Map<String, Map<String, Integer>>> apiStatus = new ApiStatus<Map<String, Map<String, Integer>>>(HttpStatus.OK,
				"Request to get a user tans and role information based on deductor pan","NO ALERT", tans);
		return new ResponseEntity<ApiStatus<Map<String, Map<String, Integer>>>>(apiStatus, HttpStatus.OK);
	}

}
