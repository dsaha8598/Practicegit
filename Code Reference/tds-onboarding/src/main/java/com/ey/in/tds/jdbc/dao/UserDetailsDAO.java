package com.ey.in.tds.jdbc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.dto.user.UserPanTansDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.UserCassandraDTO;
import com.ey.in.tds.jdbc.rowmapper.UserDetailsRowMapper;
/**
 * DAO contains logic for data base operation for user
 * @author scriptbees
 *
 */
@Repository
public class UserDetailsDAO {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private HashMap<String, String> queries;
	
	@Autowired
	private DataSource dataSource;
	private JdbcTemplate jdbcTemplate;
    private SimpleJdbcInsert jdbcInsert;
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    
	
	@PostConstruct
	  private void postConstruct() {
		namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	      jdbcTemplate = new JdbcTemplate(dataSource);
	}

	/**
	 * method gives the data source object TODO: hard coded values will be removed
	 * 
	 * @return
	 */
	public DriverManagerDataSource getDataSource() {
		logger.info("preparing datasource object {}");
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		// dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		dataSource.setUrl("jdbc:sqlserver://tdsdevelop.com:1433;databaseName=tds_client2");
		dataSource.setUsername("sa");
		dataSource.setPassword("yourStrong(!)Password");
		logger.info("datasource object prepared successfully {}");
		return dataSource;
	}

	/**
	 * retrieves user details from user table based on id
	 * @param userId
	 * @return
	 */
	public List<UserCassandraDTO> findById(Integer userId) {
		logger.info("retrieving user based on id {}");
		List<UserCassandraDTO> listDao = jdbcTemplate.query(String.format(queries.get("user_by_id")),
				new UserDetailsRowMapper(), userId);
		logger.info("user retrieved successfully {}");
		return listDao;
	}

	/**
	 * maaps values with the table columns
	 * @param dto
	 * @return
	 */
	public Map<String, Object> mapColumnValues(UserCassandraDTO dto) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("user_id", dto.getUserId()); // dao.getUserId()
		parameters.put("activeflag", dto.getActiveflag() == true ? 1 : 0);
		parameters.put("consent", dto.getConsent());
		parameters.put("consentdate", dto.getConsentDate());
		parameters.put("createddate", dto.getCreateddate());
		parameters.put("createduser", dto.getCreateduser());
		parameters.put("deductor_pans", dto.getDeductorPans());
		parameters.put("email", dto.getEmail());
		parameters.put("user_for_all_tans", dto.getUserforAllTans());
		parameters.put("modifieddate", dto.getModifieddate());
		parameters.put("modifieduser", dto.getModifieduser());
		parameters.put("sourcetype", dto.getSourceType());
		parameters.put("username", dto.getUsername());

