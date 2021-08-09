package com.ey.in.tds.onboarding.service.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ey.in.tds.common.dto.user.UserAccessDetailsDTO;
import com.ey.in.tds.common.dto.user.UserDTO;
import com.ey.in.tds.common.dto.user.UserPanTansDTO;
import com.ey.in.tds.common.dto.user.UserRoleDeductorDTO;
import com.ey.in.tds.common.dto.user.UserTanRolesDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.UserAccessDetailsJdbcDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.UserCassandraDTO;
import com.ey.in.tds.common.onboarding.response.dto.UserDetailsResponseDTO;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.jdbc.dao.UserAccessDetailDAO;
import com.ey.in.tds.jdbc.dao.UserDetailsDAO;

/**
 * service class contains user operation logic
 * 
 * @author scriptbees
 *
 */
@Service
public class UserServiceCassandra {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private UserDetailsDAO userDetailsDAO;

	@Autowired
	private UserAccessDetailDAO userAccessDetailDAO;

	/*
	 * public UserCassandra saveUserInCassandra(UserRoleDeductorDTO
	 * userCreationDeductorDTO, String userName) {
	 * 
	 * // user save if (userCreationDeductorDTO.getUserId() != null) {
	 * Optional<UserCassandra> userData = userCassandraRepository
	 * .findByUserId(userCreationDeductorDTO.getUserId()); if (userData.isPresent())
	 * { userData.get().setUserActiveflag(false);
	 * userCassandraRepository.save(userData.get()); if
	 * (!userCreationDeductorDTO.getUserAccessDetails().isEmpty()) { for
	 * (UserAccessDetailsDTO userAccessDetailsDTO :
	 * userCreationDeductorDTO.getUserAccessDetails()) { if
	 * (userAccessDetailsDTO.getPan() != null &&
	 * userAccessDetailsDTO.getUserTanLevelAccess() != null) { for (Map<String,
	 * UUID> map : userAccessDetailsDTO.getUserTanLevelAccess()) { for
	 * (Entry<String, UUID> entry : map.entrySet()) { UserAccessDetails.Key
	 * userAccessDetailsKey = new UserAccessDetails.Key(
	 * userCreationDeductorDTO.getEmail(), userAccessDetailsDTO.getPan(),
	 * entry.getKey()); Optional<UserAccessDetails> userAccessData =
	 * userAccessDetailsRepository .findByKey(userAccessDetailsKey); if
	 * (userAccessData.isPresent()) { userAccessData.get().setActive(false);
	 * userAccessDetailsRepository.save(userAccessData.get()); } } } } } } } } if
	 * (userCreationDeductorDTO.getUserId() == null &&
	 * userCreationDeductorDTO.getEmail() != null) { Optional<UserCassandra>
	 * userData = userCassandraRepository
	 * .findByUserEmail(userCreationDeductorDTO.getEmail()); if
	 * (!userData.isPresent()) { logger.error("User email should be unique" +
	 * userData.toString()); throw new
	 * CustomException("User email should be unique",
	 * HttpStatus.INTERNAL_SERVER_ERROR); } } UserCassandra user = new
	 * UserCassandra(); UserCassandra.Key userKey = new
	 * UserCassandra.Key(UUID.randomUUID()); user.setKey(userKey);
	 * user.getKey().setUserId(UUID.randomUUID()); user.setUserActiveflag(true);
	 * user.setUserCreateduser(userName); user.setUserModifieduser(userName);
	 * user.setUserCreateddate(new Timestamp(new Date().getTime()));
	 * user.setUserModifieddate(new Timestamp(new Date().getTime()));
	 * user.setUserEmail(userCreationDeductorDTO.getEmail());
	 * user.setUserUsername(userCreationDeductorDTO.getUsername());
	 * user.setForAllTans(userCreationDeductorDTO.isForAllTans());
	 * user.setDeductorPans(userCreationDeductorDTO.getDeductorPans());
	 * user.setConsent(false); if
	 * (!userCreationDeductorDTO.getUserAccessDetails().isEmpty()) { for
	 * (UserAccessDetailsDTO userAccessDetailsDTO :
	 * userCreationDeductorDTO.getUserAccessDetails()) { if
	 * (userAccessDetailsDTO.getPan() != null &&
	 * userAccessDetailsDTO.getUserTanLevelAccess() != null) { for (Map<String,
	 * UUID> map : userAccessDetailsDTO.getUserTanLevelAccess()) { for
	 * (Entry<String, UUID> entry : map.entrySet()) { UserAccessDetails
	 * userAccessDetails = new UserAccessDetails(); UserAccessDetails.Key
	 * userAccessDetailsKey = new UserAccessDetails.Key(
	 * userCreationDeductorDTO.getEmail(), userAccessDetailsDTO.getPan(),
	 * entry.getKey()); userAccessDetails.setKey(userAccessDetailsKey);
	 * userAccessDetails.setRole_id(entry.getValue());
	 * userAccessDetails.setActive(true); userAccessDetails.setCreateddate(new
	 * Date()); userAccessDetails.setCreateduser(userName);
	 * //userAccessDetailsRepository.save(userAccessDetails); } } } else { throw new
	 * CustomException(
	 * "Pan and User Tan Level Access not a null values, please fill those details",
	 * HttpStatus.INTERNAL_SERVER_ERROR); } } }
	 * 
	 * try { user = userCassandraRepository.save(user); } catch (Exception e) {
	 * logger.error("Error occured while saving record in user", e); throw new
	 * CustomException("Error occured while saving record in user",
	 * HttpStatus.INTERNAL_SERVER_ERROR); }
	 * 
	 * return user; }
	 */

