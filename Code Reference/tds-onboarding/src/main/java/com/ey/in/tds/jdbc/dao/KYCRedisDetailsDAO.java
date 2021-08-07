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
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.onboarding.jdbc.dto.KYCRedisDetails;
import com.ey.in.tds.jdbc.rowmapper.KYCRedisDetailsRowMapper;

/**
 * 
 * @author vamsir
 *
 */
@Repository
public class KYCRedisDetailsDAO {
	


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
		jdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("kyc_redis_details").withSchemaName("Onboarding")
				.usingGeneratedKeyColumns("id");
	}

	/**
	 * This method for save kyc redis deatails
	 *
	 * @param kycDetails
	 */
	public KYCRedisDetails save(KYCRedisDetails kycRedisDetails) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(kycRedisDetails);
		kycRedisDetails.setId(jdbcInsert.executeAndReturnKey(namedParameters).intValue());
		logger.info("Record inserted to kyc redis details table {}");
		return kycRedisDetails;

	}

	/**
	 * 
	 * @param deductorPan
	 * @param deductorTan
	 * @param valueOf
	 * @return
	 */
	public List<KYCRedisDetails> getKycEmailRediesKeys(Integer kydId) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("kydId", kydId);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_kycemail_by_kycid")), parameters,
				new KYCRedisDetailsRowMapper());
	}

	/**
	 * 
	 * @param kyc
	 */
	public int update(KYCRedisDetails kyc) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(kyc);
		int status = namedParameterJdbcTemplate.update(String.format(queries.get("kyc_email_details_update")), namedParameters);
		return status;
	}

	
	/**
	 * 
	 * @param kycDetailsList
	 */
	@org.springframework.transaction.annotation.Transactional
	public void batchUpdate(List<KYCRedisDetails> kycDetailsList) {
		String updateQuery = "UPDATE Onboarding.kyc_redis_details SET active = 0 WHERE id = :id";
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(kycDetailsList);
		namedParameterJdbcTemplate.batchUpdate(updateQuery, batch);
		logger.info("key email details batch updated successfully {}", kycDetailsList.size());
	}

}
