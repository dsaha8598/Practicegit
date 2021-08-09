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

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.onboarding.jdbc.dto.KYCDeclarationEmailDetails;
import com.ey.in.tds.jdbc.rowmapper.KYCDeclarationEmailDetailsRowMapper;

/**
 * 
 * @author vamsir
 *
 */
@Repository
public class KYCDeclarationEmailDetailsDAO {

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
		jdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("kyc_declaration_email_details")
				.withSchemaName("Onboarding").usingGeneratedKeyColumns("id");
	}

	/**
	 * This method for save KYC Declaration Email Details
	 *
	 * @param kycDetails
	 */
	public KYCDeclarationEmailDetails save(KYCDeclarationEmailDetails kycDetails) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(kycDetails);
		kycDetails.setId(jdbcInsert.executeAndReturnKey(namedParameters).intValue());
		logger.info("Record inserted to KYC Declaration Email Details table {}");
		return kycDetails;
	}

	/**
	 * 
	 * @param perferences
	 * @return
	 */
	public int update(KYCDeclarationEmailDetails perferences) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(perferences);
		int status = namedParameterJdbcTemplate.update(String.format(queries.get("update_kyc_declaration_email")),
				namedParameters);
		return status;
	}

	/**
	 * 
	 * @param deductorTan
	 * @return
	 */
	public List<KYCDeclarationEmailDetails> getAllPerferences(String deductorTan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorTan", deductorTan);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_all_kyc_declaration")), parameters,
				new KYCDeclarationEmailDetailsRowMapper());
	}
	
	/**
	 * 
	 * @param perferences
	 * @return
	 */
	public int updateApproved(KYCDeclarationEmailDetails perferences) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(perferences);
		int status = namedParameterJdbcTemplate.update(String.format(queries.get("update_kyc_approved")),
				namedParameters);
		return status;
	}

	/**
	 * 
	 * @param deductorTan
	 * @param type
	 * @return
	 */
	public List<KYCDeclarationEmailDetails> getAllPerferences(String deductorTan, String type) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorTan", deductorTan);
		parameters.put("type", type);
		return namedParameterJdbcTemplate.query(String.format(queries.get("kyc_approved_check")), parameters,
				new KYCDeclarationEmailDetailsRowMapper());
	}

}