	// TODO: Open Declarationcom.ey.in.tds.common.dto.user.UserAccessDetailsDTO TO
	// BE REMOVED

	/**
	 * method responsible for saving the user and user access details
	 * 
	 * @param userCreationDeductorDTO
	 * @param userName
	 * @return
	 */
	@Transactional
	public UserCassandraDTO saveUserInCassandra(UserRoleDeductorDTO userCreationDeductorDTO, String userName) {
		logger.info("service method execution started to save user {}");
		// user save
		if (userCreationDeductorDTO.getUserId() != null) {
			List<UserCassandraDTO> listUserCassendraDTO = userDetailsDAO.findById(userCreationDeductorDTO.getUserId()); // TODO

			// .findByUserId(userCreationDeductorDTO.getUserId());
			if (!listUserCassendraDTO.isEmpty()) {
				listUserCassendraDTO.get(0).setActiveflag(false);
				userDetailsDAO.update(listUserCassendraDTO.get(0), userName);
				// acessing user_acess_details record based on user id
				List<UserAccessDetailsJdbcDTO> listUserAccessDetails = userAccessDetailDAO
						.findById(listUserCassendraDTO.get(0).getUserId());
				if (!listUserAccessDetails.isEmpty()) {
					// iterating all useraccessdetails and updating as false
					for (UserAccessDetailsJdbcDTO dto : listUserAccessDetails) {
						dto.setActive(false);
						userAccessDetailDAO.update(dto);
						logger.info("user access details updated {}");
					}
				}
			}
		}
		if (userCreationDeductorDTO.getUserId() == null && userCreationDeductorDTO.getEmail() != null) {
			// checking for the record in db using email
			List<UserCassandraDTO> listUserCassandraDTO = userDetailsDAO
					.findByUserEmail(userCreationDeductorDTO.getEmail());
			if (!listUserCassandraDTO.isEmpty()) {
				logger.error("User email should be unique" + listUserCassandraDTO.get(0).toString());
				throw new CustomException("User email should be unique", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		UserCassandraDTO userCassandraDTO = new UserCassandraDTO();
		userCassandraDTO.setActiveflag(true);
		userCassandraDTO.setUsername(userCreationDeductorDTO.getUsername());
		userCassandraDTO.setModifieduser(userName);
		userCassandraDTO.setCreateddate(new Date());
		userCassandraDTO.setCreateduser(userCreationDeductorDTO.getUsername());
		userCassandraDTO.setModifieddate(new Date());
		userCassandraDTO.setEmail(userCreationDeductorDTO.getEmail());
		userCassandraDTO.setUserforAllTans(userCreationDeductorDTO.isForAllTans());
		userCassandraDTO.setDeductorPans(userCreationDeductorDTO.getDeductorPans().toString());
		userCassandraDTO.setConsent(false);
		userCassandraDTO.setConsentDate(new Date());
		try {
			// userAccessDetailDAO.insert(new UserAccessDetailsJdbcDTO());
			userCassandraDTO = userDetailsDAO.save(userCassandraDTO, userName);
			logger.info("userDetails record created {}");
		} catch (Exception e) {
			logger.error("Exception occured while saving data in user table  {}" + e);
		}
		if (!userCreationDeductorDTO.getUserAccessDetails().isEmpty() && userCassandraDTO.getUserId() != null) {
			for (UserAccessDetailsDTO userAccessDetailsDTO : userCreationDeductorDTO.getUserAccessDetails()) {
				if (userAccessDetailsDTO.getPan() != null && userAccessDetailsDTO.getUserTanLevelAccess() != null) {
					for (Map<String, Integer> map : userAccessDetailsDTO.getUserTanLevelAccess()) {
						for (Entry<String, Integer> entry : map.entrySet()) {
							logger.info("saving user access details data  {}");
							UserAccessDetailsJdbcDTO userAccessDetails = new UserAccessDetailsJdbcDTO();
							userAccessDetails.setUserId(userCassandraDTO.getUserId());
							userAccessDetails.setUserName(userCassandraDTO.getEmail());
							userAccessDetails.setRoleId(entry.getValue());
							userAccessDetails.setActive(true);
							userAccessDetails.setCreateddate(new Date());
							userAccessDetails.setCreateduser(userName);
							userAccessDetails.setPan(userAccessDetailsDTO.getPan());
							userAccessDetails.setTan(entry.getKey());
							userAccessDetailDAO.save(userAccessDetails);
							logger.info("user access details data inserted successfully {}");
						}
					}
				} else {
					throw new CustomException(
							"Pan and User Tan Level Access not a null values, please fill those details",
							HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}
		}

		return userCassandraDTO;
	}

	/*
	 * public PagedData<UserDTO> findAllUsers(Pagination pagination) { List<UserDTO>
	 * userDTOList = new ArrayList<>(); PagedData<UserCassandra> users =
	 * userCassandraRepository.findAllUsers(pagination); if (users != null &&
	 * !users.getData().isEmpty()) { Collections.sort(users.getData(), new
	 * Comparator<UserCassandra>() {
	 * 
	 * @Override public int compare(UserCassandra id1, UserCassandra id2) { return
	 * (id1.getUserCreateddate() == null || id2.getUserCreateddate() == null) ? 0 :
	 * id2.getUserCreateddate().compareTo(id1.getUserCreateddate()); } }); for
	 * (UserCassandra user : users.getData()) { UserDTO userDTO = new UserDTO();
	 * userDTO.setUserEmail(user.getUserEmail());
	 * userDTO.setUserUsername(user.getUserUsername());
	 * userDTO.setActive(user.getUserActiveflag());
	 * userDTO.setUserId(user.getKey().getUserId()); List<UserTanRolesDTO>
	 * userTanRolesList = new ArrayList<>();
	 * userDTO.setDeductorPans(user.getDeductorPans());
	 * userDTO.setUserTanRoles(userTanRolesList); userDTOList.add(userDTO); } return
	 * new PagedData<UserDTO>(userDTOList, users.getPageSize(),
	 * users.getPageStates()); } else { return new PagedData<UserDTO>(userDTOList,
	 * 0, Collections.emptyList()); } }
	 */
	/**
	 * retrievs all active user
	 * 
	 * @return
	 */
	public List<UserDTO> findAllUsers() {
		logger.info("service method execution started to retrieve all active user  {}");
		List<UserDTO> userDTOList = new ArrayList<>();
		// PagedData<UserCassandra> users =
		// userCassandraRepository.findAllUsers(pagination);
		List<UserCassandraDTO> users = userDetailsDAO.findAll();
		if (users != null && !users.isEmpty()) {

			for (UserCassandraDTO user : users) {
				UserDTO userDTO = new UserDTO();
				userDTO.setUserEmail(user.getEmail());
				userDTO.setUserUsername(user.getUsername());
				userDTO.setActive(user.getActiveflag());
				userDTO.setUserId(user.getUserId()); // TODO dynamic values to be assigned
				List<UserTanRolesDTO> userTanRolesList = new ArrayList<>();
				String pansStr = user.getDeductorPans().replace("[", "").replace("]", "");
				userDTO.setDeductorPans(Arrays.asList(pansStr.split(",")));
				userDTO.setUserTanRoles(userTanRolesList);
				userDTOList.add(userDTO);
			}
			logger.info("service method execution completed, all active user retrieved {}");
			return userDTOList;
		} else {
			return new ArrayList<UserDTO>();
		}
	}

	/**
	 * retrieve the user based on email
	 * 
	 * @param email
	 * @return
	 */
	public UserDTO getUserBasedOnEmail(String email) {
		logger.info("service method execution started to retrieve user details based on email {}");
		List<UserCassandraDTO> userData = userDetailsDAO.findActiveUsersByUserEmail(email);
		UserDTO userDTO = new UserDTO();
		if (!userData.isEmpty()) {
			String pansStr = userData.get(0).getDeductorPans().replace("[", "").replace("]", "");
			String[] pans = Arrays.stream(pansStr.split(",")).map(String::trim).toArray(String[]::new);

			userDTO.setUserId(userData.get(0).getUserId());
			userDTO.setUserEmail(userData.get(0).getEmail());
			userDTO.setUserUsername(userData.get(0).getUsername());
			userDTO.setDeductorPans(Arrays.asList(pans));
			userDTO.setConsentRequired(!(userData.get(0).getConsent()));
			// userDTO.setUserAccessDetails(
			// userAccessDetailsRepository.findByUserEmail(email,
			// Pagination.UNPAGED).getData());
			// TODO id need to be assigned
			userDTO.setUserAccessDetails(userAccessDetailDAO.findActiveUsersById(userData.get(0).getUserId()));
		}
		logger.info("service method execution completed,  user details based on email retrieved {}");
		return userDTO;
	}

	/**
	 * updates user details
	 * 
	 * @param consent
	 * @param userName
	 * @return
	 */
	public Boolean updateUserConsent(Boolean consent, String userName) {
		// Optional<UserCassandra> userData =
		// userCassandraRepository.findByUserEmail(userName);
		logger.info("method execution started to update User details   {}");
		List<UserCassandraDTO> listuserCassandraDTO = userDetailsDAO.findActiveUsersByUserEmail(userName);
		Boolean updated;
		if (!listuserCassandraDTO.isEmpty()) {
			UserCassandraDTO userCassandra = listuserCassandraDTO.get(0);
			userCassandra.setConsent(consent);
			userCassandra.setConsentDate(new Date());
			userCassandra.setModifieddate(new Date());
			userCassandra.setModifieduser(userName);
			try {
				// updated = userCassandraRepository.save(userCassandra) != null;
				userCassandra = userDetailsDAO.update(userCassandra, userName);
				updated = userCassandra.getUserId() != null ? true : false;
			} catch (Exception e) {
				logger.error("Error occured while saving record in user", e);
				throw new CustomException("Error occured while saving record in user",
						HttpStatus.INTERNAL_SERVER_ERROR);
			}

		} else {
			logger.error("Error occurred while fetching record in user for updating consent. User "
					+ listuserCassandraDTO.get(0).toString());
			throw new CustomException("Error occurred while fetching record in user for updating consent",
					HttpStatus.INTERNAL_SERVER_ERROR);

		}
		return updated;
	}

	public UserDetailsResponseDTO copyToResponse(UserCassandraDTO dto) {
		UserDetailsResponseDTO response = new UserDetailsResponseDTO();
		response.setUserId(dto.getUserId());
		response.setConsent(dto.getConsent());
		response.setConsentDate(dto.getConsentDate());
		String pansStr = dto.getDeductorPans().replace("[", "").replace("]", "");
		response.setDeductorPans(Arrays.asList(pansStr.split(",")));
		response.setForAllTans(dto.getUserforAllTans());
		// response.setUserAccessDetails(nu);
		response.setUserActiveflag(dto.getActiveflag());
		response.setUserCreateddate(dto.getCreateddate());
		response.setUserCreateduser(dto.getCreateduser());
		response.setUserEmail(dto.getEmail());
		response.setUserModifieddate(dto.getModifieddate());
		response.setUserModifieduser(dto.getModifieduser());
		response.setUserSourceType(dto.getSourceType());
		response.setUserUsername(dto.getUsername());

		return response;
	}

	
	/**
	 * returns data  for edit user functionality
	 * @param email
	 * @return
	 */
	public UserDTO getUserDetailsBasedOnEmail(Integer id) {
		logger.info("service method execution started to retrieve user details based on email {}");
		List<UserCassandraDTO> userData = userDetailsDAO.findById(id);
		UserDTO userDTO = new UserDTO();
		List<UserAccessDetailsJdbcDTO> finalList=new ArrayList<>();
		if (!userData.isEmpty()) {
			String pansStr = userData.get(0).getDeductorPans().replace("[", "").replace("]", "");
			String[] pans = Arrays.stream(pansStr.split(",")).map(String::trim).toArray(String[]::new);

			userDTO.setUserId(userData.get(0).getUserId());
			userDTO.setUserEmail(userData.get(0).getEmail());
			userDTO.setUserUsername(userData.get(0).getUsername());
			userDTO.setDeductorPans(Arrays.asList(pans));
			userDTO.setConsentRequired(!(userData.get(0).getConsent()));
			 List<UserAccessDetailsJdbcDTO> listUserAcessDetails=userAccessDetailDAO.findActiveUsersById(userData.get(0).getUserId());
			 for(UserAccessDetailsJdbcDTO dto:listUserAcessDetails) {
				 String moduleType=userDetailsDAO.getModuleTypeAndRoleID(dto.getRoleId());
				 logger.info("Retrieved module type for Role Id "+dto.getRoleId()+" is "+moduleType);
				 dto.setModuleType(moduleType);
				 finalList.add(dto);
			 }
			 userDTO.setUserAccessDetails(finalList);
		}
		logger.info("service method execution completed,  user details based on email retrieved {}");
		return userDTO;
	}

	public List<UserPanTansDTO> getUserPansAndTansBasedOnEmail(String email) {
		return userDetailsDAO.getUserPansAndTansBasedOnEmail(email);
	}
}
