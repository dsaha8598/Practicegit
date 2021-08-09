package com.ey.in.tds.onboarding.web.rest;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.owasp.esapi.errors.IntrusionException;
import org.owasp.esapi.errors.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.dto.user.UserDTO;
import com.ey.in.tds.common.dto.user.UserPanTansDTO;
import com.ey.in.tds.common.dto.user.UserRoleDeductorDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorTanAddress;
import com.ey.in.tds.common.onboarding.jdbc.dto.UserAccessDetailsJdbcDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.UserCassandraDTO;
import com.ey.in.tds.common.onboarding.response.dto.UserDetailsResponseDTO;
import com.ey.in.tds.common.security.SecurityValidations;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.jdbc.dao.DeductorTanAddressDAO;
import com.ey.in.tds.jdbc.dao.UserAccessDetailDAO;
import com.ey.in.tds.jdbc.dao.UserDetailsDAO;
import com.ey.in.tds.onboarding.service.user.UserServiceCassandra;

@RestController
@RequestMapping("/api/onboarding")
public class UserControllerCassandra {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private UserServiceCassandra userService;

	@Autowired
	private UserDetailsDAO userDao;

	@Autowired
	private UserAccessDetailDAO userAccessDetailDAO;

	@Autowired
	private DeductorTanAddressDAO deductorTanAddressDAO;