		return parameters;
	}

	/**
	 * responsible to save the user details in user table
	 * @param dto
	 * @param userName
	 * @return
	 */
	public UserCassandraDTO save(UserCassandraDTO dto,String userName) {
		logger.info("DAO method executing to save user data");
		KeyHolder holder = new GeneratedKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {

			@Override
			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				PreparedStatement ps = connection.prepareStatement(String.format(queries.get("insert_user")),
						Statement.RETURN_GENERATED_KEYS);
				
				ps.setInt(1, dto.getActiveflag()==true?1:0);  //activeflag
				ps.setInt(2,dto.getConsent()==true?1:0);      //consent
				ps.setDate(3, new java.sql.Date(dto.getConsentDate().getTime()));  //consentdate
				ps.setDate(4, new java.sql.Date(dto.getCreateddate().getTime()));  //createddate
				ps.setString(5,userName);    //createduser
				ps.setString(6,dto.getDeductorPans().toString().replace("[", "").replace("]", ""));   //deductor_pans
				ps.setString(7, dto.getEmail());     //email
				ps.setInt(8, dto.getUserforAllTans()==true?1:0);    //user_for_all_tans
				ps.setDate(9, new java.sql.Date(dto.getModifieddate().getTime()));   //modifieddate
				ps.setString(10, userName);   //modifieduser
				ps.setString(11, dto.getSourceType());   //sourcetype
				ps.setString(12, dto.getUsername());   //username
				return ps;
			}
		}, holder);
		dto.setUserId(holder.getKey().intValue());
		logger.info("DAO method execution successful {}");
		return dto;
	}
	public UserCassandraDTO update(UserCassandraDTO dto,String userName) {
		logger.info("DAO method executing to save user data");
		jdbcTemplate.update(new PreparedStatementCreator() {

			@Override
			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				PreparedStatement ps = connection.prepareStatement(String.format(queries.get("update_user")),
						Statement.RETURN_GENERATED_KEYS);
				
				ps.setInt(1, dto.getActiveflag()==true?1:0);  //activeflag
				ps.setInt(2,dto.getConsent()==true?1:0);      //consent
				ps.setDate(3, new java.sql.Date(dto.getConsentDate().getTime()));  //consentdate
				ps.setDate(4, new java.sql.Date(dto.getCreateddate().getTime()));  //createddate
				ps.setString(5,userName);    //createduser
				ps.setString(6,dto.getDeductorPans().toString());   //deductor_pans
				ps.setString(7, dto.getEmail());     //email
				ps.setInt(8, dto.getUserforAllTans()==true?1:0);    //user_for_all_tans
				ps.setDate(9, new java.sql.Date(dto.getModifieddate().getTime()));   //modifieddate
				ps.setString(10, userName);   //modifieduser
				ps.setString(11, dto.getSourceType());   //sourcetype
				ps.setString(12, dto.getUsername());   //username
				ps.setInt(13, dto.getUserId());
				return ps;
			}
		});
		logger.info("DAO method execution successful {}");
		return dto;
	}

	/**
	 * retrieves the user based on email
	 * @param email
	 * @return
	 */
	public List<UserCassandraDTO> findByUserEmail(String email) {
		logger.info("retrieving user based on email id {}");
		List<UserCassandraDTO> listDao = jdbcTemplate.query(String.format(queries.get("user_by_email")),
				new UserDetailsRowMapper(), email); 
		logger.info("user retrieved based on email successfully {}");
		return listDao;
	}
	/**
	 * to find the active users based on email
	 * @param email
	 * @return
	 */
	public List<UserCassandraDTO> findActiveUsersByUserEmail(String email) {
		logger.info("retrieving active user based on email id {}");
		List<UserCassandraDTO> listDao = jdbcTemplate.query(String.format(queries.get("user_active_by_email")),
				new UserDetailsRowMapper(), email);
		logger.info(" active user based on email id retrieved successfully{}");
		return listDao;
	}
	/**
	 *To Retrieve all active users  
	 * @return
	 */
	public List<UserCassandraDTO> findAll(){
		List<UserCassandraDTO> listDto = jdbcTemplate.query(
				String.format(queries.get("user_all_active")),
				new UserDetailsRowMapper()); 
        logger.info("All active users retrieved  {}");
    	return listDto;
	}
	
	public String getModuleTypeAndRoleID(Integer id){
		logger.info("Getting tan,role id and module type for user id {}"+id);
		Map<String,Object> param=new HashMap<>();
		param.put("id", id);
		return namedParameterJdbcTemplate.queryForObject(String.format(queries.get("get_moduleYtpe_by_roleID")), param, String.class);
	}

	public List<UserPanTansDTO> getUserPansAndTansBasedOnEmail(String email) {
		Map<String, Object> param = new HashMap<>();
		param.put("email", email);
		String query = "select distinct user_pan as pan, user_tan as tan from Onboarding.user_access_details"
				+ " where USER_NAME = :email and active = 1 order by user_pan";
		return namedParameterJdbcTemplate.query(query, param,
				new BeanPropertyRowMapper<UserPanTansDTO>(UserPanTansDTO.class));
	}
}
