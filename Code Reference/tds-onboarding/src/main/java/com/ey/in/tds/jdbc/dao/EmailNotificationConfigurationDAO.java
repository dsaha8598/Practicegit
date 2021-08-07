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

import com.ey.in.tds.common.onboarding.jdbc.dto.EmailNotificationConfiguration;
import com.ey.in.tds.jdbc.rowmapper.NotificationRowMapper;

/**
 * Repository class for Data base operations with email_notification_config
 * table
 * 
 * @author Dipak
 *
 */
@Repository
public class EmailNotificationConfigurationDAO {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired(required = false)
	private HashMap<String, String> queries;

	private JdbcTemplate jdbcTemplate;

	private SimpleJdbcInsert simpleJdbcInsert;

	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	private DataSource dataSource;

	@PostConstruct
	private void postConstruct() {
		jdbcTemplate = new JdbcTemplate(dataSource);
		namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("email_notification_config")
				.withSchemaName("Transactions").usingGeneratedKeyColumns("id");

	}

	/**
	 * 
	 * @param dto
	 * @return
	 */
	public EmailNotificationConfiguration save(EmailNotificationConfiguration dto) {
		logger.info("insert method execution started  {}");

		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(dto);
		dto.setId(simpleJdbcInsert.executeAndReturnKey(namedParameters).intValue());

		logger.info("Record inserted to email_notification_config table {}");
		return dto;
	}

	/**
	 * getting email notification configuration based on id
	 * 
	 * @param id
	 * @return
	 */
	public List<EmailNotificationConfiguration> findById(Integer id) {
		logger.info("Retrieving email notification configuration by id {}", id);
		Map<String, Object> param = new HashMap<>();
		param.put("id", id);
		return namedParameterJdbcTemplate.query(queries.get("get_email_confiuration_by_id"), param,
				new NotificationRowMapper());
	}

	/**
	 * 
	 * @param dto
	 * @return
	 */
	public EmailNotificationConfiguration update(EmailNotificationConfiguration dto) {
		logger.info("DAO method executing to save user data ");

		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(dto);
		int status = namedParameterJdbcTemplate.update(String.format(queries.get("update_email_confiuration_by_id")),
				namedParameters);

		if (status != 0) {
			logger.info("AoMaster data is updated for ID " + dto.getId());
		} else {
			logger.info("No record found with ID " + dto.getId());
		}
		logger.info("DAO method execution successful {}");
		return dto;
	}
	
	/**
	 * This method for get all email notification
	 * @return
	 */
	public List<EmailNotificationConfiguration> findAll() {
		logger.info("Retrieving All email notification configuration {}");
		return namedParameterJdbcTemplate.query(queries.get("get_All_email_confiuration"), new NotificationRowMapper());
	}
	
	/**
	 * This method for get all email notification based on name
	 * @param name
	 * @param tenantId
	 * @return
	 */
	public List<EmailNotificationConfiguration> findByType(String type, String tenantId) {
		Map<String, Object> param = new HashMap<>();
		param.put("type", type);
		return namedParameterJdbcTemplate.query(queries.get("get_email_confiuration_by_type"), param,
				new NotificationRowMapper());
	}
}