	/***
	 * 
	 * @param userCreateRoleDeductorDTO
	 * @return
	 * @throws ParseException
	 * @throws ValidationException
	 * @throws IntrusionException
	 */
	@PostMapping(value = "/createuser", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<UserDetailsResponseDTO>> createUserDedutor(
			@RequestBody UserRoleDeductorDTO userCreateRoleDeductorDTO, @RequestHeader("USER_NAME") String userName)
			throws IntrusionException, ValidationException, ParseException {
		logger.info("user creation api executing{}");
		if (userCreateRoleDeductorDTO.getEmail() == null) {
			throw new CustomException("Email cannot be Null", HttpStatus.BAD_REQUEST);
		}

		if (userCreateRoleDeductorDTO.getUsername() == null) {
			throw new CustomException("User name cannot be Null", HttpStatus.BAD_REQUEST);
		}
		if (userCreateRoleDeductorDTO.getDeductorPans().isEmpty()) {
			throw new CustomException("Pan  cannot be Null", HttpStatus.BAD_REQUEST);
		}

		// ESAPI Validating user input
		SecurityValidations.userInputValidation(userCreateRoleDeductorDTO);
		UserCassandraDTO dto = userService.saveUserInCassandra(userCreateRoleDeductorDTO, userName);
		UserDetailsResponseDTO response = userService.copyToResponse(dto);
		ApiStatus<UserDetailsResponseDTO> apiStatus = new ApiStatus<UserDetailsResponseDTO>(HttpStatus.OK,
				"Request to create a User Record", "User Record Created", response);
		logger.info("user creation api executed successfully {}");
		return new ResponseEntity<ApiStatus<UserDetailsResponseDTO>>(apiStatus, HttpStatus.OK);
	}

	/***
	 * This api created for getting user data based on user id
	 * 
	 * @return
	 */

	@GetMapping("/user/id/{userId}")
	public ResponseEntity<ApiStatus<UserDetailsResponseDTO>> getUserBasedOnId(@PathVariable Integer userId) {

		UserDetailsResponseDTO user = new UserDetailsResponseDTO();
		List<UserCassandraDTO> userInfo = userDao.findById(userId);
		if (!userInfo.isEmpty()) {
			user = userService.copyToResponse(userInfo.get(0));
		}
		ApiStatus<UserDetailsResponseDTO> apiStatus = new ApiStatus<UserDetailsResponseDTO>(HttpStatus.OK,
				"Request to get a user record based on user id", "User record", user);
		return new ResponseEntity<ApiStatus<UserDetailsResponseDTO>>(apiStatus, HttpStatus.OK);
	}

	/***
	 * This api created for getting all user data
	 * 
	 * @return
	 */
	@GetMapping("/users")
	public ResponseEntity<ApiStatus<List<UserDTO>>> getAllUsers() {
		List<UserDTO> users = userService.findAllUsers();
		ApiStatus<List<UserDTO>> apiStatus = new ApiStatus<List<UserDTO>>(HttpStatus.OK,
				"Request to get a list of user record ", "List of user records", users);
		return new ResponseEntity<ApiStatus<List<UserDTO>>>(apiStatus, HttpStatus.OK);
	}

	/***
	 * This api created for getting user role and permissions data based on email
	 * 
	 * @param email
	 * @return
	 */
	// Feign Client
	@GetMapping("/users/{email}")
	public ResponseEntity<ApiStatus<UserDTO>> getUserBasedOnEmail(@PathVariable String email) {
		logger.debug("<-- getUserBasedOnEmail");
		UserDTO userInfo = userService.getUserBasedOnEmail(email);
		ApiStatus<UserDTO> apiStatus = new ApiStatus<UserDTO>(HttpStatus.OK,
				"Request to get a user information based on email", userInfo);
		return new ResponseEntity<ApiStatus<UserDTO>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param userRoleDeductorDTO
	 * @param userName
	 * @return
	 */
	@PutMapping(value = "/userConsent", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<Boolean>> updateUserConsent(@RequestBody UserRoleDeductorDTO userRoleDeductorDTO,
			@RequestHeader("X-USER-EMAIL") String userName) {
		logger.debug("<-- updateUserConsent");
		Boolean updated = userService.updateUserConsent(userRoleDeductorDTO.isConsent(), userName);
		ApiStatus<Boolean> apiStatus = new ApiStatus<Boolean>(HttpStatus.OK,
				"Request to update consent of a user based on email", "NO ALERT", updated);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param email
	 * @param pan
	 * @param userName
	 * @return
	 */
	@GetMapping("userAccessDetails/tans/{email}/{pan}")
	public ResponseEntity<ApiStatus<UserAccessDetailsJdbcDTO>> getUseracessDetails(@PathVariable String email,
			@PathVariable String pan, @RequestHeader("X-USER-EMAIL") String userName) {
		logger.debug("fetching tans from user acess details");
		UserAccessDetailsJdbcDTO userAcessDetails = new UserAccessDetailsJdbcDTO();
		List<UserAccessDetailsJdbcDTO> list = userAccessDetailDAO.findByPanAndEmail(email, pan);
		if (!list.isEmpty()) {
			userAcessDetails = list.get(0);
		}

		ApiStatus<UserAccessDetailsJdbcDTO> apiStatus = new ApiStatus<UserAccessDetailsJdbcDTO>(HttpStatus.OK,
				"Request to get  a useracess details based on email", userAcessDetails);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param email
	 * @param pan
	 * @return
	 * @throws ParseException
	 */
	@PostMapping("/usertansbyuseremail")
	public ResponseEntity<ApiStatus<Map<String, Map<String, Integer>>>> getUserTansByUserEmail(
			@RequestParam("pan") String pan, @RequestParam("email") String email,
			@RequestHeader("X-USER-EMAIL") String userName, @RequestParam("moduleScope") String moduleScope)
			throws ParseException {

		Map<String, Integer> tansAndRoleIds = new HashMap<>();
		Map<String, Integer> tanAndstates = new HashMap<>();
		Map<String, Map<String, Integer>> tansAndRoles = new HashMap<>();

		String tenantName = userName.substring(userName.lastIndexOf("@") + 1, userName.length());
		MultiTenantContext.setTenantId(tenantName);

		List<UserAccessDetailsJdbcDTO> userAccessDetailsList = userAccessDetailDAO
				.findUserAcessByModuleTypePanAndUserEmail(email, pan, moduleScope);

		for (UserAccessDetailsJdbcDTO userAccessDetails : userAccessDetailsList) {
			tansAndRoleIds.put(userAccessDetails.getTan(), userAccessDetails.getRoleId());
			List<DeductorTanAddress> deductorTanAddress = deductorTanAddressDAO
					.findPanNameByTan(userAccessDetails.getTan());
			if (deductorTanAddress != null) {
				tanAndstates.put(userAccessDetails.getTan() + " - " + deductorTanAddress.get(0).getStateName(),
						userAccessDetails.getRoleId());
			}
			tansAndRoles.put("tanAndstates", tanAndstates);
			tansAndRoles.put("tansAndRoleIds", tansAndRoleIds);

		}
		ApiStatus<Map<String, Map<String, Integer>>> apiStatus = new ApiStatus<Map<String, Map<String, Integer>>>(
				HttpStatus.OK, "Request to get a user tans and role information based on deductor pan", "NO ALERT",
				tansAndRoles);
		return new ResponseEntity<ApiStatus<Map<String, Map<String, Integer>>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * to get user and user acess details for edit call
	 * 
	 * @param userId
	 * @return
	 */
	@GetMapping("/user/by/{id}")
	public ResponseEntity<ApiStatus<UserDTO>> getUserDetailsBasedOnEmail(@PathVariable Integer id) {
		logger.debug("<-- getUserBasedOnEmail");
		UserDTO userInfo = userService.getUserDetailsBasedOnEmail(id);
		ApiStatus<UserDTO> apiStatus = new ApiStatus<UserDTO>(HttpStatus.OK,
				"Request to get a user information based on User Id", userInfo);
		return new ResponseEntity<ApiStatus<UserDTO>>(apiStatus, HttpStatus.OK);
	}
	
	@GetMapping("/userpanandtans/by/{email}")
	public ResponseEntity<ApiStatus<List<UserPanTansDTO>>> getUserPansAndTansBasedOnEmail(@RequestHeader(value = "X-TENANT-ID") String tenantId, @PathVariable String email) {
		List<UserPanTansDTO> userInfo = userService.getUserPansAndTansBasedOnEmail(email);
		ApiStatus<List<UserPanTansDTO>> apiStatus = new ApiStatus<>(HttpStatus.OK,
				"Request to get a user pan and tan  based on user email.", userInfo);
		return new ResponseEntity<ApiStatus<List<UserPanTansDTO>>>(apiStatus, HttpStatus.OK);
	}
}
