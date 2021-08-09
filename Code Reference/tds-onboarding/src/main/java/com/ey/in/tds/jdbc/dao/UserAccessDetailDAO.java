package com.ey.in.tds.jdbc.dao;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.onboarding.jdbc.dto.AoMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.UserAccessDetailsJdbcDTO;
import com.ey.in.tds.jdbc.rowmapper.UserAccessDetailsRowMapper;

/**
 * DAO class to perform db operations with user_acess_details
 * @author scriptbees
 *
 */
@Repository
public class UserAccessDetailDAO {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private HashMap<String, String> queries;
	@Autowired
	private DataSource dataSource;
	private JdbcTemplate jdbcTemplate;
    private SimpleJdbcInsert jdbcInsert;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	
	@PostConstruct
	  private void postConstruct() {
	      jdbcTemplate = new JdbcTemplate(dataSource);
	      namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	      jdbcInsert = new SimpleJdbcInsert(dataSource)
	              .withTableName("user_access_details")
					.withSchemaName("Onboarding").usingGeneratedKeyColumns("user_access_details_id");
	}

	/**
	 * mapps values with respective data base table columns
	 * @param dto
	 * @return
	 */
	public  Map<String, Object> mapColumnValues(UserAccessDetailsJdbcDTO dto){
		logger.info("Mapping the values to respective columns   {}");
		Map<String, Object> parameters=new HashMap<String, Object>();
		parameters.put("user_access_details_id",dto.getUserAccessDetailsId());  // dao.getUserId()
		parameters.put("user_id",dto.getUserId());
		parameters.put("user_name",dto.getUserName());
		parameters.put("user_pan",dto.getPan());
		parameters.put("user_tan",dto.getTan());
		parameters.put("role_id",dto.getRoleId());  //TODO DYNAMIC VALUE TO BE ASSIGNED
		parameters.put("active",dto.getActive());
		parameters.put("created_date",dto.getCreateddate());
		parameters.put("created_user",dto.getCreateduser());
		logger.info("mapped values to the columns  {}");
		return parameters;
	}
	/**
	 * to insert user acess details data
	 * @param dto
	 * @return
	 */
	public UserAccessDetailsJdbcDTO save(UserAccessDetailsJdbcDTO dto) {
		logger.info("insert method execution started to insert user acess details  {}");
		
		Map<String, Object> parameters=mapColumnValues(dto);
		
		int userId=jdbcInsert.executeAndReturnKey(parameters).intValue();
		dto.setUserId(userId);
		logger.info("insert method execution completed user acess details  data inserted{}");
		return dto;
	}
	/**
	 * retrieves data bassed on id
	 * @param userId
	 * @return
	 */
	public List<UserAccessDetailsJdbcDTO> findById(Integer userId){
		logger.info("method execution started to retrieve user acess details based on id {}");
    	List<UserAccessDetailsJdbcDTO> listDao = jdbcTemplate.query(
				String.format(queries.get("userAccessDetails_by_id")),
				new UserAccessDetailsRowMapper(),userId); 
    	logger.info("method execution completed , user acess details retrieved based on id {}");
    	return listDao;
	}
	/**
	 * retrieves active data based on id
	 * @param userId
	 * @return
	 */
	public List<UserAccessDetailsJdbcDTO> findActiveUsersById(Integer userId){
		logger.info("method execution started to retrieve user acess details based on id {}");
    	List<UserAccessDetailsJdbcDTO> listDao = jdbcTemplate.query(
				String.format(queries.get("userAccessDetails_active_by_id")),
				new UserAccessDetailsRowMapper(),userId);
    	logger.info("method execution completed, user acess details retrieved sucessfully based on id {}");
    	return listDao;
	}
	
	public List<UserAccessDetailsJdbcDTO> findByPanAndEmail(String usremail,String pan){
		logger.info("method execution started to retrieve user acess details based on id {}");
    	List<UserAccessDetailsJdbcDTO> listDao = jdbcTemplate.query(
				String.format(queries.get("userAccessDetails_by_emai_pan")),
				new UserAccessDetailsRowMapper(),usremail,pan); 
    	logger.info("method execution completed , user acess details retrieved based on id {}");
    	return listDao;
	}
	
	public List<UserAccessDetailsJdbcDTO> findUserAcessByModuleTypePanAndUserEmail(String usremail,String pan,String moduleType){
		logger.info("method execution started to retrieve user acess details based on id {}");
    	List<UserAccessDetailsJdbcDTO> listDao = jdbcTemplate.query(
				String.format(queries.get("findUserAcess_By_ModuleType_Pan_And_UserEmail")),
				new UserAccessDetailsRowMapper(),usremail,pan,moduleType); 
    	logger.info("method execution completed , user acess details retrieved based on id {}");
    	return listDao;
	}
	
	public UserAccessDetailsJdbcDTO update(UserAccessDetailsJdbcDTO dto) {

		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(dto);
		int status = namedParameterJdbcTemplate.update(String.format(queries.get("update_userAccessDetails")), namedParameters);

		
		
		if (status != 0) {
			logger.info("UserAcessDetails data is updated for ID " + dto.getUserAccessDetailsId());
		} else {
			logger.info("No record found with ID " + dto.getUserAccessDetailsId());
		}
		logger.info("DAO method execution successful {}");
		return dto;
	}}
